/***********************************************************************************************************************
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
 **********************************************************************************************************************/

package eu.stratosphere.api.java.io;

import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.api.java.tuple.Tuple5;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.core.fs.FileInputSplit;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.types.DateValue;
import eu.stratosphere.types.parser.DateParser;
import eu.stratosphere.util.LogUtils;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class CsvInputFormatTest {
	
	private static final Path PATH = new Path("an/ignored/file/");
	
	
	@BeforeClass
	public static void initialize() {
		LogUtils.initializeDefaultConsoleLogger(Level.WARN);
	}
	
	@Test
	public void readStringFields() {
		try {
			final String fileContent = "abc|def|ghijk\nabc||hhg\n|||";
			final FileInputSplit split = createTempFile(fileContent);
			
			final CsvInputFormat<Tuple3<String, String, String>> format = new CsvInputFormat<Tuple3<String, String, String>>(PATH, "\n", '|', String.class, String.class, String.class);
		
			final Configuration parameters = new Configuration();
			format.configure(parameters);
			format.open(split);
			
			Tuple3<String, String, String> result = new Tuple3<String, String, String>();
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals("abc", result.f0);
			assertEquals("def", result.f1);
			assertEquals("ghijk", result.f2);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals("abc", result.f0);
			assertEquals("", result.f1);
			assertEquals("hhg", result.f2);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals("", result.f0);
			assertEquals("", result.f1);
			assertEquals("", result.f2);
			
			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}
	
	@Test
	public void readStringFieldsWithTrailingDelimiters() {
		try {
			final String fileContent = "abc|def|ghijk\nabc||hhg\n|||\n";
			final FileInputSplit split = createTempFile(fileContent);
		
			final CsvInputFormat<Tuple3<String, String, String>> format = new CsvInputFormat<Tuple3<String, String, String>>(PATH);
			
			format.setFieldDelimiter('|');
			format.setFieldTypes(String.class, String.class, String.class);
			
			format.configure(new Configuration());
			format.open(split);

			Tuple3<String, String, String> result = new Tuple3<String, String, String>();
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals("abc", result.f0);
			assertEquals("def", result.f1);
			assertEquals("ghijk", result.f2);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals("abc", result.f0);
			assertEquals("", result.f1);
			assertEquals("hhg", result.f2);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals("", result.f0);
			assertEquals("", result.f1);
			assertEquals("", result.f2);
			
			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}
	
	@Test
	public void testIntegerFieldsl() throws IOException {
		try {
			final String fileContent = "111|222|333|444|555\n666|777|888|999|000|\n";
			final FileInputSplit split = createTempFile(fileContent);	
		
			final CsvInputFormat<Tuple5<Integer, Integer, Integer, Integer, Integer>> format = new CsvInputFormat<Tuple5<Integer, Integer, Integer, Integer, Integer>>(PATH);
			
			format.setFieldDelimiter('|');
			format.setFieldTypes(Integer.class, Integer.class, Integer.class, Integer.class, Integer.class);
			
			format.configure(new Configuration());
			format.open(split);
			
			Tuple5<Integer, Integer, Integer, Integer, Integer> result = new Tuple5<Integer, Integer, Integer, Integer, Integer>();
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(111), result.f0);
			assertEquals(Integer.valueOf(222), result.f1);
			assertEquals(Integer.valueOf(333), result.f2);
			assertEquals(Integer.valueOf(444), result.f3);
			assertEquals(Integer.valueOf(555), result.f4);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(666), result.f0);
			assertEquals(Integer.valueOf(777), result.f1);
			assertEquals(Integer.valueOf(888), result.f2);
			assertEquals(Integer.valueOf(999), result.f3);
			assertEquals(Integer.valueOf(000), result.f4);
			
			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}
	
	@Test
	public void testReadFirstN() throws IOException {
		try {
			final String fileContent = "111|222|333|444|555|\n666|777|888|999|000|\n";
			final FileInputSplit split = createTempFile(fileContent);	
		
			final CsvInputFormat<Tuple2<Integer, Integer>> format = new CsvInputFormat<Tuple2<Integer, Integer>>(PATH);
			
			format.setFieldDelimiter('|');
			format.setFieldTypes(Integer.class, Integer.class);
			
			format.configure(new Configuration());
			format.open(split);
			
			Tuple2<Integer, Integer> result = new Tuple2<Integer, Integer>();
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(111), result.f0);
			assertEquals(Integer.valueOf(222), result.f1);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(666), result.f0);
			assertEquals(Integer.valueOf(777), result.f1);
			
			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
		
	}
	
	@Test
	public void testReadSparseWithNullFieldsForTypes() throws IOException {
		try {
			final String fileContent = "111|222|333|444|555|666|777|888|999|000|\n000|999|888|777|666|555|444|333|222|111|";
			final FileInputSplit split = createTempFile(fileContent);	
			
			final CsvInputFormat<Tuple3<Integer, Integer, Integer>> format = new CsvInputFormat<Tuple3<Integer, Integer, Integer>>(PATH);
			
			format.setFieldDelimiter('|');
			format.setFieldTypes(Integer.class, null, null, Integer.class, null, null, null, Integer.class);
			
			format.configure(new Configuration());
			format.open(split);
			
			Tuple3<Integer, Integer, Integer> result = new Tuple3<Integer, Integer, Integer>();
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(111), result.f0);
			assertEquals(Integer.valueOf(444), result.f1);
			assertEquals(Integer.valueOf(888), result.f2);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(000), result.f0);
			assertEquals(Integer.valueOf(777), result.f1);
			assertEquals(Integer.valueOf(333), result.f2);
			
			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}
	
	@Test
	public void testReadSparseWithPositionSetter() throws IOException {
		try {
			final String fileContent = "111|222|333|444|555|666|777|888|999|000|\n000|999|888|777|666|555|444|333|222|111|";
			final FileInputSplit split = createTempFile(fileContent);	
			
			final CsvInputFormat<Tuple3<Integer, Integer, Integer>> format = new CsvInputFormat<Tuple3<Integer, Integer, Integer>>(PATH);
			
			format.setFieldDelimiter('|');
			
			format.setFields(new int[] {0, 3, 7}, new Class<?>[] {Integer.class, Integer.class, Integer.class});
			
			
			format.configure(new Configuration());
			format.open(split);
			
			Tuple3<Integer, Integer, Integer> result = new Tuple3<Integer, Integer, Integer>();
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(111), result.f0);
			assertEquals(Integer.valueOf(444), result.f1);
			assertEquals(Integer.valueOf(888), result.f2);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(000), result.f0);
			assertEquals(Integer.valueOf(777), result.f1);
			assertEquals(Integer.valueOf(333), result.f2);
			
			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}
	
	@Test
	public void testReadSparseWithMask() throws IOException {
		try {
			final String fileContent = "111|222|333|444|555|666|777|888|999|000|\n000|999|888|777|666|555|444|333|222|111|";
			final FileInputSplit split = createTempFile(fileContent);	
			
			final CsvInputFormat<Tuple3<Integer, Integer, Integer>> format = new CsvInputFormat<Tuple3<Integer, Integer, Integer>>(PATH);
			
			format.setFieldDelimiter('|');

			format.setFields(new boolean[] { true, false, false, true, false, false, false, true }, new Class<?>[] { Integer.class,
					Integer.class, Integer.class });
			
			format.configure(new Configuration());
			format.open(split);
			
			Tuple3<Integer, Integer, Integer> result = new Tuple3<Integer, Integer, Integer>();
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(111), result.f0);
			assertEquals(Integer.valueOf(444), result.f1);
			assertEquals(Integer.valueOf(888), result.f2);
			
			result = format.nextRecord(result);
			assertNotNull(result);
			assertEquals(Integer.valueOf(000), result.f0);
			assertEquals(Integer.valueOf(777), result.f1);
			assertEquals(Integer.valueOf(333), result.f2);
			
			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	@Test
	public void testDateParser() throws IOException {
		try {
			final String fileContent = "03-21-2012|2012-13-02|2012-03-02\n04-21-1963|1970-13-08|2012-03-22\n";
			final FileInputSplit split = createTempFile(fileContent);

			final CsvInputFormat<Tuple3<DateValue, DateValue, DateValue>> format = new CsvInputFormat<Tuple3<DateValue, DateValue, DateValue>>(PATH);

			format.setFieldDelimiter('|');
			format.setFieldTypes(DateValue.class, DateValue.class, DateValue.class);

			format.configure(new Configuration());
			format.open(split);
			DateParser zeroParser = (DateParser) format.getFieldParser(0);
			zeroParser.setFormat("MM-dd-yyyy");
			DateParser oneParser = (DateParser) format.getFieldParser(1);
			oneParser.setFormat("yyyy-dd-MM");

			Tuple3<DateValue, DateValue, DateValue> result = new Tuple3<DateValue, DateValue, DateValue>();

			result = format.nextRecord(result);
			assertNotNull(result);
			assertTrue("f0="+result.f0.toString(), new DateValue("03-21-2012", "MM-dd-yyyy").asLong() == (result.f0.asLong()));
			assertTrue("f1=" + result.f1.toString(), new DateValue("2012-13-02", "yyyy-dd-MM").asLong() == (result.f1.asLong()));
			assertTrue("f2="+result.f2.toString(), new DateValue("2012-03-02").asLong() == (result.f2.asLong()));

			result = format.nextRecord(result);
			assertNotNull(result);
			assertTrue("f0="+result.f0.toString(), new DateValue("04-21-1963", "MM-dd-yyyy").asLong() == (result.f0.asLong()));
			assertTrue("f1="+result.f1.toString(), new DateValue("1970-13-08", "yyyy-dd-MM").asLong() == (result.f1.asLong()));
			assertTrue("f2="+result.f2.toString(), new DateValue("2012-03-22").asLong() == (result.f2.asLong()));

			result = format.nextRecord(result);
			assertNull(result);
			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	@Test
	public void testReadSparseWithShuffledPositions() throws IOException {
		try {
			final CsvInputFormat<Tuple3<Integer, Integer, Integer>> format = new CsvInputFormat<Tuple3<Integer, Integer, Integer>>(PATH);
			
			format.setFieldDelimiter('|');
			
			try {
				format.setFields(new int[] {8, 1, 3}, new Class<?>[] {Integer.class, Integer.class, Integer.class});
				fail("Input sequence should have been rejexted.");
			}
			catch (IllegalArgumentException e) {
				// that is what we want
			}
			
//			format.configure(new Configuration());
//			format.open(split);
//			
//			Tuple3<Integer, Integer, Integer> result = new Tuple3<Integer, Integer, Integer>();
//			
//			result = format.nextRecord(result);
//			assertNotNull(result);
//			assertEquals(Integer.valueOf(999), result.f0);
//			assertEquals(Integer.valueOf(222), result.f1);
//			assertEquals(Integer.valueOf(444), result.f2);
//			
//			result = format.nextRecord(result);
//			assertNotNull(result);
//			assertEquals(Integer.valueOf(222), result.f0);
//			assertEquals(Integer.valueOf(999), result.f1);
//			assertEquals(Integer.valueOf(777), result.f2);
//			
//			result = format.nextRecord(result);
//			assertNull(result);
//			assertTrue(format.reachedEnd());
		}
		catch (Exception ex) {
			fail("Test failed due to a " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}
	
	private FileInputSplit createTempFile(String content) throws IOException {
		File tempFile = File.createTempFile("test_contents", "tmp");
		tempFile.deleteOnExit();
		
		FileWriter wrt = new FileWriter(tempFile);
		wrt.write(content);
		wrt.close();
			
		return new FileInputSplit(0, new Path(tempFile.toURI().toString()), 0, tempFile.length(), new String[] {"localhost"});
	}

}
