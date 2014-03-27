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
import eu.stratosphere.api.java.LocalEnvironment;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.functions.JoinFunction;
import eu.stratosphere.api.java.functions.KeySelector;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.util.Collector;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;


@SuppressWarnings("serial")
public class WordCountCustomType {

	public static class WCBase {
		protected String word;
	}

	public static class WC extends WCBase {

		public int count;
		public int docId;

		public WC() {}

		public WC(String word, int docId, int count) {
			this.word = word;
			this.count = count;
			this.docId = docId;
		}

		@Override
		public String toString() {
			return "count(" + word + ", " + docId + ", " + count + ")";
		}

		public String getWord() {
			return word;
		}
	}

	public static void main(String[] args) throws Exception {

		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		env.setDegreeOfParallelism(4);

		DataSet<String> text = env.fromElements("To be", "or not to be", "or to be still", "and certainly not to be not at all", "is that the question?");

		DataSet<WC> tokenized = text.flatMap(new FlatMapFunction<String, WC>() {
			@Override
			public void flatMap(String value, Collector<WC> out) {
				String[] tokens = value.toLowerCase().split("\\W");
				int docId = 0;
				for (String token : tokens) {
					out.collect(new WC(token, docId, 1));
					docId = 1 - docId;
				}
			}
		});


		DataSet<WC> result = tokenized
				.groupBy("word", "docId")
				.reduce(new ReduceFunction<WC>() {
					public WC reduce(WC value1, WC value2) {
						return new WC(value1.word, value1.docId, value1.count + value2.count);
					}
				});

		result.print();

		env.execute();
	}
}
