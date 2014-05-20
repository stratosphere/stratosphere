/***********************************************************************************************************************
 * Copyright (C) 2010-2014 by the Stratosphere project (http://stratosphere.eu)
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

package eu.stratosphere.hadoopcompatibility.mapred.example;

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.common.Program;
import eu.stratosphere.api.common.ProgramDescription;
import eu.stratosphere.api.java.record.operators.MapOperator;
import eu.stratosphere.api.java.record.operators.ReduceOperator;
import eu.stratosphere.client.LocalExecutor;
import eu.stratosphere.hadoopcompatibility.mapred.record.HadoopDataSink;
import eu.stratosphere.hadoopcompatibility.mapred.record.HadoopDataSource;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.HadoopMapperWrapper;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.HadoopReducerWrapper;
import eu.stratosphere.types.StringValue;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.LongSumReducer;
import org.apache.hadoop.mapred.lib.TokenCountMapper;

import java.io.IOException;

public class HadoopWordCount implements Program, ProgramDescription {

	public static class TestTokenCounterMapper<K> extends TokenCountMapper<K> {
		@Override
		public void map(K key, Text value, OutputCollector<Text,LongWritable> output, Reporter reporter)
				throws IOException {
			String line = value.toString().replaceAll("\\W+", " ").toLowerCase();
			Text strippedValue = new Text(line);
			super.map(key, strippedValue, output, reporter);
		}
	}

	@Override
	public Plan getPlan(String... args) {
		// parse job parameters
		int numSubTasks   = (args.length > 0 ? Integer.parseInt(args[0]) : 1);
		String dataInput = (args.length > 1 ? args[1] : "");
		String output    = (args.length > 2 ? args[2] : "");

		JobConf jobConf = new JobConf();
		HadoopDataSource<LongWritable, Text> source = new HadoopDataSource<LongWritable, Text>(
				new TextInputFormat(), jobConf, "Input Lines");
		TextInputFormat.addInputPath(source.getJobConf(), new Path(dataInput));

		jobConf.setMapperClass(TestTokenCounterMapper.class);
		MapOperator mapper = MapOperator.builder(new HadoopMapperWrapper(jobConf))
				.input(source)
				.name("Tokenize Lines")
				.build();

		jobConf.setReducerClass(LongSumReducer.class);
		jobConf.setCombinerClass(LongSumReducer.class);
		ReduceOperator reducer = ReduceOperator.builder(new HadoopReducerWrapper(jobConf), StringValue.class, 0)
				.input(mapper)
				.name("Count Words")
				.build();
		HadoopDataSink<Text, LongWritable> out = new HadoopDataSink<Text, LongWritable>(new TextOutputFormat<Text,
				LongWritable>(),jobConf, "Hadoop TextOutputFormat", reducer, Text.class, LongWritable.class);
		TextOutputFormat.setOutputPath(out.getJobConf(), new Path(output));

		Plan plan = new Plan(out, "Hadoop Basic Tasks Example");
		plan.setDefaultParallelism(numSubTasks);
		return plan;
	}

	@Override
	public String getDescription() {
		return "Parameters: [numSubStasks] [input] [output]";
	}

	public static void main(String[] args) throws Exception {
		HadoopWordCount wc = new HadoopWordCount();

		if (args.length < 3) {
			System.err.println(wc.getDescription());
			System.exit(1);
		}

		Plan plan = wc.getPlan(args);

		// This will execute the word-count embedded in a local context. replace this line by the commented
		// succeeding line to send the job to a local installation or to a cluster for execution
		LocalExecutor.execute(plan);
//		PlanExecutor ex = new RemoteExecutor("localhost", 6123, "target/pact-examples-0.4-SNAPSHOT-WordCount.jar");
//		ex.executePlan(plan);
	}
}
