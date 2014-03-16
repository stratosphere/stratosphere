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
package eu.stratosphere.api.java.functions.util;

import java.util.Collection;

import eu.stratosphere.api.common.typeutils.TypeSerializer;
import eu.stratosphere.util.Collector;

/**
 * Implements an output collector that stores its collected elements in a supplied list.
 */
public class CollectionOutputCollector<E> implements Collector<E> {
	
	private final Collection<E> output;
	private final TypeSerializer<E> serializer;
	
	public CollectionOutputCollector(Collection<E> outputList, TypeSerializer<E> serializer) {
		this.output = outputList;
		this.serializer = serializer;
	}

	@Override
	public void collect(E in) {
		E copy = serializer.createInstance();
		serializer.copy(in, copy);
		this.output.add(copy);
	}

	@Override
	public void close() {}
}