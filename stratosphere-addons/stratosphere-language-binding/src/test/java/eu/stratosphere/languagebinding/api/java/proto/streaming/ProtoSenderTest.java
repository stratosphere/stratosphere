/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ********************************************************************************************************************/
package eu.stratosphere.languagebinding.api.java.proto.streaming;

import eu.stratosphere.api.java.tuple.Tuple9;
import eu.stratosphere.languagebinding.api.java.proto.ProtocolTuple.ProtoTuple;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ProtoSenderTest {
	private class TestObject {
		@Override
		public String toString() {
			return "testobject";
		}
	}

	/**
	 * Test of convertTuple method, of class ProtoSender.
	 */
	@Test
	public void testConvertTuple() {
		Tuple9 t = new Tuple9();
		t.setField(true, 0);
		t.setField((byte) 1, 1);
		t.setField((double) 1.2, 2);
		t.setField((float) 1.1, 3);
		t.setField((int) 3, 4);
		t.setField((long) 4, 5);
		t.setField((short) 2, 6);
		t.setField("test", 7);
		t.setField(new TestObject(), 8);

		ProtoTuple pt = new ProtoSender(null, null, null).convertTuple(t, 0);

		assertEquals(true, pt.getValues(0).getBoolVal());
		assertEquals((byte) 1, (byte) pt.getValues(1).getInt32Val());
		assertEquals((double) 1.2, pt.getValues(2).getDoubleVal(), 0.0001);
		assertEquals((float) 1.1, pt.getValues(3).getFloatVal(), 0.0001);
		assertEquals((int) 3, pt.getValues(4).getInt32Val());
		assertEquals((long) 4, pt.getValues(5).getInt64Val());
		assertEquals((short) 2, (short) pt.getValues(6).getInt32Val());
		assertEquals("test", pt.getValues(7).getStringVal());
		assertEquals("testobject", pt.getValues(8).getStringVal());
	}
}
