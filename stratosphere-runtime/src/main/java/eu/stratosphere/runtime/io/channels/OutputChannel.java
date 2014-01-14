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

package eu.stratosphere.runtime.io.channels;

import eu.stratosphere.nephele.event.task.AbstractEvent;
import eu.stratosphere.nephele.event.task.AbstractTaskEvent;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.runtime.io.Buffer;
import eu.stratosphere.runtime.io.network.envelope.Envelope;
import eu.stratosphere.runtime.io.gates.OutputGate;
import eu.stratosphere.runtime.io.network.ReceiverNotFoundEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class OutputChannel extends Channel {

	private static final Log LOG = LogFactory.getLog(OutputChannel.class);

	private final OutputGate outputGate;

	private final Queue<AbstractEvent> incomingEvents;

	private boolean isCloseRequested;

	private boolean isCloseAcknowledged;

	private int seqNum;

	private int seqNumLastNotFound;

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new output channel object.
	 *
	 * @param outputGate the output gate this channel is connected to
	 * @param index the index of the channel in the output gate
	 * @param id the ID of the channel
	 * @param connectedId the ID of the channel this channel is connected to
	 * @param type the type of this channel
	 */
	public OutputChannel(OutputGate outputGate, int index, ChannelID id, ChannelID connectedId, ChannelType type) {
		super(index, id, connectedId, type);

		this.outputGate = outputGate;
		this.incomingEvents = new LinkedBlockingDeque<AbstractEvent>();
		this.seqNumLastNotFound = -1;
	}

	// -----------------------------------------------------------------------------------------------------------------
	//                                           Data processing
	// -----------------------------------------------------------------------------------------------------------------

	public void sendBuffer(Buffer buffer) throws IOException, InterruptedException {
		processIncomingEvents();

		if (this.isCloseRequested) {
			throw new IllegalStateException(String.format("Channel %s already requested to be closed.", getID()));
		}

		Envelope envelope = createNextEnvelope();
		envelope.setBuffer(buffer);
		this.envelopeDispatcher.dispatchFromOutputChannel(envelope);
	}

	public void sendEvent(AbstractEvent event) throws IOException, InterruptedException {
		processIncomingEvents();

		if (this.isCloseRequested) {
			throw new IllegalStateException(String.format("Channel %s already requested to be closed.", getID()));
		}

		Envelope envelope = createNextEnvelope();
		envelope.serializeEventList(Arrays.asList(event));
		this.envelopeDispatcher.dispatchFromOutputChannel(envelope);
	}

	public void sendBufferAndEvent(Buffer buffer, AbstractEvent event) throws IOException, InterruptedException {
		processIncomingEvents();

		if (this.isCloseRequested) {
			throw new IllegalStateException(String.format("Channel %s already requested to be closed.", getID()));
		}

		Envelope envelope = createNextEnvelope();
		envelope.setBuffer(buffer);
		envelope.serializeEventList(Arrays.asList(event));
		this.envelopeDispatcher.dispatchFromOutputChannel(envelope);
	}

	// -----------------------------------------------------------------------------------------------------------------
	//                                          Event processing
	// -----------------------------------------------------------------------------------------------------------------

	@Override
	public void queueEnvelope(Envelope envelope) {
		if (envelope.hasBuffer()) {
			throw new IllegalStateException("Envelope for OutputChannel has Buffer attached.");
		}

		for (AbstractEvent event : envelope.deserializeEvents()) {
			if (event instanceof AbstractTaskEvent) {
				processEvent(event);
			} else {
				this.incomingEvents.offer(event);
			}
		}
	}

	public void processIncomingEvents() {
		// sync processing
		AbstractEvent event = this.incomingEvents.poll();
		while (event != null) {
			if (event instanceof ChannelCloseEvent) {
				if (!this.isCloseRequested) {
					throw new IllegalStateException("Received channel close ACK without close request.");
				}

				this.isCloseAcknowledged = true;
			} else if (event instanceof ReceiverNotFoundEvent) {
				this.seqNumLastNotFound = ((ReceiverNotFoundEvent) event).getSequenceNumber();
			} else if (event instanceof AbstractTaskEvent) {
				throw new IllegalStateException("Received synchronous task event: " + event);
			}

			event = this.incomingEvents.poll();
		}
	}

	public void processEvent(AbstractEvent event) {
		// async processing
		if (event instanceof AbstractTaskEvent) {
			this.outputGate.deliverEvent((AbstractTaskEvent) event);
		} else {
			LOG.error("Channel " + getID() + " received unknown event " + event);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	//                                              Shutdown
	// -----------------------------------------------------------------------------------------------------------------

	public void requestClose() throws IOException, InterruptedException {
		if (this.isCloseRequested) {
			return;
		}

		this.isCloseRequested = true;

		Envelope envelope = createNextEnvelope();
		envelope.serializeEventList(Arrays.asList(new ChannelCloseEvent()));
		this.envelopeDispatcher.dispatchFromOutputChannel(envelope);
	}

	@Override
	public boolean isClosed() throws IOException, InterruptedException {
		if (this.isCloseRequested) {
			processIncomingEvents();

			return this.isCloseAcknowledged || (this.seqNumLastNotFound + 1) == this.seqNum;
		}

		return false;
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Override
	public boolean isInputChannel() {
		return false;
	}

	@Override
	public JobID getJobID() {
		return this.outputGate.getJobID();
	}

	private Envelope createNextEnvelope() {
		return new Envelope(this.seqNum++, getJobID(), getID());
	}

	@Override
	public void transferEvent(AbstractEvent event) throws IOException, InterruptedException {
		// TODO remove with pending changes for input side
	}

	@Override
	public void releaseAllResources() {
		// nothing to do for buffer oriented runtime => TODO remove with pending changes for input side
	}

	@Override
	public void destroy() {
		// nothing to do for buffer oriented runtime => TODO remove with pending changes for input side
	}
}
