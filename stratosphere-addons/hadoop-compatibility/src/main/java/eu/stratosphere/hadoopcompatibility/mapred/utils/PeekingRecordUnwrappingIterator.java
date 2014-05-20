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
package eu.stratosphere.hadoopcompatibility.mapred.utils;

import eu.stratosphere.hadoopcompatibility.mapred.record.datatypes.StratosphereTypeConverter;
import eu.stratosphere.types.Record;
import java.util.Iterator;

public class PeekingRecordUnwrappingIterator<V, K> implements Iterator<V>, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Iterator<Record> iterator;
	private StratosphereTypeConverter converter;
	private K key;
	private Record first;

	public PeekingRecordUnwrappingIterator(StratosphereTypeConverter converter) {
		this.converter =converter;
	}

	@SuppressWarnings("unchecked")
	public void set(Iterator<Record> iterator) {
		this.iterator = iterator;

		if(this.hasNext()) {
			this.first = iterator.next();
			this.key = (K) converter.convertKey(this.first);
		}
	}

	@Override
	public boolean hasNext() {
		if(this.first != null) {
			return true;
		}
		return iterator.hasNext();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V next() {
		if(this.first != null) {
			V val = (V) converter.convertValue(this.first);
			this.first = null;
			return val;
		}
		return (V) converter.convertValue(iterator.next());
	}

	public K getKey() {
		return this.key;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
