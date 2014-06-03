/***********************************************************************************************************************
 *
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
 *
 **********************************************************************************************************************/
package eu.stratosphere.languagebinding.api.java.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 An object of this class prints all messages from an input stream to the console.
 */
public class StreamPrinter extends Thread {
	private final BufferedReader reader;

	public StreamPrinter(InputStream stream) {
		this.reader = new BufferedReader(new InputStreamReader(stream));
	}

	@Override
	public void run() {
		boolean done = false;
		while (!done) {
			try {
				String line = reader.readLine();
				if (line != null) {
					System.out.println(line);
				}
			} catch (IOException ex) {
				done = true;
			}
		}
	}
}
