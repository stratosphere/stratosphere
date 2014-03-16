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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.stratosphere.api.common.typeutils.TypeComparator;
import eu.stratosphere.api.common.typeutils.TypeSerializer;
import eu.stratosphere.api.java.functions.util.CollectionOutputCollector;
import eu.stratosphere.api.java.functions.util.KeyGroupedIterator;
import eu.stratosphere.api.java.functions.util.MutableObjectIteratorWrapper;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.api.java.typeutils.TupleTypeInfo;
import eu.stratosphere.util.Collector;
import eu.stratosphere.util.MutableObjectIterator;

public class ReduceFunctionTest {

	@Test
	public void reduceTest() throws Exception {
		
		// input and output data
		List<Tuple2<Integer, Integer>> inData = new ArrayList<Tuple2<Integer, Integer>>();
		List<Tuple2<Integer, Integer>> outData = new ArrayList<Tuple2<Integer, Integer>>();
		
		// initialize input data
		for(int i=1; i<6; i++) {
			for(int j=0; j<i; j++) {
				inData.add(new Tuple2<Integer, Integer>(i,1));
			}
		}
				
		// create serializer and comparators
		TupleTypeInfo<Tuple2<Integer, Integer>> typeInfo = 
				new TupleTypeInfo<Tuple2<Integer, Integer>>(
						BasicTypeInfo.INT_TYPE_INFO,
						BasicTypeInfo.INT_TYPE_INFO);
		
		TypeSerializer<Tuple2<Integer, Integer>> serializer = 
				typeInfo.createSerializer();
		TypeComparator<Tuple2<Integer, Integer>> comparator = 
				typeInfo.createComparator(new int[]{0}, new boolean[]{true});
		
		// create mutable iterators as in runtime
		MutableObjectIterator<Tuple2<Integer, Integer>> moi = 
				new MutableObjectIteratorWrapper<Tuple2<Integer, Integer>>(inData.iterator(), serializer);
		KeyGroupedIterator<Tuple2<Integer, Integer>> kgi = 
				new KeyGroupedIterator<Tuple2<Integer, Integer>>(moi, serializer, comparator);
		
		// initialize reduce function
		ReduceFunction<Tuple2<Integer, Integer>> testFunc = new ReduceTestFunction();
		testFunc.setTypeSerializer(serializer);
				
		// initialize output collector
		Collector<Tuple2<Integer, Integer>> out = 
				new CollectionOutputCollector<Tuple2<Integer, Integer>>(outData, serializer);
		
		// run reduce function on input data
		while (kgi.nextKey()) {
			testFunc.reduce(kgi.getValues(), out);
		}
		
		// check result
		for(int i=0; i<outData.size(); i++) {
			Assert.assertTrue(
					((Integer)outData.get(i).getField(0)).intValue() ==
					((Integer)outData.get(i).getField(1)).intValue());
		}
		
	}
	
	private static class ReduceTestFunction extends ReduceFunction<Tuple2<Integer, Integer>> {
		private static final long serialVersionUID = 1L;

		@Override
		public Tuple2<Integer, Integer> reduce(Tuple2<Integer, Integer> value1, Tuple2<Integer, Integer> value2) 
				throws Exception 
		{

			int newVal = ((Integer)value1.getField(1)).intValue() + 
						 ((Integer)value2.getField(1)).intValue();
			value1.setField(newVal, 1);
			return value1;
		}
	}
	
}
