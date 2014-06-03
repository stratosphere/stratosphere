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
package eu.stratosphere.languagebinding.api.java.python.functions;

import com.google.protobuf.InvalidProtocolBufferException;
import eu.stratosphere.api.java.functions.FilterFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.languagebinding.api.java.python.streaming.PythonStreamer;
import eu.stratosphere.languagebinding.api.java.streaming.Converter;
import java.io.IOException;

/**
 * Cross-function that uses an external python function.
 * @param <IN>
 */
public class PythonFilter<IN> extends FilterFunction<IN> {
	private final String scriptName;
	private PythonStreamer streamer;
	public Converter inConverter;

	public PythonFilter(String scriptName) {
		this.scriptName = scriptName;
	}

	public PythonFilter(String scriptName, Converter inConverter) {
		this(scriptName);
		this.inConverter = inConverter;
	}

	/**
	 * Opens this function.
	 *
	 * @param ignored ignored
	 * @throws IOException
	 */
	@Override
	public void open(Configuration ignored) throws IOException {
		streamer = inConverter == null
				? new PythonStreamer(this, scriptName)
				: new PythonStreamer(this, scriptName, inConverter, null);
		streamer.open();
	}

	/**
	 * Calls the external python function.
	 * @param value function input
	 * @return function output
	 * @throws IOException
	 */
	@Override
	public final boolean filter(IN value) throws Exception {
		try {
			return (Boolean) streamer.stream(value);
		} catch (InvalidProtocolBufferException ipbe) {
			throw new IOException("An error occurred while receiving data. This usually means that the python process "
					+ "has prematurely terminated (or may have never started)", ipbe);
		} catch (IOException ioe) {
			if (ioe.getMessage().startsWith("Stream closed")) {
				throw new IOException(
						"The python process has prematurely terminated (or may have never started).", ioe);
			}
			throw ioe;
		}
	}

	/**
	 * Closes this function.
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		streamer.close();
	}
}
