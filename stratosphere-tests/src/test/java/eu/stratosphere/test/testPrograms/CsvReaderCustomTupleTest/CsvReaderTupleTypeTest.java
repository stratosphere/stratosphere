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

package eu.stratosphere.test.testPrograms.CsvReaderCustomTupleTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.io.CsvReader;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

public class CsvReaderTupleTypeTest {

	public static class Item extends Tuple4<Integer, String, Double, String> {
		
		private static final long serialVersionUID = -7444437337392053502L;
		
		public Item() {
		}
		
		public Item(Integer f0, String f1, Double f2, String f3) {
			this.f0 = f0;
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
		
		public int getID() {
			return this.f0;
		}
		public void setID(int iD) {
			this.f0 = iD;
		}
		public String getValue1() {
			return this.f1;
		}
		public void setValue1(String value1) {
			this.f1 = value1;
		}
		public double getDouble1() {
			return this.f2;
		}
		public void setDouble1(double double1) {
			this.f2 = double1;
		}
		public String getValue3() {
			return this.f3;
		}
		public void setValue3(String value3) {
			this.f3 = value3;
		}
	}	
	
	public static void main(String[] args) throws Exception {
	String testString = "1|value1|32.4|value3";
		
			ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
			String filePath = createTempFile(testString);
			DataSet<Item> itemSet = env.readCsvFile(filePath).fieldDelimiter('|').lineDelimiter(";").includeFields("1111").tupleType(Item.class);
			itemSet = itemSet.flatMap(new FlatMapFunction<Item, Item>() {
				Item expected;
				
				@Override
				public void open(Configuration parameters) throws Exception {
					super.open(parameters);
					expected = new Item(1, "value1", 32.4, "value3");
				}

				@Override
				public void flatMap(Item value, Collector<Item> out) throws Exception {
					System.out.println(value + " | " + expected);
					Assert.assertTrue(value.getID() == expected.getID());
					Assert.assertTrue(value.getValue1().equals(expected.getValue1()));
					Assert.assertTrue(value.getValue3().equals(expected.getValue3()));
					Assert.assertTrue(value.getDouble1() == expected.getDouble1());
					out.collect(value);
				}
				
			});
			itemSet.print();
			env.execute();
		
	}
	
	static private String createTempFile(String content) throws IOException {
		File tempFile = File.createTempFile("test_contents", "tmp");
		tempFile.deleteOnExit();
		
		FileWriter wrt = new FileWriter(tempFile);
		wrt.write(content);
		wrt.close();
			
		return tempFile.getAbsolutePath();
	}
}
