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
package eu.stratosphere.api.java.io;


import eu.stratosphere.api.common.io.OutputFormat;
import eu.stratosphere.api.common.typeutils.TypeSerializer;
import eu.stratosphere.configuration.Configuration;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

/**
 *  An output format that writes record into collection
 */
public class CollectionOutputFormat<T> implements OutputFormat<T> {

	private static final long serialVersionUID = 1L;

	private static Object resultWrapper;   //since generic collection is not allowed to be static

	private transient Collection<T> taskResult;

	public TypeSerializer<T> typeSerializer;

	public CollectionOutputFormat(Collection<T> out, TypeSerializer<T> serializer) {
		this.resultWrapper = out;
		this.typeSerializer = serializer;
	}


	@Override
	public void configure(Configuration parameters) {
	}


	@Override
	public void open(int taskNumber, int numTasks) throws IOException {
		try {
			this.taskResult = (Collection<T>) this.resultWrapper.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new IOException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void writeRecord(T record) throws IOException {
		T recordCopy = this.typeSerializer.createInstance();
		this.typeSerializer.copy(record, recordCopy);
		this.taskResult.add(recordCopy);
	}


	@Override
	public void close() throws IOException {
		Collection<T> result = (Collection<T>) this.resultWrapper;
		result.addAll(this.taskResult);
	}


}
