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

import eu.stratosphere.api.java.tuple.Tuple8;
import eu.stratosphere.languagebinding.api.java.proto.ProtocolTuple.ProtoTuple;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ProtoReceiverTest {
	@Test
	public void testConvertProto() {
		ProtoTuple.Builder ptb = ProtoTuple.newBuilder();
		ptb.setAtomicType(false);
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.Boolean)
				.setBoolVal(true));
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.Byte)
				.setInt32Val((byte) 1));
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.Double)
				.setDoubleVal((double) 1.2));
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.Float)
				.setFloatVal((float) 1.1));
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.Integer)
				.setInt32Val((int) 3));
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.Long)
				.setInt64Val((long) 4));
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.Short)
				.setInt32Val((short) 2));
		ptb.addValues(ProtoTuple.FieldValue.newBuilder()
				.setFieldType(ProtoTuple.FieldType.String)
				.setStringVal("test"));
		ProtoTuple pt = ptb.build();

		Tuple8 t = (Tuple8) new ProtoReceiver(null, null, null).convertProtoNormal(pt);

		assertEquals(true, t.getField(0));
		assertEquals((byte) 1, t.getField(1));
		assertEquals((double) 1.2, t.getField(2));
		assertEquals((float) 1.1, t.getField(3));
		assertEquals((int) 3, t.getField(4));
		assertEquals((long) 4, t.getField(5));
		assertEquals((short) 2, t.getField(6));
		assertEquals("test", t.getField(7));
	}
}
