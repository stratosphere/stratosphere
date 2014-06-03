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
import eu.stratosphere.api.java.functions.CrossFunction;
import eu.stratosphere.api.java.typeutils.ResultTypeQueryable;
import static eu.stratosphere.api.java.typeutils.TypeExtractor.getForObject;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.languagebinding.api.java.python.streaming.PythonStreamer;
import eu.stratosphere.languagebinding.api.java.streaming.Converter;
import eu.stratosphere.types.TypeInformation;
import java.io.IOException;

/**
 * Cross-function that uses an external python function.
 * @param <IN1>
 * @param <IN2>
 * @param <OUT>
 */
public class PythonCross<IN1, IN2, OUT> extends CrossFunction<IN1, IN2, OUT> implements ResultTypeQueryable {
	private final String scriptName;
	private PythonStreamer streamer;
	private Converter inConverter1;
	private Converter inConverter2;
	private Converter outConverter;
	private final Object info;

	public PythonCross(String scriptName, Object info) {
		this.info = info;
		this.scriptName = scriptName;
	}

	public PythonCross(String scriptName, Object info,
			Converter inConverter1, Converter inConverter2, Converter outConverter) {
		this(scriptName, info);
		this.inConverter1 = inConverter1;
		this.inConverter2 = inConverter2;
		this.outConverter = outConverter;
	}

	/**
	 Opens this function.
	 @param ignored ignored
	 @throws IOException 
	 */
	@Override
	public void open(Configuration ignored) throws IOException {
		streamer = inConverter1 == null && inConverter2 == null && outConverter == null
				? new PythonStreamer(this, scriptName)
				: new PythonStreamer(this, scriptName, inConverter1, inConverter2, outConverter);
		streamer.open();
	}

	/**
	 * Calls the external python function.
	 * @param first function input
	 * @param second function input
	 * @return OUT function output
	 * @throws IOException
	 */
	@Override
	public final OUT cross(IN1 first, IN2 second) throws Exception {
		try {
			return (OUT) streamer.streamWithGroups(first, second);
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

	@Override
	public TypeInformation getProducedType() {
		return getForObject(info);
	}
}
