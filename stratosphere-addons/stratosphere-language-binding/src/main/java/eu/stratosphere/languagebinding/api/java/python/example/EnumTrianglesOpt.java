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

import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.api.java.tuple.Tuple4;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.INT;
import eu.stratosphere.languagebinding.api.java.python.PythonUtils;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonFlatMap;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonGroupReduce;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonJoin;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonMap;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonReduce;

/**
 * Python Implementation of eu.stratosphere.example.java.triangles.EnumTrianglesOpt
 */
public class EnumTrianglesOpt {
	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		
		PythonUtils.preparePythonEnvironment(
				env,
				"src/main/python/eu/stratosphere/languagebinding/api/python/stratosphere");
		
		DataSet<Tuple2> edges = env.fromElements(new Tuple2(1, 2),
				new Tuple2(1, 3),
				new Tuple2(1, 4),
				new Tuple2(1, 5),
				new Tuple2(2, 3),
				new Tuple2(2, 5),
				new Tuple2(3, 4),
				new Tuple2(3, 7),
				new Tuple2(3, 8),
				new Tuple2(5, 6),
				new Tuple2(7, 8));

		DataSet<Tuple4> edgesWithDegrees = edges
				.flatMap(new PythonFlatMap(
								"/example/EdgeDuplicator.py",
								new Tuple2(INT, INT)))
				.groupBy(0)
				.sortGroup(1, Order.ASCENDING)
				.reduceGroup(new PythonGroupReduce(
								"/example/DegreeCounter.py",
								new Tuple4(INT, INT, INT, INT)))
				.groupBy(0, 2)
				.reduce(new PythonReduce(
								"/example/DegreeJoiner.py"));

		// project edges by degrees
		DataSet<Tuple2> edgesByDegree = edgesWithDegrees
				.map(new PythonMap(
								"/example/EdgeByDegreeProjector.py",
								new Tuple2(INT, INT)));
		// project edges by vertex id
		DataSet<Tuple2> edgesById = edgesByDegree
				.map(new PythonMap(
								"/example/EdgeByIdProjector.py",
								new Tuple2(INT, INT)));

		// build and filter triads
		DataSet<Tuple3> triangles = edgesByDegree
				.groupBy(0)
				.sortGroup(1, Order.ASCENDING)
				.reduceGroup(new PythonGroupReduce(
								"/example/TriadBuilder.py",
								new Tuple3(INT, INT, INT)))
				.join(edgesById)
				.where(1, 2)
				.equalTo(0, 1)
				.with(new PythonJoin(
								"/example/TriadFilter.py",
								new Tuple3(INT, INT, INT)));
		triangles.print();
		
		env.execute();
	}
}
