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
package eu.stratosphere.api.java.functions;

import java.util.Iterator;

import eu.stratosphere.api.common.functions.AbstractFunction;
import eu.stratosphere.api.common.functions.GenericGroupReduce;
import eu.stratosphere.api.common.typeutils.TypeSerializer;
import eu.stratosphere.util.Collector;


public abstract class ReduceFunction<T> extends AbstractFunction implements GenericGroupReduce<T, T> {
	
	private static final long serialVersionUID = 1L;
	
	private TypeSerializer<T> serializer;
	private T curr; 

	/**
	 * Combines two values into one.
	 * The reduce function is consecutively applied to all values of a group until only a single value remains.
	 * In functional programming, this is known as a fold-style aggregation.
	 * 
	 * Important: It is fine to return the first value (value1) as result from this function. 
	 * You may NOT return the second value from this function.
	 * 
	 * @param value1 The first value to combine. This object may be returned as result.  
	 * @param value2 The second value to combine. This object may NOT be returned as result.
	 * @return The combined value of both input values.
	 * 
	 * @throws Exception
	 */
	public abstract T reduce(T value1, T value2) throws Exception;
	
	@Override
	public final void reduce(Iterator<T> values, Collector<T> out) throws Exception {
		
		if(this.serializer == null) {
			throw new RuntimeException("TypeSerializer was not initialized.");
		}
		
		this.serializer.copy(values.next(), this.curr);
		
		while (values.hasNext()) {
			this.curr = reduce(this.curr, values.next());
		}
		
		out.collect(this.curr);
	}
	
	@Override
	public final void combine(Iterator<T> values, Collector<T> out) throws Exception {
		reduce(values, out);
	}
	
	public final void setTypeSerializer(TypeSerializer<T> serializer) {
		this.serializer = serializer;
		this.curr = serializer.createInstance();
	}
}
