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
package eu.stratosphere.languagebinding.api.java.python.example;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.tuple.Tuple10;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.languagebinding.api.java.python.PythonUtils;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonGroupReduce;

/**
 KMeans-Algorithm that uses a third-party library.
 */
public class ExternalAlgorithm {
	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		
		PythonUtils.preparePythonEnvironment(
				env,
				"src/main/python/eu/stratosphere/languagebinding/api/python/stratosphere");
		
		DataSet<Tuple4> data = env.fromElements(
				new Tuple4(5.1, 3.5, 1.4, 0.2),
				new Tuple4(4.9, 3.0, 1.4, 0.2),
				new Tuple4(4.7, 3.2, 1.3, 0.2),
				new Tuple4(4.6, 3.1, 1.5, 0.2),
				new Tuple4(5.0, 3.6, 1.4, 0.2),
				new Tuple4(5.4, 3.9, 1.7, 0.4),
				new Tuple4(4.6, 3.4, 1.4, 0.3),
				new Tuple4(5.0, 3.4, 1.5, 0.2),
				new Tuple4(4.4, 2.9, 1.4, 0.2),
				new Tuple4(4.9, 3.1, 1.5, 0.1));
		
		data
				.reduceGroup(
						new PythonGroupReduce("/example/ExternalKMeans.py", new Tuple10(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)))
				.print();
		
		env.execute();
	}
}
