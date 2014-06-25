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
package eu.stratosphere.languagebinding.api.java.python.streaming;

import eu.stratosphere.api.common.functions.AbstractFunction;
import eu.stratosphere.languagebinding.api.java.proto.streaming.ProtoReceiver;
import eu.stratosphere.languagebinding.api.java.proto.streaming.ProtoSender;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_EXECUTOR_ID;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_GOOGLE_ID;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_PYTHON_ID;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_USER_ID;
import eu.stratosphere.languagebinding.api.java.streaming.Converter;
import eu.stratosphere.languagebinding.api.java.streaming.StreamPrinter;
import eu.stratosphere.languagebinding.api.java.streaming.Streamer;
import java.io.IOException;

/**
 * This streamer is used by functions with two input types to send/receive data to/from an external python process.
 *
 * Type(flag) conversion table (Java -> PB -> Python)
 * bool	  -> bool         -> bool
 * byte	  -> int32(byte)  -> int
 * short  -> int32(short) -> int
 * int	  -> int32        -> int
 * long   -> int64        -> long
 * float  -> float        -> float
 * double -> double       -> floatt
 * string -> string       -> string
 *   ?    -> string       -> string
 *
 * Type(flag) conversion table (Python -> PB -> Java)
 * bool   -> bool   -> bool
 * int    -> int32  -> int
 * long   -> int64  -> long
 * float  -> float  -> float
 * float  -> float  -> float
 * string -> string -> string
 *   ?    -> string -> string
 */
public class PythonStreamer extends Streamer {
	private final String scriptPathSuffix;
	private AbstractFunction function;
	private Process process;
	private Converter inConverter1;
	private Converter inConverter2;
	private Converter outConverter;

	public PythonStreamer(AbstractFunction function, String scriptPathSuffix) {
		this.function = function;
		this.scriptPathSuffix = scriptPathSuffix;
	}

	public PythonStreamer(AbstractFunction function, String scriptPathSuffix,
			Converter inConverter1,
			Converter outConverter) {
		this(function, scriptPathSuffix);
		this.inConverter1 = inConverter1;
		this.outConverter = outConverter;
	}

	public PythonStreamer(AbstractFunction function, String scriptPathSuffix,
			Converter inConverter1,
			Converter inConverter2,
			Converter outConverter) {
		this(function, scriptPathSuffix);
		this.inConverter1 = inConverter1;
		this.inConverter2 = inConverter2;
		this.outConverter = outConverter;
	}

	/**
	 * Opens this streamer and starts the python script.
	 * @throws IOException 
	 */
	@Override
	public void open() throws IOException {
		ProcessBuilder pb = new ProcessBuilder();

		String userPackagePath = function.getRuntimeContext().getDistributedCache()
				.getFile(STRATOSPHERE_USER_ID).getAbsolutePath();
		function.getRuntimeContext().getDistributedCache()
				.getFile(STRATOSPHERE_PYTHON_ID).getAbsolutePath();
		function.getRuntimeContext().getDistributedCache()
				.getFile(STRATOSPHERE_GOOGLE_ID).getAbsolutePath();
		String executorPath = function.getRuntimeContext().getDistributedCache()
				.getFile(STRATOSPHERE_EXECUTOR_ID).getAbsolutePath();

		String scriptPath = userPackagePath + scriptPathSuffix;

		pb.command("python", executorPath, scriptPath);
		process = pb.start();
		sender = inConverter1 == null
				? new ProtoSender(function, process.getOutputStream())
				: new ProtoSender(function, process.getOutputStream(), new Converter[]{inConverter1, inConverter2});
		receiver = outConverter == null
				? new ProtoReceiver(function, process.getInputStream())
				: new ProtoReceiver(function, process.getInputStream(), outConverter);
		new StreamPrinter(process.getErrorStream()).start();
	}

	@Override
	public void close() throws IOException {
		super.close();
		process.destroy();
	}
}
