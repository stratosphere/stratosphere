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
package eu.stratosphere.languagebinding.api.java.python;

import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.CsvInputFormat;
import eu.stratosphere.api.java.io.CsvOutputFormat;
import eu.stratosphere.api.java.io.PrintingOutputFormat;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.io.TextOutputFormat;
import eu.stratosphere.api.java.io.jdbc.JDBCInputFormat;
import eu.stratosphere.api.java.io.jdbc.JDBCOutputFormat;
import eu.stratosphere.api.java.operators.Grouping;
import eu.stratosphere.api.java.operators.SortedGrouping;
import eu.stratosphere.api.java.operators.UnsortedGrouping;
import eu.stratosphere.api.java.tuple.Tuple;
import static eu.stratosphere.api.java.typeutils.BasicTypeInfo.STRING_TYPE_INFO;
import static eu.stratosphere.api.java.typeutils.TypeExtractor.getForObject;
import eu.stratosphere.core.fs.FileSystem;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.languagebinding.api.java.proto.streaming.ProtoReceiver;
import eu.stratosphere.languagebinding.api.java.proto.streaming.ProtoSender;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonCoGroup;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonCross;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonFilter;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonFlatMap;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonGroupReduce;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonJoin;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonMap;
import eu.stratosphere.languagebinding.api.java.python.functions.PythonReduce;
import static eu.stratosphere.languagebinding.api.java.streaming.Receiver.createTuple;
import eu.stratosphere.languagebinding.api.java.streaming.StreamPrinter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 This class allows the execution of a stratosphere plan written in python.
 */
public class PythonExecutor {
	private static ProtoSender sender;
	private static ProtoReceiver receiver;
	private static Process process;
	private static HashMap<Integer, Object> sets;
	private static ExecutionEnvironment env;
	private static String stratospherePath;
	private static String tmpPackagePath;

	public static final byte BYTE = new Integer(1).byteValue();
	public static final int SHORT = new Integer(1).shortValue();
	public static final int INT = 1;
	public static final long LONG = 1L;
	public static final String STRING = "type";
	public static final double FLOAT = 1.5F;
	public static final double DOUBLE = 1.5D;
	public static final boolean BOOLEAN = true;

	public static final String STRATOSPHERE_PYTHON_HDFS_PATH = "hdfs:///tmp/stratosphere/stratosphere";
	public static final String STRATOSPHERE_GOOGLE_HDFS_PATH = "hdfs:///tmp/stratosphere/google";
	public static final String STRATOSPHERE_EXECUTOR_HDFS_PATH = "hdfs:///tmp/stratosphere/executor.py";
	public static final String STRATOSPHERE_HDFS_PATH = "hdfs:///tmp/stratospere";

	public static final String STRATOSPHERE_PYTHON_ID = "stratosphere";
	public static final String STRATOSPHERE_GOOGLE_ID = "google";
	public static final String STRATOSPHERE_EXECUTOR_ID = "executor";
	public static final String STRATOSPHERE_USER_ID = "userpackage";
	
	
	public static final String STRATOSPHERE_PYTHON_PATH_PREFIX = "/resources/python";
	public static final String STRATOSPHERE_PYTHON_PATH_SUFFIX = STRATOSPHERE_PYTHON_PATH_PREFIX + "/stratosphere";
	public static final String STRATOSPHERE_GOOGLE_PATH_SUFFIX = STRATOSPHERE_PYTHON_PATH_PREFIX + "/google";
	public static final String STRATOSPHERE_EXECUTOR_PATH_SUFFIX = STRATOSPHERE_PYTHON_PATH_PREFIX + "/executor.py";

	public static final String STRATOSPHERE_DIR = "STRATOSPHERE_ROOT_DIR";

	/**
	 Entry point for the execution of a python plan.
	 @param args 
	 [0] = local path to python script containing the plan
	 [1] = local path to user package
	 @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		try {
			if (args.length != 2) {
				throw new IllegalArgumentException("too few/many arguments. Expected 2, received " + args.length + ".");
			}
			env = ExecutionEnvironment.getExecutionEnvironment();
			distributePackages(args[1]);
			open(args[0]);
			receivePlan();
			env.execute();
			close();
		} catch (Exception ex) {
			try {
				close();
			} catch (NullPointerException npe) {
			}
			throw ex;
		}
	}

	private static void open(String planPath) throws IOException {
		sets = new HashMap();
		String executorPath = stratospherePath + STRATOSPHERE_EXECUTOR_PATH_SUFFIX;

		ProcessBuilder pb = new ProcessBuilder();
		pb.command("python", executorPath, planPath);
		process = pb.start();

		sender = new ProtoSender(null, process.getOutputStream());
		receiver = new ProtoReceiver(null, process.getInputStream());
		new StreamPrinter(process.getErrorStream()).start();
	}

	private static void distributePackages(String packagePath) throws IOException {
		PythonUtils.preparePythonEnvironment(env, packagePath);
	}

	private static void close() throws IOException, URISyntaxException {
		FileSystem hdfs = FileSystem.get(new URI(STRATOSPHERE_PYTHON_HDFS_PATH));
		hdfs.delete(new Path(STRATOSPHERE_HDFS_PATH), true);
		
		//delete temporary files
		if (!tmpPackagePath.endsWith("stratosphere")) {
			FileSystem local = FileSystem.get(new URI(tmpPackagePath));
			local.delete(new Path(tmpPackagePath), true);
		}
		sender.close();
		receiver.close();
		process.destroy();
	}

	//====Plan==========================================================================================================
	/**
	 General procedure:
	 Retrieve all operations that are executed on the environment, except input format operations. (for e.g DOP)
	 Retrieve all data sources.
	 For each data source:
	 >>recursion>>
	 receive all sinks
	 receive all operations
	 for every result due to operations:
	 GOTO recursion
	 */
	private static void receivePlan() throws IOException {
		receiveEnvironment();
		receiveSources();
		receiveSets();
	}

	private static void receiveEnvironment() throws IOException {
		receiveParameters();
		receiveCachedFiles();
	}

	private static void receiveSources() throws IOException {
		Object value;
		while ((value = receiver.receiveSpecialRecord()) != null) {
			int id = (Integer) value;
			String identifier = (String) receiver.receiveSpecialRecord();
			Tuple args = (Tuple) receiver.receiveSpecialRecord();
			switch (InputFormats.valueOf(identifier.toUpperCase())) {
				case CSV:
					createCsvSource(id, args);
					break;
				case JDBC:
					createJDBCSource(id, args);
					break;
				case TEXT:
					createTextSource(id, args);
					break;
			}
		}
	}

	private static void receiveSets() throws IOException {
		int c = (Integer) receiver.receiveSpecialRecord();
		for (int x = 0; x < c; x++) {
			receiveSinks();
			receiveOperations();
			receiveSets();
		}
	}

	private static void receiveSinks() throws IOException {
		Object value;
		while ((value = receiver.receiveSpecialRecord()) != null) {
			int parentID = (Integer) value;
			String identifier = (String) receiver.receiveSpecialRecord();
			Tuple args = (Tuple) receiver.receiveSpecialRecord();
			switch (OutputFormats.valueOf(identifier.toUpperCase())) {
				case CSV:
					createCsvSink(parentID, args);
					break;
				case JDBC:
					createJDBCSink(parentID, args);
					break;
				case PRINT:
					createPrintSink(parentID);
					break;
				case TEXT:
					createTextSink(parentID, args);
					break;
			}
		}
	}

	private static void receiveOperations() throws IOException {
		Object value;
		while ((value = receiver.receiveSpecialRecord()) != null) {
			String identifier = (String) value;
			int id = (Integer) receiver.receiveSpecialRecord();
			switch (Operations.valueOf(identifier.toUpperCase())) {
				case COGROUP:
					createCoGroupOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//otherID
							(Object) receiver.receiveSpecialRecord(),//types
							(Tuple) receiver.receiveSpecialRecord());//firstKeys-secondKeys-script
					break;
				case CROSS:
					createCrossOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//otherID
							(Object) receiver.receiveSpecialRecord(),//types
							(Tuple) receiver.receiveSpecialRecord());//script
					break;
				case CROSS_H:
					createCrossHugeOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//otherID
							(Object) receiver.receiveSpecialRecord(),//types
							(Tuple) receiver.receiveSpecialRecord());//script
					break;
				case CROSS_T:
					createCrossTinyOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//otherID
							(Object) receiver.receiveSpecialRecord(),//types
							(Tuple) receiver.receiveSpecialRecord());//script
					break;
				case FILTER:
					createFilterOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(String) receiver.receiveSpecialRecord());//script
					break;
				case FLATMAP:
					createFlatMapOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(String) receiver.receiveSpecialRecord(),//script
							(Object) receiver.receiveSpecialRecord());//types
					break;
				case GROUPREDUCE:
					createGroupReduceOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(String) receiver.receiveSpecialRecord(),//script
							(Object) receiver.receiveSpecialRecord());//types
					break;
				case JOIN:
					createJoinOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//otherID
							(Object) receiver.receiveSpecialRecord(),//types
							(Tuple) receiver.receiveSpecialRecord());//script
					break;
				case JOIN_H:
					createJoinHugeOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//otherID
							(Object) receiver.receiveSpecialRecord(),//types
							(Tuple) receiver.receiveSpecialRecord());//script
					break;
				case JOIN_T:
					createJoinTinyOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//otherID
							(Object) receiver.receiveSpecialRecord(),//types
							(Tuple) receiver.receiveSpecialRecord());//script
					break;
				case MAP:
					createMapOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(String) receiver.receiveSpecialRecord(),//script
							(Object) receiver.receiveSpecialRecord());//types
					break;
				case REDUCE:
					createReduceOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(String) receiver.receiveSpecialRecord());//script
					break;
				case GROUPBY:
					createGroupOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Tuple) receiver.receiveSpecialRecord());//fields
					break;
				case SORT:
					createSortOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord(),//field
							(Integer) receiver.receiveSpecialRecord());//order
					break;
				case UNION:
					createUnionOperation(
							id,
							(Integer) receiver.receiveSpecialRecord(),//childID
							(Integer) receiver.receiveSpecialRecord());//otherID
					break;
			}
		}
	}

	//====Environment===================================================================================================
	private static void receiveParameters() throws IOException {
		Object value;
		value = receiver.receiveSpecialRecord();
		if (value != null) {
			env.setDegreeOfParallelism((Integer) value);
		}
	}

	private static void receiveCachedFiles() throws IOException {
		Object value;
		while ((value = receiver.receiveSpecialRecord()) != null) {
			String name = (String) value;
			String path = (String) receiver.receiveSpecialRecord();
			env.registerCachedFile(path, name);
		}
	}

	//====Sources=======================================================================================================
	/**
	 This enum contains the identifiers for all supported InputFormats.
	 */
	private enum InputFormats {
		CSV,
		JDBC,
		TEXT
	}

	private static void createCsvSource(int id, Tuple args) {
		Tuple t = createTuple(args.getArity() - 1);
		Class[] classes = new Class[t.getArity()];
		for (int x = 0; x < args.getArity() - 1; x++) {
			t.setField(args.getField(x + 1), x);
			classes[x] = t.getField(x).getClass();
		}
		DataSet<Tuple> set = env.createInput(
				new CsvInputFormat(new Path((String) args.getField(0)), classes),
				getForObject(t));
		sets.put(id, set);
	}

	private static void createJDBCSource(int id, Tuple args) {
		DataSet<Tuple> set;
		if (args.getArity() == 3) {
			set = env.createInput(JDBCInputFormat.buildJDBCInputFormat()
					.setDrivername((String) args.getField(0))
					.setDBUrl((String) args.getField(1))
					.setQuery((String) args.getField(2))
					.finish());
			sets.put(id, set);
		}
		if (args.getArity() == 5) {
			set = env.createInput(JDBCInputFormat.buildJDBCInputFormat()
					.setDrivername((String) args.getField(0))
					.setDBUrl((String) args.getField(1))
					.setQuery((String) args.getField(2))
					.setUsername((String) args.getField(3))
					.setPassword((String) args.getField(4))
					.finish());
			sets.put(id, set);
		}
	}

	private static void createTextSource(int id, Tuple args) {
		Path path = new Path((String) args.getField(0));
		DataSet<String> set = env.createInput(new TextInputFormat(path), STRING_TYPE_INFO);
		sets.put(id, set);
	}

	//====Sinks=========================================================================================================
	/**
	 This enum contains the identifiers for all supported OutputFormats.
	 */
	private enum OutputFormats {
		CSV,
		JDBC,
		PRINT,
		TEXT
	}

	private static void createCsvSink(int id, Tuple args) {
		((DataSet) sets.get(id)).output(new CsvOutputFormat(new Path((String) args.getField(0))));
	}

	private static void createJDBCSink(int id, Tuple args) {
		switch (args.getArity()) {
			case 3:
				((DataSet) sets.get(id)).output(JDBCOutputFormat.buildJDBCOutputFormat()
						.setDrivername((String) args.getField(0))
						.setDBUrl((String) args.getField(1))
						.setQuery((String) args.getField(2))
						.finish());
				break;
			case 4:
				((DataSet) sets.get(id)).output(JDBCOutputFormat.buildJDBCOutputFormat()
						.setDrivername((String) args.getField(0))
						.setDBUrl((String) args.getField(1))
						.setQuery((String) args.getField(2))
						.setBatchInterval((Integer) args.getField(3))
						.finish());
				break;
			case 5:
				((DataSet) sets.get(id)).output(JDBCOutputFormat.buildJDBCOutputFormat()
						.setDrivername((String) args.getField(0))
						.setDBUrl((String) args.getField(1))
						.setQuery((String) args.getField(2))
						.setUsername((String) args.getField(3))
						.setPassword((String) args.getField(4))
						.finish());
				break;
			case 6:
				((DataSet) sets.get(id)).output(JDBCOutputFormat.buildJDBCOutputFormat()
						.setDrivername((String) args.getField(0))
						.setDBUrl((String) args.getField(1))
						.setQuery((String) args.getField(2))
						.setUsername((String) args.getField(3))
						.setPassword((String) args.getField(4))
						.setBatchInterval((Integer) args.getField(5))
						.finish());
				break;
		}
	}

	private static void createPrintSink(int id) {
		((DataSet) sets.get(id)).output(new PrintingOutputFormat());
	}

	private static void createTextSink(int id, Tuple args) {
		((DataSet) sets.get(id)).output(new TextOutputFormat(new Path((String) args.getField(0))));
	}

	//====Operations====================================================================================================
	/**
	 This enum contains the identifiers for all supported DataSet operations.
	 */
	private enum Operations {
		COGROUP,
		CROSS,
		CROSS_H,
		CROSS_T,
		FILTER,
		FLATMAP,
		GROUPBY,
		GROUPREDUCE,
		JOIN,
		JOIN_H,
		JOIN_T,
		MAP,
		REDUCE,
		SORT,
		UNION
		//project
		//aggregate
		//iterate
		//withParameters (cast to UdfOperator)
		//withBroadCastSet (cast to UdfOperator)
	}

	private static void createCoGroupOperation(int parentID, int childID, int otherID, Object types, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		int keyCount = (parameters.getArity() - 1) / 2;
		int[] firstKeys = new int[keyCount];
		int[] secondKeys = new int[keyCount];
		for (int x = 0; x < keyCount; x++) {
			firstKeys[x] = (Integer) parameters.getField(x);
			secondKeys[x] = (Integer) parameters.getField(x + keyCount);
		}
		String path = parameters.getField(parameters.getArity() - 1);
		sets.put(childID, op1.coGroup(op2).where(firstKeys).equalTo(secondKeys).with(new PythonCoGroup(path, types)));
	}

	private static void createCrossOperation(int parentID, int childID, int otherID, Object types, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		String path = (String) parameters.getField(0);
		sets.put(childID, op1.crossWithHuge(op2).with(new PythonCross(path, types)));
	}

	private static void createCrossHugeOperation(int parentID, int childID, int otherID,
			Object types, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		String path = (String) parameters.getField(0);
		sets.put(childID, op1.crossWithTiny(op2).with(new PythonCross(path, types)));
	}

	private static void createCrossTinyOperation(int parentID, int childID, int otherID,
			Object types, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		String path = (String) parameters.getField(0);
		sets.put(childID, op1.cross(op2).with(new PythonCross(path, types)));
	}

	private static void createFilterOperation(int parentID, int childID, String path) {
		DataSet op1 = (DataSet) sets.get(parentID);
		sets.put(childID, op1.filter(new PythonFilter(path)));
	}

	private static void createFlatMapOperation(int parentID, int childID, String path, Object types) {
		DataSet op1 = (DataSet) sets.get(parentID);
		sets.put(childID, op1.flatMap(new PythonFlatMap(path, types)));
	}

	private static void createGroupOperation(int parentID, int childID, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		int[] fields = new int[parameters.getArity()];
		for (int x = 0; x < fields.length; x++) {
			fields[x] = (Integer) parameters.getField(x);
		}
		sets.put(childID, op1.groupBy(fields));
	}

	private static void createGroupReduceOperation(int parentID, int childID, String path, Object types) {
		Object op1 = sets.get(parentID);
		if (op1 instanceof DataSet) {
			sets.put(childID, ((DataSet) op1).reduceGroup(new PythonGroupReduce(path, types)));
			return;
		}
		if (op1 instanceof UnsortedGrouping) {
			sets.put(childID, ((UnsortedGrouping) op1).reduceGroup(new PythonGroupReduce(path, types)));
			return;
		}
		if (op1 instanceof SortedGrouping) {
			sets.put(childID, ((SortedGrouping) op1).reduceGroup(new PythonGroupReduce(path, types)));
		}
	}

	private static void createJoinOperation(int parentID, int childID, int otherID, Object types, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		int keyCount = (parameters.getArity() - 1) / 2;
		int[] firstKeys = new int[keyCount];
		int[] secondKeys = new int[keyCount];
		for (int x = 0; x < keyCount; x++) {
			firstKeys[x] = (Integer) parameters.getField(x);
			secondKeys[x] = (Integer) parameters.getField(x + keyCount);
		}
		String path = parameters.getField(parameters.getArity() - 1);
		sets.put(childID, op1.join(op2).where(firstKeys).equalTo(secondKeys).with(new PythonJoin(path, types)));
	}

	private static void createJoinHugeOperation(int parentID, int childID, int otherID,
			Object types, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		int keyCount = (parameters.getArity() - 1) / 2;
		int[] firstKeys = new int[keyCount];
		int[] secondKeys = new int[keyCount];
		for (int x = 0; x < keyCount; x++) {
			firstKeys[x] = (Integer) parameters.getField(x);
			secondKeys[x] = (Integer) parameters.getField(x + keyCount);
		}
		String path = parameters.getField(parameters.getArity() - 1);
		sets.put(childID, op1.joinWithHuge(op2).where(firstKeys).equalTo(secondKeys).with(new PythonJoin(path, types)));
	}

	private static void createJoinTinyOperation(int parentID, int childID, int otherID,
			Object types, Tuple parameters) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		int keyCount = (parameters.getArity() - 1) / 2;
		int[] firstKeys = new int[keyCount];
		int[] secondKeys = new int[keyCount];
		for (int x = 0; x < keyCount; x++) {
			firstKeys[x] = (Integer) parameters.getField(x);
			secondKeys[x] = (Integer) parameters.getField(x + keyCount);
		}
		String path = (String) parameters.getField(parameters.getArity() - 1);
		sets.put(childID, op1.joinWithTiny(op2).where(firstKeys).equalTo(secondKeys).with(new PythonJoin(path, types)));
	}

	private static void createMapOperation(int parentID, int childID, String path, Object types) {
		DataSet op1 = (DataSet) sets.get(parentID);
		sets.put(childID, op1.map(new PythonMap(path, types)));
	}

	private static void createReduceOperation(int parentID, int childID, String path) {
		Object op1 = (DataSet) sets.get(parentID);
		if (op1 instanceof DataSet) {
			sets.put(childID, ((DataSet) op1).reduce(new PythonReduce(path)));
			return;
		}
		if (op1 instanceof UnsortedGrouping) {
			sets.put(childID, ((UnsortedGrouping) op1).reduce(new PythonReduce(path)));
		}
	}

	private static void createSortOperation(int parentID, int childID, int field, int order) {
		Grouping op1 = (Grouping) sets.get(parentID);
		Order o;
		switch (order) {
			case 0:
				o = Order.NONE;
				break;
			case 1:
				o = Order.ASCENDING;
				break;
			case 2:
				o = Order.DESCENDING;
				break;
			case 3:
				o = Order.ANY;
				break;
			default:
				o = Order.NONE;
				break;
		}
		if (op1 instanceof UnsortedGrouping) {
			sets.put(childID, ((UnsortedGrouping) op1).sortGroup(field, o));
			return;
		}
		if (op1 instanceof SortedGrouping) {
			sets.put(childID, ((SortedGrouping) op1).sortGroup(field, o));
		}
	}

	private static void createUnionOperation(int parentID, int childID, int otherID) {
		DataSet op1 = (DataSet) sets.get(parentID);
		DataSet op2 = (DataSet) sets.get(otherID);
		sets.put(childID, op1.union(op2));
	}

	//====Utilities=====================================================================================================
	public static Class convertTypeInfo(Object types) {
		return types.getClass();
	}
}
