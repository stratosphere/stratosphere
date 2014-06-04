/***********************************************************************************************************************
 *
 * Copyright (C) 2010-2014 by the Stratosphere project (http://stratosphere.eu)
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
package eu.stratosphere.api.java.operators;

import eu.stratosphere.api.common.functions.GenericMap;
import eu.stratosphere.api.common.functions.GenericReduce;
import eu.stratosphere.api.common.operators.Operator;
import eu.stratosphere.api.common.operators.OperatorInformation;
import eu.stratosphere.api.common.operators.UnaryOperatorInformation;
import eu.stratosphere.api.common.operators.Union;
import eu.stratosphere.api.common.operators.base.GenericDataSourceBase;
import eu.stratosphere.api.common.operators.base.MapOperatorBase;
import eu.stratosphere.api.common.operators.base.ReduceOperatorBase;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.api.java.io.CollectionInputFormat;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;

import java.util.Arrays;

/**
 * The CountOperator will be translated to a map and reduce function.
 * <p/>
 * The map function will map every input element to a 1 and the following reduce will sum up all ones.
 *
 * @param <IN> The type of the data set filtered by the operator.
 */
public class CountOperator<IN> extends SingleInputUdfOperator<IN, Long, CountOperator<IN>> {

	public CountOperator(DataSet<IN> input) {
		super(input, BasicTypeInfo.LONG_TYPE_INFO);
	}

	@Override
	protected ReduceOperatorBase<Long, GenericReduce<Long>> translateToDataFlow(Operator input) {
		MapOperatorBase<IN, Long, GenericMap<IN, Long>> mapToOnes =
				new MapOperatorBase<IN, Long, GenericMap<IN, Long>>(
						new CountingMapUdf<IN>(),
						new UnaryOperatorInformation<IN, Long>(getInputType(), BasicTypeInfo.LONG_TYPE_INFO),
						"Count: map to ones");
		mapToOnes.setInput(input);
		mapToOnes.setDegreeOfParallelism(input.getDegreeOfParallelism());

		// dummy source with a single element (0L), which is needed for empty
		// inputs (count 0) to ensure at least a single element for the reducer
		GenericDataSourceBase<Long, CollectionInputFormat<Long>> dummySource = new GenericDataSourceBase(
				new CollectionInputFormat<Long>(Arrays.asList(new Long[]{0L})),
				new OperatorInformation<Long>(BasicTypeInfo.LONG_TYPE_INFO),
				"Count: dummy source (for 0 count)");

		Union<Long> unionMapToOnesAndZero = new Union<Long>(mapToOnes, dummySource);

		ReduceOperatorBase<Long, GenericReduce<Long>> sumOnes =
				new ReduceOperatorBase<Long, GenericReduce<Long>>(
						new CountingReduceUdf(),
						new UnaryOperatorInformation<Long, Long>(BasicTypeInfo.LONG_TYPE_INFO, BasicTypeInfo.LONG_TYPE_INFO),
						new int[0],
						"Count: sum up ones");
		sumOnes.setInput(unionMapToOnesAndZero);
		sumOnes.setDegreeOfParallelism(1);

		return sumOnes;
	}

	// -----------------------------------------------------------------------------------------------------------------

	public static class CountingMapUdf<IN> extends MapFunction<IN, Long> {

		private static final long serialVersionUID = 1L;

		@Override
		public Long map(IN value) throws Exception {
			return 1L;
		}
	}

	public static class CountingReduceUdf extends ReduceFunction<Long> {

		private static final long serialVersionUID = 1L;

		@Override
		public Long reduce(Long value1, Long value2) throws Exception {
			return value1 + value2;
		}
	}
}
