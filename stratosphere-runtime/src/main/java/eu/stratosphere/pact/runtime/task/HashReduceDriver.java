/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WTHOUT WARRANTIES OR CONDTIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/

package eu.stratosphere.pact.runtime.task;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.api.common.functions.GenericReduce;
import eu.stratosphere.api.common.typeutils.TypeComparator;
import eu.stratosphere.api.common.typeutils.TypePairComparator;
import eu.stratosphere.api.common.typeutils.TypeSerializer;
import eu.stratosphere.core.memory.MemorySegment;
import eu.stratosphere.nephele.services.memorymanager.MemoryManager;
import eu.stratosphere.pact.runtime.hash.AbstractHashTableProber;
import eu.stratosphere.pact.runtime.hash.AbstractMutableHashTable;
import eu.stratosphere.pact.runtime.hash.CompactingHashTable;
import eu.stratosphere.pact.runtime.task.util.TaskConfig;
import eu.stratosphere.util.Collector;
import eu.stratosphere.util.MutableObjectIterator;

/**
 * HashReduce task which is executed by a Nephele task manager. The task has a
 * single input and one or multiple outputs. It is provided with a ReduceFunction
 * implementation.
 * <p>
 * The ReduceTask creates a iterator over all records from its input. 
 * Each element is saved in a hash-table. 
 * If there is already a entry for the hash-value of the record in the hash-table
 * reduce is called with the new record and the record from the table and the result is saved in the hashtable.
 * 
 * @see GenericReduce
 */
public class HashReduceDriver<T> implements PactDriver<GenericReduce<T>, T> {
	
	private static final Log LOG = LogFactory.getLog(HashReduceDriver.class);

	private PactTaskContext<GenericReduce<T>, T> taskContext;
	
	private MutableObjectIterator<T> input;

	private TypeSerializer<T> serializer;

	private TypeComparator<T> comparator;
	
	private volatile boolean running;
	
	private AbstractMutableHashTable<T> table;
	
	private AbstractHashTableProber<T, T> prober;

	// ------------------------------------------------------------------------

	// A TypePairComparator for just one type, it is used for the hashtable-prober, which usually expects two different classes
	@SuppressWarnings("hiding")
	private class HashReduceTypePairComparator<T> extends TypePairComparator<T, T>{

		private TypeComparator<T> comparator;
		private TypeComparator<T> comparator2;
		
		public HashReduceTypePairComparator(TypeComparator<T> comparator){
			this.comparator = comparator;
			this.comparator2 = this.comparator.duplicate();
		}
		
		@Override
		public void setReference(T reference) {
			this.comparator.setReference(reference);
		}

		@Override
		public boolean equalToReference(T candidate) {
			return this.comparator.equalToReference(candidate);
		}

		@Override
		public int compareToReference(T candidate) {
			this.comparator2.setReference(candidate);
			return this.comparator.compareToReference(comparator2);
		}
		
	}
	
	@Override
	public void setup(PactTaskContext<GenericReduce<T>, T> context) {
		this.taskContext = context;
		this.running = true;
	}
	
	@Override
	public int getNumberOfInputs() {
		return 1;
	}

	@Override
	public Class<GenericReduce<T>> getStubType() {
		@SuppressWarnings("unchecked")
		final Class<GenericReduce<T>> clazz = (Class<GenericReduce<T>>) (Class<?>) GenericReduce.class;
		return clazz;
	}

	@Override
	public boolean requiresComparatorOnInput() {
		return true;
	}

	// --------------------------------------------------------------------------------------------

	@Override
	public void prepare() throws Exception {
		TaskConfig config = this.taskContext.getTaskConfig();
		if (config.getDriverStrategy() != DriverStrategy.HASH_REDUCE) {
			throw new Exception("Unrecognized driver strategy for HashReduce driver: " + config.getDriverStrategy().name());
		}
		this.serializer = this.taskContext.getInputSerializer(0);
		this.comparator = this.taskContext.getInputComparator(0);
		this.input = this.taskContext.getInput(0);
		
		// obtain task manager's memory manager and I/O manager to obtain memory for the hashtable
		final MemoryManager memoryManager = this.taskContext.getMemoryManager();
		final long availableMemory = config.getMemoryDriver();
		final int numPages = memoryManager.computeNumberOfPages(availableMemory);
		final List<MemorySegment> memorySegments = memoryManager.allocatePages(this.taskContext.getOwningNepheleTask(), numPages);
		
		// setup hashtable and prober
		table = new CompactingHashTable<T>(serializer, comparator, memorySegments);
		table.open();
		prober = table.getProber(comparator, new HashReduceTypePairComparator<T>(comparator));
	}

	@Override
	public void run() throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug(this.taskContext.formatLogString("HashReducer preprocessing done. Running Reducer code."));
		}
		
		// cache references on the stack
		final GenericReduce<T> stub = this.taskContext.getStub();
		final Collector<T> output = this.taskContext.getOutputCollector();
		
		// Create to instances of T
		T curr = this.serializer.createInstance();
		T fromTable = this.serializer.createInstance();
		
		// Now run while we have input
		while (this.running && (input.next(curr) != null)) {
			// If there is a match in the hash-table reduce/update, otherwise just insert the element
			if(prober.getMatchFor(curr, fromTable)){
				prober.updateMatch(stub.reduce(curr, fromTable));
			}else{
				table.insert(curr);
			} 
		}
		
		if(!this.running){
			return;
		}

		// Read all the entries from the table and output them
		MutableObjectIterator<T> outIter = table.getEntryIterator();
		while(this.running && (curr = outIter.next(curr)) != null){
			output.collect(curr);
		}
	}

	@Override
	public void cleanup() {
		this.table.close();
	}

	@Override
	public void cancel() {
		this.running = false;
		this.table.abort();
	}
}