package eu.stratosphere.language.binding.java;

import java.io.InputStream;

import eu.stratosphere.language.binding.protos.StratosphereRecordProtoBuffers.ProtoRecordSize;
import eu.stratosphere.language.binding.protos.StratosphereRecordProtoBuffers.ProtoStratosphereRecord;
import eu.stratosphere.language.binding.protos.StratosphereRecordProtoBuffers.ProtoStratosphereRecord.ProtoValue;
import eu.stratosphere.types.IntValue;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.types.Value;
import eu.stratosphere.util.Collector;

public class RecordReceiver {
	
	private InputStream inStream;
	
	public RecordReceiver(InputStream inStream){
		this.inStream = inStream;
	}
	
	public void receive(Collector<Record> collector) throws Exception{
		int size;
		while( (size = getSize()) != ProtobufTupleStreamer.SIGNAL_ALL_CALLS_DONE){
			byte[] buffer = new byte[size];
			inStream.read(buffer);
			ProtoStratosphereRecord psr = ProtoStratosphereRecord.parseFrom(buffer);
			Record record = getRecord(psr);
			collector.collect(record);
		}	
	}
	
	private Record getRecord(ProtoStratosphereRecord psr) {
		Record result = new Record(psr.getValuesCount());
		
		for( int i = 0; i < psr.getValuesCount(); i++){
			ProtoValue protoVal = psr.getValues(i);
			Value val = null;
			switch(protoVal.getValueType()){
				case StringValue: 
					val = new StringValue(protoVal.getStringVal());
					break;
				case IntegerValue32: 
					val = new IntValue(protoVal.getInt32Val());
					break;
				default:
					throw new RuntimeException("Any not implemented ProtoValType received");
			}
			result.setField(i, val);
		}
		
		return result;
	}
	
	public int getSize() throws Exception{
		byte[] buf = new byte[5];
		inStream.read(buf);
		ProtoRecordSize size = ProtoRecordSize.parseFrom(buf);
		return size.getValue();
	}
}
