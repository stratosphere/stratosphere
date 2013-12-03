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
import eu.stratosphere.pact.common.stubs.aggregators.DoubleCounter;
import eu.stratosphere.pact.common.stubs.aggregators.IntCounter;

/**
 *
 */
public class RuntimeUDFContext implements RuntimeContext {
	
	private final String name;
	
	private final int numParallelSubtasks;
	
	private final int subtaskIndex;
	
  private HashMap<String, DoubleCounter> doubleCounters = null;
  private HashMap<String, IntCounter> intCounters = null;
	
	public RuntimeUDFContext(String name, int numParallelSubtasks, int subtaskIndex) {
		this.name = name;
		this.numParallelSubtasks = numParallelSubtasks;
		this.subtaskIndex = subtaskIndex;
		this.intCounters = new HashMap<String, IntCounter>();
		this.doubleCounters = new HashMap<String, DoubleCounter>();
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
  public DoubleCounter getDoubleCounter(String name) {
    DoubleCounter counter = doubleCounters.get(name);
    if (counter == null) {
      counter = new DoubleCounter();
      doubleCounters.put(name, counter);
    }
    return counter;
  }

  @Override
  public IntCounter getIntCounter(String name) {
    IntCounter counter = intCounters.get(name);
    if (counter == null) {
      counter = new IntCounter();
      intCounters.put(name, counter);
    }
    return counter;
  }
  
}
