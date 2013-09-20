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

package eu.stratosphere.pact.compiler;

import static org.junit.Assert.fail;

import org.junit.Test;

import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.FileDataSource;
import eu.stratosphere.pact.common.contract.ReduceContract;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.compiler.plan.candidate.OptimizedPlan;
import eu.stratosphere.pact.compiler.plantranslate.NepheleJobGraphGenerator;
import eu.stratosphere.pact.compiler.util.DummyInputFormat;
import eu.stratosphere.pact.compiler.util.DummyOutputFormat;
import eu.stratosphere.pact.compiler.util.IdentityReduce;
import eu.stratosphere.pact.generic.contract.BulkIteration;


/**
 * This test case has been created to validate bugs that occurred when
 * the ReduceContract was used without a grouping key.
 */
public class ReduceAllTest extends CompilerTestBase  {

	@Test
	public void testReduce() {
		// construct the plan
		FileDataSource source = new FileDataSource(DummyInputFormat.class, IN_FILE, "Source");
		ReduceContract reduce1 = ReduceContract.builder(IdentityReduce.class).name("Reduce1").input(source).build();
		FileDataSink sink = new FileDataSink(DummyOutputFormat.class, OUT_FILE, "Sink");
		sink.setInput(reduce1);
		Plan plan = new Plan(sink, "Test Temp Task");
		plan.setDefaultParallelism(DEFAULT_PARALLELISM);
		
		
		try {
			OptimizedPlan oPlan = compileNoStats(plan);
			NepheleJobGraphGenerator jobGen = new NepheleJobGraphGenerator();
			jobGen.compileJobGraph(oPlan);
		} catch(CompilerException ce) {
			ce.printStackTrace();
			fail("The pact compiler is unable to compile this plan correctly");
		}
	}
	
	@Test
	public void testAllReduceIterations() {
		// construct a plan that uses an all reducer inside iterations
		FileDataSource source = new FileDataSource(DummyInputFormat.class, IN_FILE, "Source");

		BulkIteration iteration = new BulkIteration("Bulk Iteration AllReduce Test");
		iteration.setMaximumNumberOfIterations(2);
		iteration.setInput(source);
		
		ReduceContract reduce1 = ReduceContract.builder(IdentityReduce.class)
			.name("Reduce1").input(iteration.getPartialSolution()).build();
		iteration.setNextPartialSolution(reduce1);
		
		FileDataSink sink = new FileDataSink(DummyOutputFormat.class, OUT_FILE, "Sink");
		sink.setInput(iteration);
		Plan plan = new Plan(sink, "Test Temp Task");
		
		// Explicitly defined this to be > 1. This caused the compiler to report an error.
		plan.setDefaultParallelism(2);
		
		try {
			OptimizedPlan oPlan = compileNoStats(plan);
			NepheleJobGraphGenerator jobGen = new NepheleJobGraphGenerator();
			jobGen.compileJobGraph(oPlan);
		} catch(CompilerException ce) {
			ce.printStackTrace();
			fail("The pact compiler is unable to compile this plan correctly");
		}
	}	
}
