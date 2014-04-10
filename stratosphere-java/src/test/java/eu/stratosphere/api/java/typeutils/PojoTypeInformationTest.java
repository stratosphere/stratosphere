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

package eu.stratosphere.api.java.typeutils;

import eu.stratosphere.api.java.record.io.CsvInputFormat;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.configuration.IllegalConfigurationException;
import eu.stratosphere.core.fs.FileInputSplit;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.types.IntValue;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.LogUtils;
import junit.framework.Assert;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class PojoTypeInformationTest {

	static class SimplePojo {
		String str;
		Boolean Bl;
		boolean bl;
		Byte Bt;
		byte bt;
		Short Shrt;
		short shrt;
		Integer Intgr;
		int intgr;
		Long Lng;
		long lng;
		Float Flt;
		float flt;
		Double Dbl;
		double dbl;
		Character Ch;
		char ch;
	}

	@Test
	public void testSimplePojoTypeExtraction() {
		TypeInformation<SimplePojo> type = TypeExtractor.getForClass(SimplePojo.class);
		assertTrue("Extracted type is not a Pojo type but should be.", type instanceof PojoTypeInfo);
	}

	static class SimpleNonPojo {
		int[] array;
	}

	@Test
	public void testSimpleNonPojoTypeExtraction() {
		TypeInformation<SimpleNonPojo> type = TypeExtractor.getForClass(SimpleNonPojo.class);
		assertFalse("Extracted type is a Pojo type but should not be.", type instanceof PojoTypeInfo);
	}

	static class NestedPojoInner {
		private String field;
	}

	static class NestedPojoOuter {
		private Integer intField;
		NestedPojoInner inner;
	}

	@Test
	public void testNestedPojoTypeExtraction() {
		TypeInformation<NestedPojoOuter> type = TypeExtractor.getForClass(NestedPojoOuter.class);
		assertTrue("Extracted type is not a Pojo type but should be.", type instanceof PojoTypeInfo);
	}

	static class Recursive1Pojo {
		private Integer intField;
		Recursive2Pojo rec;
	}

	static class Recursive2Pojo {
		private String strField;
		Recursive1Pojo rec;
	}

	@Test
	public void testRecursivePojoTypeExtraction() {
		TypeInformation<Recursive1Pojo> type = TypeExtractor.getForClass(Recursive1Pojo.class);
		assertFalse("Extracted type is a Pojo type but should not be.", type instanceof PojoTypeInfo);
	}
}
