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

package eu.stratosphere.api.common.operators.base;

import eu.stratosphere.api.common.functions.GenericReducer;
import eu.stratosphere.api.common.operators.SingleInputOperator;
import eu.stratosphere.api.common.operators.util.UserCodeClassWrapper;
import eu.stratosphere.api.common.operators.util.UserCodeObjectWrapper;
import eu.stratosphere.api.common.operators.util.UserCodeWrapper;


/**
 * ReduceContract represents a Pact with a Reduce Input Operator.
 * InputContracts are second-order functions. They have one or multiple input sets of records and a first-order
 * user function (stub implementation).
 * <p> 
 * Reduce works on a single input and calls the first-order user function of a {@link GenericReducer} for each group of 
 * records that share the same key.
 * 
 * @see GenericReducer
 */
public class ReduceOperatorBase<T extends GenericReducer<?, ?>> extends SingleInputOperator<T> {
	
	private boolean combinable;
	
	
	public ReduceOperatorBase(UserCodeWrapper<T> udf, int[] keyPositions, String name) {
		super(udf, keyPositions, name);
		this.combinable = false;
	}
	
	public ReduceOperatorBase(T udf, int[] keyPositions, String name) {
		super(new UserCodeObjectWrapper<T>(udf), keyPositions, name);
		this.combinable = false;
	}
	
	public ReduceOperatorBase(Class<? extends T> udf, int[] keyPositions, String name) {
		super(new UserCodeClassWrapper<T>(udf), keyPositions, name);
		this.combinable = false;
	}
	
	public ReduceOperatorBase(UserCodeWrapper<T> udf, String name) {
		super(udf, name);
		this.combinable = false;
	}
	
	public ReduceOperatorBase(T udf, String name) {
		super(new UserCodeObjectWrapper<T>(udf), name);
		this.combinable = false;
	}
	
	public ReduceOperatorBase(Class<? extends T> udf, String name) {
		super(new UserCodeClassWrapper<T>(udf), name);
		this.combinable = false;
	}
	
	public void setCombinable(boolean combinable) {
		this.combinable = combinable;
	}
	
	public boolean isCombinable() {
		return this.combinable;
	}

}
