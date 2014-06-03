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

import eu.stratosphere.api.common.functions.AbstractFunction;
import eu.stratosphere.api.java.tuple.Tuple;
import eu.stratosphere.api.java.tuple.Tuple1;
import eu.stratosphere.languagebinding.api.java.proto.ProtocolTuple;
import eu.stratosphere.languagebinding.api.java.proto.ProtocolTuple.ProtoTuple;
import static eu.stratosphere.languagebinding.api.java.proto.streaming.ProtoSignals.PROTO_SIGNAL_DONE;
import eu.stratosphere.languagebinding.api.java.streaming.Converter;
import eu.stratosphere.languagebinding.api.java.streaming.Sender;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Sender to send tuples via google protocol buffers.
 *
 * type(flag) conversion table (Java -> PB)
 * bool	  -> bool
 * byte	  -> int32(byte)
 * short  -> int32(short)
 * int	  -> int32
 * long   -> int64
 * float  -> float
 * double -> double
 * string -> string
 *   ?    -> string
 */
public class ProtoSender extends Sender {
	private final supportedInputTypes[][] classes;
	private Converter[] converter;
	private final AbstractFunction function;

	public ProtoSender(AbstractFunction function, OutputStream outStream) {
		super(outStream);
		classes = new supportedInputTypes[2][];
		this.function = function;
	}

	public ProtoSender(AbstractFunction function, OutputStream outStream, Converter[] converter) {
		this(function, outStream);
		this.converter = converter;
	}

	@Override
	public void sendRecord(Object tuple) throws IOException {
		sendRecord(tuple, 0);
	}

	@Override
	public void sendRecord(Object tuple, int group) throws IOException {
		ProtocolTuple.ProtoTuple pt = convertTuple(tuple, group);
		sendSize(pt.getSerializedSize());
		pt.writeTo(outStream);
		outStream.flush();
	}

	@Override
	public void sendCompletionSignal() throws IOException {
		sendSize(PROTO_SIGNAL_DONE);
	}

	private void sendSize(int serializedSize) throws IOException {
		ProtocolTuple.TupleSize size = buildTupleSize(serializedSize);
		size.writeTo(outStream);
		outStream.flush();
	}

	//------------------------Conversion--------------------------------------------------------------------------------
	private ProtocolTuple.TupleSize buildTupleSize(int serializedSize) {
		return ProtocolTuple.TupleSize.newBuilder()
				.setValue(serializedSize)
				.build();
	}

	protected <X> ProtocolTuple.ProtoTuple convertTuple(Object value, int group) {
		//convert value if desired
		X convertedValue;
		try {
			convertedValue = converter[group] == null
					? (X) value
					: (X) converter[group].convert(value);
		} catch (NullPointerException npe) {
			convertedValue = (X) value;
		} catch (IndexOutOfBoundsException iobe) {
			convertedValue = (X) value;
		}

		//encapsulate input(makes conversion code more compact)
		boolean atomicType;
		try {
			atomicType = !Tuple.class.isAssignableFrom(convertedValue.getClass());
		} catch (NullPointerException npe) {
			atomicType = true;
		}
		Tuple tuple = atomicType
				? new Tuple1(convertedValue)
				: (Tuple) convertedValue;

		//extract classes once
		if (classes[group] == null) {
			classes[group] = new supportedInputTypes[tuple.getArity()];
			for (int x = 0; x < classes[group].length; x++) {
				try {
					classes[group][x] = supportedInputTypes.valueOf(tuple.getField(x).getClass().getSimpleName().toUpperCase());
				} catch (IllegalArgumentException iae) {
					classes[group][x] = supportedInputTypes.OTHER;
				} catch (NullPointerException npe) {
					classes[group][x] = supportedInputTypes.NULL;
				}
			}
		}
		return convert(atomicType, tuple, classes, group);
	}

	private ProtoTuple convert(boolean atomicType, Tuple tuple, supportedInputTypes[][] classes, int group) {
		//convert to ProtoTuple
		ProtocolTuple.ProtoTuple.Builder ptb = ProtocolTuple.ProtoTuple.newBuilder();
		ptb.setAtomicType(atomicType);
		for (int x = 0; x < tuple.getArity(); x++) {
			switch (classes[group][x]) {
				case BOOLEAN:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.Boolean)
							.setBoolVal((Boolean) tuple.getField(x)));
					break;
				case BYTE:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.Byte)
							.setInt32Val(((Byte) tuple.getField(x)).intValue()));
					break;
				case SHORT:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.Short)
							.setInt32Val(((Short) tuple.getField(x)).intValue()));
					break;
				case INTEGER:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.Integer)
							.setInt32Val((Integer) tuple.getField(x)));
					break;
				case LONG:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.Long)
							.setInt64Val((Long) tuple.getField(x)));
					break;
				case STRING:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.String)
							.setStringVal((String) tuple.getField(x)));
					break;
				case FLOAT:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.Float)
							.setFloatVal((Float) tuple.getField(x)));
					break;
				case DOUBLE:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.Double)
							.setDoubleVal((Double) tuple.getField(x)));
					break;
				case OTHER:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.String)
							.setStringVal(tuple.getField(x).toString()));
					break;
				case NULL:
					ptb.addValues(ProtocolTuple.ProtoTuple.FieldValue.newBuilder()
							.setFieldType(ProtocolTuple.ProtoTuple.FieldType.String)
							.setStringVal(tuple.getField(x).toString()));
					break;
			}
		}
		return ptb.build();
	}

	private enum supportedInputTypes {

		BOOLEAN,
		BYTE,
		SHORT,
		INTEGER,
		LONG,
		FLOAT,
		DOUBLE,
		STRING,
		OTHER,
		NULL
	}
}
