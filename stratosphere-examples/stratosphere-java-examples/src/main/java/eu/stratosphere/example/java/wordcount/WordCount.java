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
package eu.stratosphere.example.java.wordcount;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.aggregation.Aggregations;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.util.Collector;

/**
 * Implements a word count which takes the input file and counts the number of
 * occurrences of each word in the file and writes the result back to disk.
 */
@SuppressWarnings("serial")
public class WordCount {
	
	/**
	 * Contains the program. 
	 * 
	 * <br /><br />
	 * 
	 * @param args Parameters defining the input and output path.  
	 * 				Paths must start with "file://..." or "hdfs://...".
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: WordCount <input path> <result path>");
			return;
		}
		
		final String inputPath = args[0];
		final String outputPath = args[1];
		
		// get the environment as starting point
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		
		// read the text file from given input path
		DataSet<String> text = env.readTextFile(inputPath);
		
		// split up the lines in pairs (tuples with arity 2) containing: (word,1)
		DataSet<Tuple2<String, Integer>> words = text.flatMap(new Tokenizer());
		
		// group by the tuple field "0" and sum up tuple field "1"
		DataSet<Tuple2<String, Integer>> result = words.groupBy(0).aggregate(Aggregations.SUM, 1);
		
		// write out the result
		result.writeAsText(outputPath);
		
		// execute the defined program
		env.execute("Word Count");
	}
	
	/**
	 * Implements a user-defined FlatMapFunction. The function takes a line (String) and splits it into 
	 * multiple pairs in the form of "(word,1)" (Tuple2<String, Integer>).
	 */
	public static final class Tokenizer extends FlatMapFunction<String, Tuple2<String, Integer>> {

		@Override
		public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
			// normalize and split the line
			String[] tokens = value.toLowerCase().split("\\W+");
			
			// emit the pairs
			for (String token : tokens) {
				if (token.length() > 0) {
					out.collect(new Tuple2<String, Integer>(token, 1));
				}
			}
		}
	}
}
