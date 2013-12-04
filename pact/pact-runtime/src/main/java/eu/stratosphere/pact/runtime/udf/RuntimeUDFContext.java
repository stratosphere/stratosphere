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
package eu.stratosphere.pact.runtime.udf;

import java.util.HashMap;

import eu.stratosphere.pact.common.stubs.RuntimeContext;
import eu.stratosphere.pact.common.stubs.accumulables.Accumulable;
import eu.stratosphere.pact.common.stubs.accumulables.Accumulator;
import eu.stratosphere.pact.common.stubs.accumulables.AccumulatorHelper;
import eu.stratosphere.pact.common.stubs.accumulables.DoubleCounter;
import eu.stratosphere.pact.common.stubs.accumulables.Histogram;
import eu.stratosphere.pact.common.stubs.accumulables.IntCounter;
import eu.stratosphere.pact.common.stubs.accumulables.LongCounter;
import eu.stratosphere.pact.common.stubs.aggregators.Aggregator;
import eu.stratosphere.pact.common.type.Value;

/**
 *
 */
public class RuntimeUDFContext implements RuntimeContext {
	
	private final String name;
	
	private final int numParallelSubtasks;
	
	private final int subtaskIndex;
	
//  private HashMap<String, DoubleCounter> doubleCounters = new HashMap<String, DoubleCounter>();
//  private HashMap<String, IntCounter> intCounters = new HashMap<String, IntCounter>();
  
//  private HashMap<String, Aggregator<?>> aggregators = new HashMap<String, Aggregator<?>>();

//  private HashMap<String, Accumulator<?>> accumulators = new HashMap<String, Accumulator<?>>();
  private HashMap<String, Accumulable<?, ?>> accumulables = new HashMap<String, Accumulable<?, ?>>();
	
	public RuntimeUDFContext(String name, int numParallelSubtasks, int subtaskIndex) {
		this.name = name;
		this.numParallelSubtasks = numParallelSubtasks;
		this.subtaskIndex = subtaskIndex;
	}
	
	@Override
	public String getTaskName() {
		return this.name;
	}

	@Override
	public int getNumberOfParallelSubtasks() {
		return this.numParallelSubtasks;
	}

	@Override
	public int getIndexOfThisSubtask() {
		return this.subtaskIndex;
	}

  @Override
  public IntCounter getIntCounter(String name) {
  	return (IntCounter) getAccumulator(name, IntCounter.class);
  }

	@Override
	public LongCounter getLongCounter(String name) {
  	return (LongCounter) getAccumulator(name, LongCounter.class);
	}

	@Override
	public Histogram getHistogram(String name) {
  	return (Histogram) getAccumulable(name, Histogram.class);
	}

  @Override
  public DoubleCounter getDoubleCounter(String name) {
  	return (DoubleCounter) getAccumulator(name, DoubleCounter.class);
  }

//	@SuppressWarnings("unchecked")
//	@Override
//	public <T extends Value> Aggregator<T> getAggregator(String name, Class<? extends Aggregator<T>> aggregatorClass) {
//
//		Aggregator<?> aggregator = aggregators.get(name);
//		
//		if (aggregator != null) {
//			if (aggregator.getClass() != aggregatorClass) {
//				throw new UnsupportedOperationException("The aggregator object '" 
//						+ name + "' was created earlier with type " + aggregator.getClass() + " but is now requested as " + aggregatorClass);
//			}
//		} else {
//			// Create new accumulable object
//			try {
//				aggregator = aggregatorClass.newInstance();
//			} catch (InstantiationException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			}
//      aggregators.put(name, aggregator);
//		}
//		return (Aggregator<T>) aggregator;
//	}

	@Override
	public <T> Accumulator<T> getAccumulator(String name, Class<? extends Accumulator<T>> accumulatorClass) {
		return (Accumulator<T>) getAccumulable(name, accumulatorClass);
	}
  
	@SuppressWarnings("unchecked")
	@Override
	public <V, A> Accumulable<V, A> getAccumulable(String name, Class<? extends Accumulable<V, A>> accumulableClass) {

			Accumulable<?,?> accumulable = accumulables.get(name);
			
			if (accumulable != null) {
				AccumulatorHelper.compareAccumulatorTypes(name, accumulable.getClass(), accumulableClass);
			} else {
				// Create new accumulable object
				try {
					accumulable = accumulableClass.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
	      accumulables.put(name, accumulable);
			}
			return (Accumulable<V, A>) accumulable;
	}

	@Override
	public HashMap<String, Accumulable<?, ?>> getAllAccumulables() {
		return this.accumulables;
	}
	
//	@Override
//	public HashMap<String, Accumulator<?>> getAllAccumulators() {
//		return this.accumulators;
//	}

}
