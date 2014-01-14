/***********************************************************************************************************************
 * Copyright (C) 2010-2014 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/

package eu.stratosphere.runtime.io.network;

import eu.stratosphere.core.io.IOReadableWritable;
import eu.stratosphere.nephele.execution.Environment;
import eu.stratosphere.nephele.execution.RuntimeEnvironment;
import eu.stratosphere.nephele.executiongraph.ExecutionVertexID;
import eu.stratosphere.nephele.instance.InstanceConnectionInfo;
import eu.stratosphere.runtime.io.channels.Channel;
import eu.stratosphere.runtime.io.channels.ChannelID;
import eu.stratosphere.runtime.io.channels.ChannelType;
import eu.stratosphere.runtime.io.channels.InputChannel;
import eu.stratosphere.runtime.io.channels.OutputChannel;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.protocols.ChannelLookupProtocol;
import eu.stratosphere.nephele.taskmanager.Task;
import eu.stratosphere.nephele.AbstractID;
import eu.stratosphere.runtime.io.Buffer;
import eu.stratosphere.runtime.io.network.bufferprovider.BufferProvider;
import eu.stratosphere.runtime.io.network.bufferprovider.BufferProviderBroker;
import eu.stratosphere.runtime.io.network.bufferprovider.GlobalBufferPool;
import eu.stratosphere.runtime.io.network.bufferprovider.LocalBufferPoolOwner;
import eu.stratosphere.runtime.io.network.envelope.Envelope;
import eu.stratosphere.runtime.io.network.envelope.EnvelopeDispatcher;
import eu.stratosphere.runtime.io.network.envelope.EnvelopeReceiverList;
import eu.stratosphere.runtime.io.gates.GateID;
import eu.stratosphere.runtime.io.gates.InputGate;
import eu.stratosphere.runtime.io.gates.OutputGate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The channel manager sets up the network buffers and dispatches data between channels.
 */
public final class ChannelManager implements EnvelopeDispatcher, BufferProviderBroker {

	private static final Log LOG = LogFactory.getLog(ChannelManager.class);

	private final ChannelLookupProtocol channelLookupService;

	private final InstanceConnectionInfo connectionInfo;

	private final Map<ChannelID, Channel> channels;

	private final Map<AbstractID, LocalBufferPoolOwner> localBuffersPools;

	private final Map<ChannelID, EnvelopeReceiverList> receiverCache;

	private final GlobalBufferPool globalBufferPool;

	private final NetworkConnectionManager networkConnectionManager;

	// -----------------------------------------------------------------------------------------------------------------

	public ChannelManager(ChannelLookupProtocol channelLookupService, InstanceConnectionInfo connectionInfo,
						  int numNetworkBuffers, int networkBufferSize) throws IOException {
		this.channelLookupService = channelLookupService;
		this.connectionInfo = connectionInfo;
		this.globalBufferPool = new GlobalBufferPool(numNetworkBuffers, networkBufferSize);
		this.networkConnectionManager = new NetworkConnectionManager(this, connectionInfo.address(), connectionInfo.dataPort());

		// management data structures
		this.channels = new ConcurrentHashMap<ChannelID, Channel>();
		this.receiverCache = new ConcurrentHashMap<ChannelID, EnvelopeReceiverList>();
		this.localBuffersPools = new ConcurrentHashMap<AbstractID, LocalBufferPoolOwner>();
	}

	public void shutdown() {
		this.networkConnectionManager.shutDown();
		this.globalBufferPool.destroy();
	}

	// -----------------------------------------------------------------------------------------------------------------
	//                                               Task registration
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Registers the given task with the channel manager.
	 *
	 * @param task the task to be registered
	 * @throws InsufficientResourcesException thrown if not enough buffers available to safely run this task
	 */
	public void register(Task task) throws InsufficientResourcesException {
		// Check if we can safely run this task with the given buffers
		ensureBufferAvailability(task);

		RuntimeEnvironment environment = task.getRuntimeEnvironment();

		// -------------------------------------------------------------------------------------------------------------
		//                                       Register output channels
		// -------------------------------------------------------------------------------------------------------------

		environment.registerGlobalBufferPool(this.globalBufferPool);

		if (this.localBuffersPools.containsKey(task.getVertexID())) {
			throw new IllegalStateException("Vertex " + task.getVertexID() + " has a previous buffer pool owner");
		}

		for (OutputGate gate : environment.outputGates()) {
			// add receiver list hints
			for (OutputChannel channel : gate.channels()) {
				// register envelope dispatcher with the channel
				channel.registerEnvelopeDispatcher(this);

				switch (channel.getChannelType()) {
					case IN_MEMORY:
						addReceiverListHint(channel.getID(), channel.getConnectedId());
						break;
					case NETWORK:
						addReceiverListHint(channel.getConnectedId(), channel.getID());
						break;
				}

				this.channels.put(channel.getID(), channel);
			}
		}

		this.localBuffersPools.put(task.getVertexID(), environment);

		// -------------------------------------------------------------------------------------------------------------
		//                                       Register input channels
		// -------------------------------------------------------------------------------------------------------------

		// register global
		for (InputGate gate : environment.inputGates()) {
			gate.registerGlobalBufferPool(this.globalBufferPool);

			for (int i = 0; i < gate.getNumberOfInputChannels(); i++) {
				InputChannel<? extends IOReadableWritable> channel = gate.getInputChannel(i);
				channel.registerEnvelopeDispatcher(this);

				if (channel.getChannelType() == ChannelType.IN_MEMORY) {
					addReceiverListHint(channel.getID(), channel.getConnectedId());
				}

				this.channels.put(channel.getID(), channel);
			}

			this.localBuffersPools.put(gate.getGateID(), gate);
		}

		// the number of channels per buffers has changed after unregistering the task
		// => redistribute the number of designated buffers of the registered local buffer pools
		redistributeBuffers();
	}

	/**
	 * Unregisters the given task from the channel manager.
	 *
	 * @param vertexId the ID of the task to be unregistered
	 * @param task the task to be unregistered
	 */
	public void unregister(ExecutionVertexID vertexId, Task task) {
		final Environment environment = task.getEnvironment();

		// destroy and remove OUTPUT channels from registered channels and cache
		for (ChannelID id : environment.getOutputChannelIDs()) {
			Channel channel = this.channels.remove(id);
			if (channel != null) {
				channel.destroy();
			}

			this.receiverCache.remove(channel);
		}

		// destroy and remove INPUT channels from registered channels and cache
		for (ChannelID id : environment.getInputChannelIDs()) {
			Channel channel = this.channels.remove(id);
			if (channel != null) {
				channel.destroy();
			}

			this.receiverCache.remove(channel);
		}

		// clear and remove INPUT side buffer pools
		for (GateID id : environment.getInputGateIDs()) {
			LocalBufferPoolOwner bufferPool = this.localBuffersPools.remove(id);
			if (bufferPool != null) {
				bufferPool.clearLocalBufferPool();
			}
		}

		// clear and remove OUTPUT side buffer pool
		LocalBufferPoolOwner bufferPool = this.localBuffersPools.remove(vertexId);
		if (bufferPool != null) {
			bufferPool.clearLocalBufferPool();
		}

		// the number of channels per buffers has changed after unregistering the task
		// => redistribute the number of designated buffers of the registered local buffer pools
		redistributeBuffers();
	}

	/**
	 * Ensures that the channel manager has enough buffers to execute the given task.
	 * <p>
	 * If there is less than one buffer per channel available, an InsufficientResourcesException will be thrown,
	 * because of possible deadlocks. With more then one buffer per channel, deadlock-freedom is guaranteed.
	 *
	 * @param task task to be executed
	 * @throws InsufficientResourcesException thrown if not enough buffers available to execute the task
	 */
	private void ensureBufferAvailability(Task task) throws InsufficientResourcesException {
		Environment env = task.getEnvironment();

		int numBuffers = this.globalBufferPool.numBuffers();
		// existing channels + channels of the task
		int numChannels = this.channels.size() + env.getNumberOfOutputChannels() + env.getNumberOfInputChannels();

		// need at least one buffer per channel
		if (numBuffers / numChannels < 1) {
			String msg = String.format("%s has not enough buffers to safely execute %s (%d buffers missing)",
					this.connectionInfo.hostname(), env.getTaskName(), numChannels - numBuffers);

			throw new InsufficientResourcesException(msg);
		}
	}

	/**
	 * Redistributes the buffers among the registered buffer pools. This method is called after each task registration
	 * and unregistration.
	 * <p>
	 * Every registered buffer pool gets buffers according to its number of channels weighted by the current buffer to
	 * channel ratio.
	 */
	private void redistributeBuffers() {
		if (this.localBuffersPools.isEmpty() | this.channels.size() == 0) {
			return;
		}

		int numBuffers = this.globalBufferPool.numBuffers();
		int numChannels = this.channels.size();

		double buffersPerChannel = numBuffers / (double) numChannels;

		if (buffersPerChannel < 1.0) {
			throw new RuntimeException("System has not enough buffers to execute tasks.");
		}

		// redistribute number of designated buffers per buffer pool
		for (LocalBufferPoolOwner bufferPool : this.localBuffersPools.values()) {
			int numDesignatedBuffers = (int) Math.ceil(buffersPerChannel * bufferPool.getNumberOfChannels());
			bufferPool.setDesignatedNumberOfBuffers(numDesignatedBuffers);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	//                                           Envelope processing
	// -----------------------------------------------------------------------------------------------------------------

	private void processEnvelope(Envelope envelope, boolean freeSourceBuffer) throws IOException {
		EnvelopeReceiverList receiverList;

		try {
			receiverList = getReceiverList(envelope.getJobID(), envelope.getSource());
		} catch (IOException e) {
			releaseEnvelope(envelope);
			throw e;
		}

		if (receiverList == null) {
			releaseEnvelope(envelope);
			return;
		}

		// This envelope is known to have either no buffer or a memory-based input buffer
		if (envelope.getBuffer() == null) {
			processEnvelopeWithoutBuffer(envelope, receiverList);
		} else {
			processEnvelopeWithBuffer(envelope, receiverList, freeSourceBuffer);
		}
	}

	private void processEnvelopeWithBuffer(Envelope envelope, EnvelopeReceiverList receiverList, boolean freeSourceBuffer) throws IOException {
		// Handle the most common (unicast) case first
		if (!freeSourceBuffer) {

			List<ChannelID> localReceivers = receiverList.getLocalReceivers();
			if (localReceivers.size() != 1) {
				LOG.error("Expected receiver list to have exactly one element");
			}

			ChannelID localReceiver = localReceivers.get(0);

			Channel channel = this.channels.get(localReceiver);
			if (channel == null) {
				try {
					sendReceiverNotFoundEvent(envelope, localReceiver);
				} finally {
					releaseEnvelope(envelope);
				}
				return;
			}

			if (!channel.isInputChannel()) {
				LOG.error("Local receiver " + localReceiver + " is not an input channel, but is supposed to accept a buffer");
			}

			channel.queueEnvelope(envelope);

			return;
		}

		// This is the in-memory or multicast case
		final Buffer srcBuffer = envelope.getBuffer();

		try {
			if (receiverList.hasLocalReceivers()) {
				for (ChannelID receiver : receiverList.getLocalReceivers()) {
					Channel channel = this.channels.get(receiver);

					if (channel == null) {
						sendReceiverNotFoundEvent(envelope, receiver);
						continue;
					}

					if (!channel.isInputChannel()) {
						LOG.error("Local receiver " + receiver + " is not an input channel, but is supposed to accept a buffer");
						continue;
					}

					final InputChannel inputChannel = (InputChannel) channel;

					Buffer destBuffer = null;
					try {
						try {
							destBuffer = inputChannel.requestBufferBlocking(srcBuffer.size());
						} catch (InterruptedException e) {
							e.printStackTrace();
							throw new IOException(e.getMessage());
						}
						srcBuffer.copyToBuffer(destBuffer);
					} catch (IOException e) {
						if (destBuffer != null) {
							destBuffer.recycleBuffer();
						}
						throw e;
					}
					// TODO: See if we can save one duplicate step here
					final Envelope dup = envelope.duplicateWithoutBuffer();
					dup.setBuffer(destBuffer);
					inputChannel.queueEnvelope(dup);
				}
			}

			if (receiverList.hasRemoteReceivers()) {
				List<RemoteReceiver> remoteReceivers = receiverList.getRemoteReceivers();

				// Generate sender hint before sending the first envelope over the network
				if (envelope.getSequenceNumber() == 0) {
					generateSenderHint(envelope, remoteReceivers);
				}

				for (RemoteReceiver receiver : remoteReceivers) {
					Envelope dup = envelope.duplicate();
					this.networkConnectionManager.queueEnvelopeForTransfer(receiver, dup);
				}
			}
		} finally {
			// Recycle the source buffer
			srcBuffer.recycleBuffer();
		}
	}

	private void processEnvelopeWithoutBuffer(Envelope envelope, EnvelopeReceiverList receiverList) throws IOException {
		// local receivers => no need to copy anything
		for (ChannelID receiverId : receiverList.getLocalReceivers()) {
			Channel receiver = this.channels.get(receiverId);

			if (receiver == null) {
				sendReceiverNotFoundEvent(envelope, receiverId);
				continue;
			}

			receiver.queueEnvelope(envelope);
		}

		// remote receivers => sender hint and queue envelopes
		if (receiverList.hasRemoteReceivers()) {
			List<RemoteReceiver> remoteReceivers = receiverList.getRemoteReceivers();

			// Generate sender hint before sending the first envelope over the network
			if (envelope.getSequenceNumber() == 0) {
				generateSenderHint(envelope, remoteReceivers);
			}

			for (RemoteReceiver receiver : remoteReceivers) {
				this.networkConnectionManager.queueEnvelopeForTransfer(receiver, envelope);
			}
		}
	}

	private void sendReceiverNotFoundEvent(Envelope envelope, ChannelID receiver) throws IOException {
		if (ReceiverNotFoundEvent.isReceiverNotFoundEvent(envelope)) {
			LOG.info("Dropping request to send ReceiverNotFoundEvent as response to ReceiverNotFoundEvent");
			return;
		}

		JobID jobID = envelope.getJobID();

		Envelope receiverNotFoundEnvelope = ReceiverNotFoundEvent.createEnvelopeWithEvent(jobID, receiver, envelope.getSequenceNumber());

		EnvelopeReceiverList receiverList = getReceiverList(jobID, receiver);
		if (receiverList == null) {
			return;
		}

		processEnvelopeWithoutBuffer(receiverNotFoundEnvelope, receiverList);
	}

	private void releaseEnvelope(Envelope envelope) {
		Buffer buffer = envelope.getBuffer();
		if (buffer != null) {
			buffer.recycleBuffer();
		}
	}

	private void addReceiverListHint(ChannelID source, ChannelID localReceiver) {
		EnvelopeReceiverList receiverList = new EnvelopeReceiverList(localReceiver);

		if (this.receiverCache.put(source, receiverList) != null) {
			LOG.warn("Receiver cache already contained entry for " + source);
		}
	}

	private void addReceiverListHint(ChannelID source, RemoteReceiver remoteReceiver) {
		EnvelopeReceiverList receiverList = new EnvelopeReceiverList(remoteReceiver);

		if (this.receiverCache.put(source, receiverList) != null) {
			LOG.warn("Receiver cache already contained entry for " + source);
		}
	}

	private void generateSenderHint(Envelope envelope, List<RemoteReceiver> remoteReceivers) {
		Channel channel = this.channels.get(envelope.getSource());
		if (channel == null) {
			LOG.error("Cannot find channel for channel ID " + envelope.getSource());
			return;
		}

		// Only generate sender hints for output channels
		if (channel.isInputChannel()) {
			return;
		}

		final ChannelID remoteSourceID = channel.getConnectedId();
		final int connectionIndex = remoteReceivers.get(0).getConnectionIndex();
		final InetSocketAddress isa = new InetSocketAddress(this.connectionInfo.address(),
			this.connectionInfo.dataPort());

		final RemoteReceiver remoteReceiver = new RemoteReceiver(isa, connectionIndex);
		final Envelope senderHint = SenderHintEvent.createEnvelopeWithEvent(envelope, remoteSourceID,
			remoteReceiver);

		for (RemoteReceiver receiver : remoteReceivers) {
			this.networkConnectionManager.queueEnvelopeForTransfer(receiver, senderHint);
		}
	}

	/**
	 * Returns the list of receivers for transfer envelopes produced by the channel with the given source channel ID.
	 *
	 * @param jobID
	 *        the ID of the job the given channel ID belongs to
	 * @param sourceChannelID
	 *        the source channel ID for which the receiver list shall be retrieved
	 * @return the list of receivers or <code>null</code> if the receiver could not be determined
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private EnvelopeReceiverList getReceiverList(JobID jobID, ChannelID sourceChannelID) throws IOException {
		EnvelopeReceiverList receiverList = this.receiverCache.get(sourceChannelID);

		if (receiverList != null) {
			return receiverList;
		}

		while (true) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}

			ConnectionInfoLookupResponse lookupResponse;
			synchronized (this.channelLookupService) {
				lookupResponse = this.channelLookupService.lookupConnectionInfo(
					this.connectionInfo, jobID, sourceChannelID);
			}

			if (lookupResponse.isJobAborting()) {
				break;
			}

			if (lookupResponse.receiverNotFound()) {
				LOG.error("Cannot find task(s) waiting for data from source channel with ID " + sourceChannelID);
				break;
			}

			if (lookupResponse.receiverNotReady()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					throw new IOException("Lookup was interrupted.");
				}
				continue;
			}

			if (lookupResponse.receiverReady()) {
				receiverList = new EnvelopeReceiverList(lookupResponse);
				break;
			}
		}

		if (receiverList != null) {
			this.receiverCache.put(sourceChannelID, receiverList);

			if (LOG.isDebugEnabled()) {
				final StringBuilder sb = new StringBuilder();
				sb.append("Receiver list for source channel ID " + sourceChannelID + " at task manager "
					+ this.connectionInfo + "\n");

				if (receiverList.hasLocalReceivers()) {
					sb.append("\tLocal receivers:\n");
					final Iterator<ChannelID> it = receiverList.getLocalReceivers().iterator();
					while (it.hasNext()) {
						sb.append("\t\t" + it.next() + "\n");
					}
				}

				if (receiverList.hasRemoteReceivers()) {
					sb.append("Remote receivers:\n");
					final Iterator<RemoteReceiver> it = receiverList.getRemoteReceivers().iterator();
					while (it.hasNext()) {
						sb.append("\t\t" + it.next() + "\n");
					}
				}

				LOG.debug(sb.toString());
			}
		}

		return receiverList;
	}

	/**
	 * Invalidates the entries identified by the given channel IDs from the receiver lookup cache.
	 *
	 * @param channelIDs channel IDs for entries to invalidate
	 */
	public void invalidateLookupCacheEntries(Set<ChannelID> channelIDs) {
		for (ChannelID id : channelIDs) {
			this.receiverCache.remove(id);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	//                                       EnvelopeDispatcher methods
	// -----------------------------------------------------------------------------------------------------------------

	@Override
	public void dispatchFromOutputChannel(Envelope envelope) throws IOException, InterruptedException {
		processEnvelope(envelope, true);
	}

	@Override
	public void dispatchFromInputChannel(Envelope envelope) throws IOException, InterruptedException {
		processEnvelope(envelope, false);
	}

	@Override
	public void dispatchFromNetwork(Envelope envelope, boolean freeSourceBuffer) throws IOException, InterruptedException {
		// Check if the envelope is the special envelope with the sender hint event
		if (SenderHintEvent.isSenderHintEvent(envelope)) {
			// Check if this is the final destination of the sender hint event before adding it
			final SenderHintEvent seh = (SenderHintEvent) envelope.deserializeEvents().get(0);
			if (this.channels.get(seh.getSource()) != null) {
				addReceiverListHint(seh.getSource(), seh.getRemoteReceiver());
				return;
			}
		}

		processEnvelope(envelope, freeSourceBuffer);
	}

	// -----------------------------------------------------------------------------------------------------------------
	//                                       BufferProviderBroker methods
	// -----------------------------------------------------------------------------------------------------------------

	@Override
	public BufferProvider getBufferProvider(JobID jobID, ChannelID sourceChannelID) throws IOException {
		final EnvelopeReceiverList receiverList = getReceiverList(jobID, sourceChannelID);

		// Receiver could not be determined, use transit buffer pool to read data from channel
		if (receiverList == null) {
			throw new IOException("Receiver for channel was not found.");
		}

		if (receiverList.hasLocalReceivers() && !receiverList.hasRemoteReceivers()) {

			final List<ChannelID> localReceivers = receiverList.getLocalReceivers();
			if (localReceivers.size() == 1) {
				// Unicast case, get final buffer provider

				final ChannelID localReceiver = localReceivers.get(0);
				final Channel channel = this.channels.get(localReceiver);
				if (channel == null) {
					// Use the transit buffer for this purpose, data will be discarded in most cases anyway.
					throw new IOException("Receiver for input was not found.");
				}

				if (!channel.isInputChannel()) {
					throw new IOException("Channel context for local receiver " + localReceiver
							+ " is not an input channel context");
				}

				return (InputChannel) channel;
			}
		}

		throw new IOException("The destination to be looked up is not a single local endpoint.");
	}

	// -----------------------------------------------------------------------------------------------------------------

	public void logBufferUtilization() {
		System.out.println("Buffer utilization at " + System.currentTimeMillis());

		System.out.println("\tUnused global buffers: " + this.globalBufferPool.numAvailableBuffers());

		System.out.println("\tLocal buffer pool status:");

		for (LocalBufferPoolOwner bufferPool : this.localBuffersPools.values()) {
			bufferPool.logBufferUtilization();
		}

		this.networkConnectionManager.logBufferUtilization();

		System.out.println("\tIncoming connections:");

		for (Channel channel : this.channels.values()) {
			if (channel.isInputChannel()) {
				((InputChannel) channel).logQueuedEnvelopes();
			}
		}
	}
}
