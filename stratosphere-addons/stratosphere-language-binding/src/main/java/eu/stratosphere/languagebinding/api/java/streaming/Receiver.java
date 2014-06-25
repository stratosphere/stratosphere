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

import com.google.protobuf.InvalidProtocolBufferException;
//CHECKSTYLE.OFF: AvoidStarImport - tuple imports
import eu.stratosphere.api.java.tuple.*;
//CHECKSTYLE.ON: AvoidStarImport
import eu.stratosphere.languagebinding.api.java.streaming.Streamer.Sentinel;
import eu.stratosphere.util.Collector;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * General purpose class to receive data via input streams.
 */
public abstract class Receiver extends Thread {
	public final InputStream inStream;
	public BlockingQueue<Collector<Object>> collectors = new SynchronousQueue();

	public Receiver(InputStream inStream) {
		this.inStream = inStream;
	}

	@Override
	public void run() {
		try {
			Collector c;
			boolean done = false;
			while (!done) {
				try {
					while (!((c = collectors.take()) instanceof Sentinel)) {
						receiveRecords(c);
					}
					done = true;
				} catch (InterruptedException ex) {
				}
			}
		} catch (InvalidProtocolBufferException ipbe) {
			System.out.println("An error occurred while receiving data. This usually means that the python process "
					+ "has prematurely terminated (or may have never started)");
		} catch (IOException ioe) {
			if (ioe.getMessage().startsWith("Stream closed")) {
				System.out.println(
						"The python process has prematurely terminated (or may have never started).");
			}
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * Closes this receiver.
	 * @throws IOException 
	 */
	public void close() throws IOException {
		inStream.close();
	}

	/**
	 * Reads a single record from the input stream and returns it.
	 * @return received record
	 * @throws java.io.IOException
	 */
	public abstract Object receiveRecord() throws IOException;

	/**
	 * Reads multiple records from the input stream and collects it.
	 * @param collector
	 * @throws java.io.IOException
	 */
	public abstract void receiveRecords(Collector collector) throws IOException;

	/**
	 * Returns a tuple of the desired size.
	 * @param size
	 * @return tuple
	 */
	public static Tuple createTuple(int size) {
		switch (size) {
			case 0:
				return null;
			case 1:
				return new Tuple1();
			case 2:
				return new Tuple2();
			case 3:
				return new Tuple3();
			case 4:
				return new Tuple4();
			case 5:
				return new Tuple5();
			case 6:
				return new Tuple6();
			case 7:
				return new Tuple7();
			case 8:
				return new Tuple8();
			case 9:
				return new Tuple9();
			case 10:
				return new Tuple10();
			case 11:
				return new Tuple11();
			case 12:
				return new Tuple12();
			case 13:
				return new Tuple13();
			case 14:
				return new Tuple14();
			case 15:
				return new Tuple15();
			case 16:
				return new Tuple16();
			case 17:
				return new Tuple17();
			case 18:
				return new Tuple18();
			case 19:
				return new Tuple19();
			case 20:
				return new Tuple20();
			case 21:
				return new Tuple21();
			case 22:
				return new Tuple22();
			case 23:
				return new Tuple23();
			case 24:
				return new Tuple24();
			case 25:
				return new Tuple25();
			default:
				throw new IllegalArgumentException("tuple size not supported.");
		}
	}

}
