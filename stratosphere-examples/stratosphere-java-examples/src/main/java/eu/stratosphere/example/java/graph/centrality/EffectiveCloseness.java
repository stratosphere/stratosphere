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

package eu.stratosphere.example.java.graph.centrality;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import com.google.common.base.Preconditions;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.DeltaIteration;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.api.java.functions.JoinFunction;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.tuple.Tuple1;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.core.fs.FileSystem.WriteMode;
import eu.stratosphere.util.Collector;

/**
Name : Effective Closeness <br>
Description : Finds closeness of a node with respect to all other nodes <br>
Reference : Centralities in Large Networks: Algorithms and Observations-U Kang
<p>
Closeness Definition: Inverse of the average of shortest
					  distances to all other nodes.
Implemented by using delta-iteration operator in stratosphere
and the corresponding data-flow follows three parts, <br>
</p>
<ul>
<li>Reads input files vertices <vertexId> and edges
<srcVertexId, targetVertexId> <br>

<li>Computes the summation part (of average computation based on
 "Effective Closeness algorithm" and Flajolet-Martin) for each vertex
 iteratively and the final output format is  <vertexId, bit[], sum> <br>

<li>Computes inverse of the average ((n-1)/sum) for each vertex using
the summation from the second step and the final output format
is <vertexId, closeness> <br>
</ul>
@Parameters [Degree of parallelism],[vertices input-path],[edge
input-path], [out-put],[Max-Num of iterations],[Number of vertices]
 */
public class EffectiveCloseness {
	
	private static final String ZERO = "0";
	private static final String TAB_DELIM = "\t" ;
	private static final String NEWLINE = "\n" ;
	private static final String COMMA_DELIM = "," ;
	static final String COMMA_STR = "comma" ;

	public static void main(String[] args) throws Exception {
		
		String fieldDelimiter = TAB_DELIM;
		char delim = '\t';
		int keyPosition = 0;
		
		if (args.length < 6) {
			System.err.println("Usage:[Degree of parallelism],[vertices input-path],"
					+ "[edge input-path],[out-put],[Max-Num of iterations],[Number of vertices]"
					+ ",[Delimiter]");
			return;
		}

		final int numSubTasks = (args.length > 0 ? Integer.parseInt(args[0]): 1);
		final String verticesInput = (args.length > 1 ? args[1] : "");
		final String edgeInput = (args.length > 2 ? args[2] : "");
		final String output = (args.length > 3 ? args[3] : "");
		final int maxIterations = (args.length > 4 ? Integer.parseInt(args[4]): 5);
		final String numVertices = (args.length > 5 ? (args[5]) : ZERO);
		
		if(args.length>6){
			fieldDelimiter = (args[6]);
		}
	
	
		if(COMMA_DELIM.equalsIgnoreCase(fieldDelimiter) ||
			COMMA_STR.equalsIgnoreCase(fieldDelimiter)){
			delim = ',';
		}

		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		env.setDegreeOfParallelism(numSubTasks);
		/**
	 	=Part1:=
	 	Reads input file that contains list of vertices <vertexId> and
	 	emits <vertexId,Bit[],initial_sum(0))> Note: VertexWithBitArray -
	 	custom data type to hold both the vertexId and its corresponding
	 	bitarray emit work-set and solution-set as <vertexId, bit, sum>*/

		DataSet<Tuple3<Long, VertexWithBitArray, Double>> initialSolutionSet = env
				.readCsvFile(verticesInput).lineDelimiter(NEWLINE)
				.types(Long.class).map(new AssignBitArrayToVertices())
				.name("Assign BitArray To Each Vertex");
		/**
		 * . The initial vertices input file is set as both work-set and
		 * solution-set
		 */
		DataSet<Tuple3<Long, VertexWithBitArray, Double>> initialworkingSet = initialSolutionSet;

		/**
		 * Reads Edges input file of the format <sourceId, destinationId>
		 */
		DataSet<Tuple2<Long, Long>> edgeSet = env.readCsvFile(edgeInput).fieldDelimiter(delim).types(Long.class, Long.class);

	
		/**
		 * =Part2:=
		 * Initializing delta-iteration
		 */
		DeltaIteration<Tuple3<Long, VertexWithBitArray, Double>,
		Tuple3<Long, VertexWithBitArray, Double>> iteration = initialSolutionSet.iterateDelta(initialworkingSet, maxIterations, keyPosition);
		//.name("Effective Closeness Iteration");

		/**
		 * Join <src,des> of edges with <vertexId, bit, sum> workingset and emit
		 * <des, bit, sum> groupby des and computes count of all neighbors of that des node by
		 * using merge method {no need to merge current vertex's node in the sum} and emit <des, updatedbit, updatedsum>
		 * join solutionset <vertexId,bit,sum> with previous step on (vertexId
		 * and des ) compare solution set's sum with the udpatedsum **/
		
		DataSet<Tuple3<Long, VertexWithBitArray, Double>> worksetRes = iteration
				.getWorkset().join(edgeSet).where(0).equalTo(0)
				.with(new SendingMessageToNeighbors()).groupBy(0)
				.reduceGroup(new PartialBitwiseOR())
				.join(iteration.getSolutionSet()).where(0).equalTo(0)
				.flatMap(new FilterMapper());//.name("Filters Converged Vertices");
		/**
		 * Termination condition for the iteration :- when the work-set of
		 * previous iteration and current iteration are same
		 */

		DataSet<Tuple3<Long, VertexWithBitArray, Double>> finalSolnSet = iteration
					.closeWith(worksetRes, worksetRes);
		/**
		 * =Part3:=
		 * Computation of average with a single map which reads the
		 * output<vertexId,sum> of the iteration
		 */
		DataSet<Tuple2<Long, Double>> closeness = finalSolnSet
			.map(new AverageComputation(numVertices)).name("Average Computation");
		/**
		 * Data Sink: Writing results back to disk
		 */
		closeness.writeAsCsv(output, NEWLINE, TAB_DELIM, WriteMode.OVERWRITE);
		env.execute();
	}

	public static final class AverageComputation extends
		MapFunction<Tuple3<Long, VertexWithBitArray, Double>,
		Tuple2<Long, Double>> {
		private static final long serialVersionUID = 1L;
		Long noOfver;
		public AverageComputation(String numVer) {
			this.noOfver = Long.parseLong(numVer);
		}
		@Override
		public Tuple2<Long, Double> map(
			Tuple3<Long, VertexWithBitArray, Double> value)
			throws Exception {
			Double closeness = 0.0;
			if (value.f2.doubleValue() > 0) {
			closeness = (noOfver - 1) / value.f2.doubleValue();
			}
			Tuple2<Long, Double> emitcloseness = new Tuple2<Long, Double>();
			emitcloseness.f0 = value.f0;
			emitcloseness.f1 = closeness;
			return emitcloseness;
		}

	}

	public static final class AssignBitArrayToVertices extends
		MapFunction<Tuple1<Long>, Tuple3<Long, VertexWithBitArray, Double>> {
		private static final long serialVersionUID = 1L;

		@Override
		public Tuple3<Long, VertexWithBitArray, Double> map(Tuple1<Long> value)
			throws Exception {
			FMCounter counter = new FMCounter();
			counter.addNode(value.f0.intValue());
			Tuple3<Long, VertexWithBitArray, Double> emitTuple = new Tuple3<Long,
					VertexWithBitArray, Double>();
			emitTuple.f0 = value.f0;
			emitTuple.f1 = new VertexWithBitArray(value.f0, counter);
			emitTuple.f2 = 0.0;
			return emitTuple;
		}
	}

	public static final class SendingMessageToNeighbors
		extends
		JoinFunction<Tuple3<Long, VertexWithBitArray, Double>,
		Tuple2<Long, Long>, Tuple3<Long, VertexWithBitArray, Double>> {
		private static final long serialVersionUID = 1L;
	
		@Override
		public Tuple3<Long, VertexWithBitArray, Double> join(
			Tuple3<Long, VertexWithBitArray, Double> vertex_workset,
			Tuple2<Long, Long> neighbors) throws Exception {
			Tuple3<Long, VertexWithBitArray, Double> output = new Tuple3<Long,
					VertexWithBitArray, Double>();
			output.f0 = (neighbors.f1.longValue());
			output.f1 = vertex_workset.f1;
			output.f2 = vertex_workset.f2;
			return output;
		}

	}

	public static final class PartialBitwiseOR
		extends
		GroupReduceFunction<Tuple3<Long, VertexWithBitArray, Double>,
		Tuple3<Long, VertexWithBitArray, Double>> {
		private static final long serialVersionUID = 1L;
		@Override
		public void reduce(
			Iterator<Tuple3<Long, VertexWithBitArray, Double>> values,
			Collector<Tuple3<Long, VertexWithBitArray, Double>> out)
			throws Exception {
			boolean flag = false;
			FMCounter firstInList = null;
			Long currentVertex = null;
			Double sum = 0.0;
			while (values.hasNext()) {
				Tuple3<Long, VertexWithBitArray, Double> tuple = values.next();
				if (!flag) {
					firstInList = tuple.f1.getFmCounter().copy();
					currentVertex = tuple.f0;
					sum = tuple.f2;
					flag = true;
				}
				firstInList.merge(tuple.f1.getFmCounter()); // BITWISE OR of current node with initial record in the iterator
			}
			Tuple3<Long, VertexWithBitArray, Double> result = new Tuple3<Long,
					VertexWithBitArray, Double>();
			result.f0 = (currentVertex);
			result.f1 = (new VertexWithBitArray(currentVertex, firstInList));
			result.f2 = sum;
			out.collect(result);
		}
	}

	public static final class FilterMapper
		extends
		FlatMapFunction<Tuple2<Tuple3<Long, VertexWithBitArray, Double>,
		Tuple3<Long, VertexWithBitArray, Double>>, Tuple3<Long,
		VertexWithBitArray, Double>> {
		private static final long serialVersionUID = 1L;

		@Override
		public void flatMap(
			Tuple2<Tuple3<Long, VertexWithBitArray, Double>, Tuple3<Long,
			VertexWithBitArray, Double>> value,
			Collector<Tuple3<Long, VertexWithBitArray, Double>> out)
			throws Exception {
			Tuple3<Long, VertexWithBitArray, Double> currentIterval = value.f0;
			Tuple3<Long, VertexWithBitArray, Double> previousIterval = value.f1;
	
			int iterationNumber = getIterationRuntimeContext()
				.getSuperstepNumber();
	
			int NNOfNode_before = previousIterval.f1.getFmCounter().getCount();
			currentIterval.f1.getFmCounter().merge(
				previousIterval.f1.getFmCounter());
			int NNofNode_After = currentIterval.f1.getFmCounter().getCount();
	
			Double sum = previousIterval.f2.doubleValue();
			int diff = NNofNode_After - NNOfNode_before;
	
			sum = sum + iterationNumber * (diff);
			currentIterval.f2 = sum.doubleValue();
	
			if (currentIterval.f2.doubleValue() != previousIterval.f2
				.doubleValue()) {
				out.collect(currentIterval);
			}
		}
	}

	/**
	 *
	 * Custom Data type - Holder for vertex and its bitarray
	 *
	 */
	public static class VertexWithBitArray implements Serializable{
		private static final long serialVersionUID = 1L;
		private Long vertexId;
		public Long getVertexId() {
			return vertexId;
		}
		public FMCounter getFmCounter() {
			return fmCounter;
		}
		private FMCounter fmCounter;
		public VertexWithBitArray() {
		}
		public VertexWithBitArray(Long vertexId, FMCounter counter) {
			this.vertexId = vertexId;
			this.fmCounter = counter;
		}
	}
}


class FMCounter {
	public final static int NUM_BUCKETS = 32;
	private final static double MAGIC_CONSTANT = 0.77351;
	private int[] buckets;

	/**
	 * Create a zero-bucket FM-Sketch. This is needed because Giraph requires a
	 * no-argument constructor.
	 */
	public FMCounter() {
		this.buckets = new int[NUM_BUCKETS];
	}

	/**
	 * Create a copy of the FM-Sketch by copying the internal integer array.
	 */
	public FMCounter copy() {
		FMCounter result = new FMCounter();
		result.buckets = Arrays.copyOf(this.buckets, this.buckets.length);
		return result;
	}

	/**
	 * Count the passed in node id.
	 *
	 * @param n
	 */
	public void addNode(int n) {
		for (int i = 0; i < buckets.length; i++) {
			int hash = hash(n, i);
			buckets[i] |= (1 << Integer.numberOfTrailingZeros(hash));
		}
	}

	/**
	 * Return the estimate for the number of unique ids.
	 *
	 */
	public int getCount() {
		int S = 0;
		int R = 0;
		int bucket = 0;
		int[] sorted = new int[buckets.length];
		for (int i = 0; i < buckets.length; ++i) {
			R = 0;
			bucket = buckets[i];
			while ((bucket & 1) == 1 && R < Integer.SIZE) {
				++R;
				bucket >>= 1;
			}
			sorted[i] = R;
		}
		Arrays.sort(sorted);
		int start = (int) (0.25 * buckets.length);
		int end = (int) (0.75 * buckets.length);
		int size = end - start;
		for (int i = start; i < end; i++) {
			S += sorted[i];
		}
		int count = (int) (Math.pow(2.0, (double) S
				/(double) size) / MAGIC_CONSTANT);
		return count;
	}

	/**
	 * Merge this FM-Sketch with the other one.
	 */
	public void merge(FMCounter other) {
		Preconditions.checkArgument(other instanceof FMCounter,
			"Other is not a FMCounterWritable.");
		FMCounter otherB = (FMCounter) other;
		Preconditions.checkState(this.buckets.length == otherB.buckets.length,
			"Number of buckets does not match.");
		for (int i = 0; i < buckets.length; ++i) {
			buckets[i] |= otherB.buckets[i];
		}
	}

	/**
	 * hash function use for the FM sketch
	 *
	 * @param code
	 *		value (node id) to hash
	 * @param level
	 *		to create different hashes for the same value
	 * @return hash value (31 significant bits)
	 */
	private int hash(int code, int level) {
		final int rotation = level * 11;
		code = (code << rotation) | (code >>> -rotation);
		code = (code + 0x7ed55d16) + (code << 12);
		code = (code ^ 0xc761c23c) ^ (code >>> 19);
		code = (code + 0x165667b1) + (code << 5);
		code = (code + 0xd3a2646c) ^ (code << 9);
		code = (code + 0xfd7046c5) + (code << 3);
		code = (code ^ 0xb55a4f09) ^ (code >>> 16);
		return code >= 0 ? code : -(code + 1);
	}

	/**
	 * Return the number of buckets.
	 */
	public int getNumBuckets() {
		return buckets.length;
	}

}
