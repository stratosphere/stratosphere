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
import eu.stratosphere.languagebinding.api.java.proto.ProtocolTuple;
import eu.stratosphere.languagebinding.api.java.proto.ProtocolTuple.ProtoTuple;
import eu.stratosphere.languagebinding.api.java.proto.ProtocolTuple.ProtoTuple.FieldType;
import static eu.stratosphere.languagebinding.api.java.proto.streaming.ProtoSignals.PROTO_SIGNAL_DONE;
import eu.stratosphere.languagebinding.api.java.streaming.Converter;
import eu.stratosphere.languagebinding.api.java.streaming.Receiver;
import eu.stratosphere.util.Collector;
import java.io.IOException;
import java.io.InputStream;

/**
 * Receiver to receive tuples via google protocol buffers.
 *
 * type(flag) conversion table (PB -> Java) 
 * bool			-> bool 
 * int32(byte)	-> byte 
 * int32(short) -> short 
 * int32		-> int
 * int64		-> long 
 * float		-> float 
 * double		-> double 
 * string		-> string
 */
public class ProtoReceiver extends Receiver {
	private FieldType[] classes = null;
	private Converter converter;
	private AbstractFunction function;

	public ProtoReceiver(AbstractFunction function, InputStream inStream) {
		super(inStream);
		this.function = function;
	}

	public ProtoReceiver(AbstractFunction function, InputStream inStream, Converter converter) {
		this(function, inStream);
		this.converter = converter;
	}

	@Override
	public Object receiveRecord() throws IOException {
		int size = readSize();
		if (size == PROTO_SIGNAL_DONE) {
			return null;
		}
		ProtocolTuple.ProtoTuple pt = receive(size);
		return convertProtoNormal(pt);
	}

	/**
	 Receives a record, extracting the classes for each record without applying converters.
	 @return record
	 @throws IOException 
	 */
	public Object receiveSpecialRecord() throws IOException {
		int size = readSize();
		if (size == PROTO_SIGNAL_DONE) {
			return null;
		}
		ProtocolTuple.ProtoTuple pt = receive(size);
		return convertProtoSpecial(pt);
	}

	@Override
	public void receiveRecords(Collector collector) throws IOException {
		int size;
		while ((size = readSize()) != PROTO_SIGNAL_DONE) {
			ProtocolTuple.ProtoTuple pt = receive(size);
			collector.collect(convertProtoNormal(pt));
		}
	}

	private ProtocolTuple.ProtoTuple receive(int size) throws IOException {
		byte[] buffer = new byte[size];
		inStream.read(buffer, 0, buffer.length);
		return ProtocolTuple.ProtoTuple.parseFrom(buffer);
	}

	private int readSize() throws IOException {
		byte[] buf = new byte[5];
		inStream.read(buf);
		ProtocolTuple.TupleSize size = ProtocolTuple.TupleSize.parseFrom(buf);
		return size.getValue();
	}

	protected Object convertProtoNormal(ProtoTuple pt) {
		if (classes == null) {
			classes = new FieldType[pt.getValuesCount()];
			for (int x = 0; x < classes.length; x++) {
				classes[x] = pt.getValues(x).getFieldType();
			}
		}
		return convertProto(pt, classes, true);
	}

	protected Object convertProtoSpecial(ProtoTuple pt) {
		return convertProto(pt, null, false);
	}

	protected Object convertProto(ProtocolTuple.ProtoTuple pt, FieldType[] classes, boolean useConverter) {
		if (classes == null) {
			classes = new FieldType[pt.getValuesCount()];
			for (int x = 0; x < classes.length; x++) {
				classes[x] = pt.getValues(x).getFieldType();
			}
		}
		int fieldCount = pt.getValuesCount();
		Tuple tuple = createTuple(fieldCount);
		for (int x = 0; x < fieldCount; x++) {
			switch (classes[x]) {
				case String:
					tuple.setField(pt.getValues(x).getStringVal(), x);
					break;
				case Byte:
					tuple.setField((byte) pt.getValues(x).getInt32Val(), x);
					break;
				case Short:
					tuple.setField((short) pt.getValues(x).getInt32Val(), x);
					break;
				case Integer:
					tuple.setField(pt.getValues(x).getInt32Val(), x);
					break;
				case Long:
					tuple.setField(pt.getValues(x).getInt64Val(), x);
					break;
				case Float:
					tuple.setField((float) pt.getValues(x).getFloatVal(), x);
					break;
				case Double:
					tuple.setField(pt.getValues(x).getDoubleVal(), x);
					break;
				case Boolean:
					tuple.setField(pt.getValues(x).getBoolVal(), x);
					break;
			}
		}
		if (useConverter) {
			if (pt.getAtomicType()) {
				return converter == null
						? tuple.getField(0)
						: converter.convert(tuple.getField(0));
			} else {
				return converter == null
						? tuple
						: converter.convert(tuple);
			}
		} else {
			if (pt.getAtomicType()) {
				return tuple.getField(0);
			} else {
				return tuple;
			}
		}
	}
}
