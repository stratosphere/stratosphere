/***********************************************************************************************************************
 *
 * Copyright (C) 2012 by the Stratosphere project (http://stratosphere.eu)
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
package eu.stratosphere.pact.common.stubs;

import java.util.HashMap;

import eu.stratosphere.pact.common.stubs.accumulables.Accumulable;
import eu.stratosphere.pact.common.stubs.accumulables.Accumulator;
import eu.stratosphere.pact.common.stubs.accumulables.DoubleCounter;
import eu.stratosphere.pact.common.stubs.accumulables.Histogram;
import eu.stratosphere.pact.common.stubs.accumulables.IntCounter;
import eu.stratosphere.pact.common.stubs.accumulables.LongCounter;

/**
 *
 */
public interface RuntimeContext {

	String getTaskName();

	int getNumberOfParallelSubtasks();

	int getIndexOfThisSubtask();

	/**
	 * For system internal usage only. Use getAccumulator(...) to obtain a
	 * accumulator. TODO hide this from UDF?!
	 */
	// HashMap<String, Accumulator<?>> getAllAccumulators();

	/**
	 * For system internal usage only. Use getAccumulator(...) to obtain a
	 * accumulable.
	 */
	HashMap<String, Accumulable<?, ?>> getAllAccumulables();

	/**
	 * Convenience function to create a counter object for integers. Internally
	 * this creates an accumulator object for double values.
	 * 
	 * @param name
	 * @return
	 */
	IntCounter getIntCounter(String name);

	LongCounter getLongCounter(String name);

	DoubleCounter getDoubleCounter(String name);

	/**
	 * Convenience function to create a accumulable histogram
	 */
	Histogram getHistogram(String name);

	// <T extends Value> Aggregator<T> getAggregator(String name, Class<? extends
	// Aggregator<T>> aggregatorClass);

	/**
	 * Note: There is no need to register accumulable objects - they will be
	 * created when a UDF asks the first time for a counter that does not exist
	 * yet locally. This implies that there can be conflicts when a counter is
	 * requested with the same name but with different types, either in the same
	 * UDF or in different. In the last case the conflict occures during merging.
	 * 
	 * TODO Create a version that checks if the counter is really new or already internally used (newOnly)
	 * 
	 * @param name
	 * @param accumulator
	 * @return
	 */
	<T> Accumulator<T> getAccumulator(String name, Class<? extends Accumulator<T>> accumulatorClass);

	<V, A> Accumulable<V, A> getAccumulable(String name, Class<? extends Accumulable<V, A>> accumulatorClass);

}
