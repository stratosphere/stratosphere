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
import eu.stratosphere.configuration.Configuration;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

/**
 *  An output format that writes record into collection
 */
public class CollectionOutputFormat<T extends Serializable> implements OutputFormat<T> {

	private static final long serialVersionUID = 1L;

	private static Object resultWrapper;   //since generic collection is not allowed to be static

	private transient Collection<T> taskResult;

	public CollectionOutputFormat(Collection<T> out) {
		this.resultWrapper = out;
	}


	@Override
	public void configure(Configuration parameters) {
	}


	@Override
	public void open(int taskNumber, int numTasks) throws IOException {
		try {
			this.taskResult = (Collection<T>) resultWrapper.getClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeRecord(T record) throws IOException {
		taskResult.add(SerializationUtils.clone(record));
	}


	@Override
	public void close() throws IOException {
		Collection<T> result = (Collection<T>) this.resultWrapper;
		result.addAll(taskResult);
	}


}
