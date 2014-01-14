/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import eu.stratosphere.nephele.event.task.AbstractEvent;
import eu.stratosphere.runtime.io.channels.ChannelID;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.runtime.io.network.envelope.Envelope;

/**
 * An unknown receiver event can be used by the framework to inform a sender task that the delivery of a
 * {@link eu.stratosphere.runtime.io.network.envelope.Envelope} has failed since the receiver could not be found.
 * 
 */
public final class ReceiverNotFoundEvent extends AbstractEvent {

	/**
	 * The sequence number that will be set for transfer envelopes which contain the receiver not found event.
	 */
	private static final int RECEIVER_NOT_FOUND_SEQUENCE_NUMBER = 0;

	/**
	 * The ID of the receiver which could not be found
	 */
	private ChannelID receiverID;

	/**
	 * The sequence number of the envelope this event refers to
	 */
	private int sequenceNumber;

	/**
	 * Constructs a new unknown receiver event.
	 * 
	 * @param receiverID
	 *        the ID of the receiver which could not be found
	 * @param sequenceNumber
	 *        the sequence number of the envelope this event refers to
	 */
	public ReceiverNotFoundEvent(final ChannelID receiverID, final int sequenceNumber) {

		if (receiverID == null) {
			throw new IllegalArgumentException("Argument unknownReceiverID must not be null");
		}

		if (sequenceNumber < 0) {
			throw new IllegalArgumentException("Argument sequenceNumber must be non-negative");
		}

		this.receiverID = receiverID;
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * Default constructor for serialization/deserialization.
	 */
	public ReceiverNotFoundEvent() {

		this.receiverID = new ChannelID();
	}

	/**
	 * Returns the ID of the receiver which could not be found.
	 * 
	 * @return the ID of the receiver which could not be found
	 */
	public ChannelID getReceiverID() {

		return this.receiverID;
	}

	/**
	 * Returns the sequence number of the envelope this event refers to.
	 * 
	 * @return the sequence number of the envelope this event refers to
	 */
	public int getSequenceNumber() {

		return this.sequenceNumber;
	}


	@Override
	public void write(final DataOutput out) throws IOException {

		this.receiverID.write(out);
		out.writeInt(this.sequenceNumber);
	}


	@Override
	public void read(final DataInput in) throws IOException {

		this.receiverID.read(in);
		this.sequenceNumber = in.readInt();
	}

	/**
	 * Creates a transfer envelope which only contains a ReceiverNotFoundEvent.
	 * 
	 * @param jobID
	 *        the ID of the job the event relates to.
	 * @param receiver
	 *        the channel ID of the receiver that could not be found
	 * @param sequenceNumber
	 *        the sequence number of the transfer envelope which caused the creation of this event
	 * @return a transfer envelope which only contains a ReceiverNotFoundEvent
	 */
	public static Envelope createEnvelopeWithEvent(final JobID jobID, final ChannelID receiver,
			final int sequenceNumber) {

		final Envelope envelope = new Envelope(RECEIVER_NOT_FOUND_SEQUENCE_NUMBER, jobID,
			receiver);

		final ReceiverNotFoundEvent unknownReceiverEvent = new ReceiverNotFoundEvent(receiver, sequenceNumber);

		envelope.serializeEventList(Arrays.asList(unknownReceiverEvent));

		return envelope;
	}

	/**
	 * Checks if the given envelope only contains a ReceiverNotFoundEvent.
	 * 
	 * @param envelope
	 *        the envelope to be checked
	 * @return <code>true</code> if the envelope only contains a ReceiverNotFoundEvent, <code>false</code> otherwise
	 */
	public static boolean isReceiverNotFoundEvent(final Envelope envelope) {

		if (envelope.getSequenceNumber() != RECEIVER_NOT_FOUND_SEQUENCE_NUMBER) {
			return false;
		}

		if (envelope.getBuffer() != null) {
			return false;
		}

		List<? extends AbstractEvent> events = envelope.deserializeEvents();

		if (events.size() != 1) {
			return false;
		}

		if (!(events.get(0) instanceof ReceiverNotFoundEvent)) {
			return false;
		}

		return true;
	}
}
