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

import eu.stratosphere.api.common.operators.Operator;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.api.java.operators.translation.PlanMapOperator;
import eu.stratosphere.api.java.operators.translation.PlanReduceOperator;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;

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
	protected Operator translateToDataFlow(Operator input) {
		PlanMapOperator<IN, Long> countMapOperator = new PlanMapOperator<IN, Long>(
				new CountingMapUdf<IN>(), "Count: map to ones", getInputType(), BasicTypeInfo.LONG_TYPE_INFO);
		countMapOperator.setInput(input);
		countMapOperator.setDegreeOfParallelism(input.getDegreeOfParallelism());

		PlanReduceOperator<Long> countReduceOperator = new PlanReduceOperator<Long>(
				new CountingReduceUdf(), new int[0], "Count: sum up ones", BasicTypeInfo.LONG_TYPE_INFO);
		countReduceOperator.setInput(countMapOperator);
		countReduceOperator.setDegreeOfParallelism(input.getDegreeOfParallelism());

		return countReduceOperator;
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
