/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
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

package eu.stratosphere.pact.test.counters;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.FileDataSource;
import eu.stratosphere.pact.common.contract.MapContract;
import eu.stratosphere.pact.common.contract.ReduceContract;
import eu.stratosphere.pact.common.contract.ReduceContract.Combinable;
import eu.stratosphere.pact.common.io.RecordOutputFormat;
import eu.stratosphere.pact.common.io.TextInputFormat;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.MapStub;
import eu.stratosphere.pact.common.stubs.ReduceStub;
import eu.stratosphere.pact.common.stubs.StubAnnotation.ConstantFields;
import eu.stratosphere.pact.common.stubs.accumulables.DoubleCounter;
import eu.stratosphere.pact.common.stubs.accumulables.Histogram;
import eu.stratosphere.pact.common.stubs.accumulables.IntCounter;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;
import eu.stratosphere.pact.example.util.AsciiUtils;
import eu.stratosphere.pact.test.util.TestBase2;

/**
 * TODO Test conflict when different UDFs write to counter with same name but with different type.
 */
@RunWith(Parameterized.class)
public class CounterITCase extends TestBase2 {

	private static final String INPUT = "one\n" + "two two\n" + "three three three\n";
	private static final String EXPECTED = "one 1\ntwo 2\nthree 3\n";

	protected String dataPath;
	protected String resultPath;
	
	public CounterITCase(Configuration config) {
		super(config);
	}

	@Override
	protected void preSubmit() throws Exception {
		dataPath = createTempFile("datapoints.txt", INPUT);
		resultPath = getTempFilePath("result");
	}
	
	@Override
	protected void postSubmit() throws Exception {
		compareResultsByLinesInMemory(EXPECTED, resultPath);
	}

	@Override
	protected Plan getPactPlan() {
		Plan plan = getTestPlanPlan(config.getInteger("IterationAllReducer#NoSubtasks", 1), dataPath, resultPath);
		return plan;
	}

	@Parameters
	public static Collection<Object[]> getConfigurations() {
		Configuration config1 = new Configuration();
		config1.setInteger("IterationAllReducer#NoSubtasks", 2);
		return toParameterList(config1);
	}
	
	static Plan getTestPlanPlan(int numSubTasks, String input, String output) {
		
		FileDataSource source = new FileDataSource(new TextInputFormat(), input, "Input Lines");
		source.setParameter(TextInputFormat.CHARSET_NAME, "ASCII");
		MapContract mapper = MapContract.builder(new TokenizeLine())
			.input(source)
			.name("Tokenize Lines")
			.build();
		ReduceContract reducer = ReduceContract.builder(CountWords.class, PactString.class, 0)
			.input(mapper)
			.name("Count Words")
			.build();
		FileDataSink out = new FileDataSink(new RecordOutputFormat(), output, reducer, "Word Counts");
		RecordOutputFormat.configureRecordFormat(out)
			.recordDelimiter('\n')
			.fieldDelimiter(' ')
			.field(PactString.class, 0)
			.field(PactInteger.class, 1);
		Plan plan = new Plan(out, "WordCount Example");
		plan.setDefaultParallelism(numSubTasks);
		
		return plan;
	}
	
	public static class TokenizeLine extends MapStub implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final PactRecord outputRecord = new PactRecord();
		private final PactString word = new PactString();
		private final PactInteger one = new PactInteger(1);
		
		private final AsciiUtils.WhitespaceTokenizer tokenizer =
				new AsciiUtils.WhitespaceTokenizer();

		// Needs to be instantiated in open() since the runtime context is not yet
		// initialized
		IntCounter cntNumLines = null;
		DoubleCounter openCloseCounter = null;
    Histogram wordsPerLineDistribution = null;
    
		@Override
		public void open(Configuration parameters) throws Exception {
		  System.out.println("Map: open");
		  
			this.cntNumLines = getRuntimeContext().getIntCounter("num-lines");
			this.openCloseCounter = getRuntimeContext().getDoubleCounter("open-close-counter");
      this.wordsPerLineDistribution = getRuntimeContext().getHistogram("words-per-line");
      
      // Create counter and test increment
      IntCounter simpleCounter = getRuntimeContext().getIntCounter("simple-counter");
      simpleCounter.add(1);
      Assert.assertEquals(simpleCounter.getLocalValue().intValue(), 1);
      
      // Test if we get the same counter
      IntCounter simpleCounter2 = getRuntimeContext().getIntCounter("simple-counter");
      Assert.assertEquals(simpleCounter.getLocalValue(), simpleCounter2.getLocalValue());

      // Should fail if we request it with different type
      try {
        DoubleCounter simpleCounter3 = getRuntimeContext().getDoubleCounter("simple-counter");
//      	DoubleSumAggregator longAggregator3 = (DoubleSumAggregator) getRuntimeContext().getAggregator("custom", DoubleSumAggregator.class);
        Assert.fail("Should not be able to obtain previously created counter with different type");
	    } catch (UnsupportedOperationException ex) {
	    }
      
      // Test counter used in open() and closed()
      this.openCloseCounter.add(0.5);
		}
		
		@Override
		public void map(PactRecord record, Collector<PactRecord> collector) {
      System.out.println("Map: map");
      
			this.cntNumLines.add(1);
			
			PactString line = record.getField(0, PactString.class);
			AsciiUtils.replaceNonWordChars(line, ' ');
			AsciiUtils.toLowerCase(line);
			this.tokenizer.setStringToTokenize(line);
			int wordsPerLine = 0;
			while (tokenizer.next(this.word))
			{
				this.outputRecord.setField(0, this.word);
				this.outputRecord.setField(1, this.one);
				collector.collect(this.outputRecord);
				++ wordsPerLine;
			}
			wordsPerLineDistribution.add(wordsPerLine);
		}
		
		@Override
		public void close() throws Exception {
      System.out.println("Map: close");
      
      // Test counter used in open and close only
      this.openCloseCounter.add(0.5);
      Assert.assertEquals(1, this.openCloseCounter.getLocalValue().intValue());
		}
		
	}

	@Combinable
	@ConstantFields(0)
	public static class CountWords extends ReduceStub implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private final PactInteger cnt = new PactInteger();
		
		private IntCounter reduceCalls = null;
		private IntCounter combineCalls = null;
		
		@Override
		public void open(Configuration parameters) throws Exception {
      System.out.println("Reduce: open");
  		this.reduceCalls = getRuntimeContext().getIntCounter("reduce-calls");
  		this.combineCalls = getRuntimeContext().getIntCounter("combine-calls");
		}
		
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception {
      System.out.println("Reduce: reduce");
      reduceCalls.add(1);
      reduceInternal(records, out);
		}
		
		@Override
		public void combine(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception {
			combineCalls.add(1);
      System.out.println("Reduce: combine");
			reduceInternal(records, out);
		}
		
		private void reduceInternal(Iterator<PactRecord> records, Collector<PactRecord> out) {
			PactRecord element = null;
			int sum = 0;
			while (records.hasNext()) {
				element = records.next();
				PactInteger i = element.getField(1, PactInteger.class);
				sum += i.getValue();
			}

			this.cnt.setValue(sum);
			element.setField(1, this.cnt);
			out.collect(element);
		}
		
		@Override
		public void close() throws Exception {
      System.out.println("Reduce: close");
		}
	}

}
