/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ********************************************************************************************************************/
package eu.stratosphere.languagebinding.api.java.streaming;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * General purpose class to send data via output streams.
 */
public abstract class Sender {

	public final OutputStream outStream;
	public final boolean done = false;

	public Sender(OutputStream outStream) {
		this.outStream = outStream;
	}

	/**
	 Closes this sender.
	 @throws IOException 
	 */
	public void close() throws IOException {
		outStream.close();
	}

	/**
	 * Sends a single record to the output stream.
	 *
	 * @param tuple tuple to send.
	 * @throws IOException
	 */
	public abstract void sendRecord(Object tuple) throws IOException;

	/**
	 * Sends a single record belonging to a specific group to the output stream.
	 *
	 * @param tuple tuple to send.
	 * @param group tuple group
	 * @throws IOException
	 */
	public abstract void sendRecord(Object tuple, int group) throws IOException;

	public void sendRecords(Iterator<Object> values) throws IOException {
		while (values.hasNext()) {
			sendRecord(values.next());
		}
		sendCompletionSignal();
	}

	public void sendRecords(Iterator<Object> values1, Iterator<Object> values2) throws IOException {
		boolean completionSignalSent1 = false;
		boolean completionSignalSent2 = false;
		while (!completionSignalSent1 || !completionSignalSent2) {
			if (!completionSignalSent1) {
				for (int c = 0; c < 10; c++) {
					if (values1.hasNext()) {
						sendRecord(values1.next(), 0);
					} else {
						sendCompletionSignal();
						completionSignalSent1 = true;
						break;
					}
				}
			}
			if (!completionSignalSent2) {
				for (int c = 0; c < 10; c++) {
					if (values2.hasNext()) {
						sendRecord(values2.next(), 1);
					} else {
						sendCompletionSignal();
						completionSignalSent2 = true;
						break;
					}
				}
			}
		}
	}

	public abstract void sendCompletionSignal() throws IOException;
}
