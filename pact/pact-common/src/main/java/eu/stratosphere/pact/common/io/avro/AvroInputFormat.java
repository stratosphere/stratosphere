package eu.stratosphere.pact.common.io.avro;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

import eu.stratosphere.nephele.fs.FileInputSplit;
import eu.stratosphere.pact.common.io.FileInputFormat;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.Value;
import eu.stratosphere.pact.common.type.base.PactBoolean;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;

/**
 * Input format to read Avro files.
 * 
 * The input format currently supports only flat avro schemas. So
 * there is no support for complex types except for nullable
 * primitve fields, e.g. ["string", null]
 * (See http://avro.apache.org/docs/current/spec.html#schema_complex)
 *
 */
public class AvroInputFormat extends FileInputFormat {

	private DataFileReader<GenericRecord> dataFileReader;
	private GenericRecord reuseAvroRecord = null;
	
	@Override
	public void open(FileInputSplit split) throws IOException {
		super.open(split);
		DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>();
		dataFileReader = new DataFileReader<GenericRecord>(new FSDataInputStreamWrapper(stream, (int)split.getLength()), datumReader);
		
	}
	@Override
	public boolean reachedEnd() throws IOException {
		return !dataFileReader.hasNext();
	}

	@Override
	public boolean nextRecord(PactRecord record) throws IOException {
		if(!dataFileReader.hasNext()) {
			return false;
		}
		if(record == null){
			throw new IllegalArgumentException("Empty PactRecord given");
		}
		reuseAvroRecord = dataFileReader.next(reuseAvroRecord);
		List<Field> fields = reuseAvroRecord.getSchema().getFields();
		for(Field field : fields) {
			final Value value = convertAvroToPactValue(field, reuseAvroRecord.get(field.pos()));
			record.setField(field.pos(), value);
		}
		return true;
	}
	/**
	 * Converts an Avro GenericRecord to a Value.
	 * @return
	 */
	private final Value convertAvroToPactValue(Field field, Object avroRecord) {
		if(avroRecord == null) {
			return null;
		}
		Type type = checkTypeConstraintsAndGetType(field.schema());
		switch(type) {
			case STRING:
				return new PactString((CharSequence) avroRecord);
			case INT:
				return new PactInteger((Integer) avroRecord);
			case BOOLEAN:
				return new PactBoolean((Boolean) avroRecord) ;
			default:
				throw new RuntimeException("Implement type "+type+" for AvroInputFormat!");
		}
	}
	
	private final Type checkTypeConstraintsAndGetType(Schema schema) {
		Type type = schema.getType();
		if(type == Type.ARRAY || type == Type.ENUM || type == Type.RECORD || type == Type.MAP ) {
			throw new RuntimeException("The given Avro file contains complex data types");
		}
		
		if( type == Type.UNION) {
			List<Schema> types = schema.getTypes();
			if(types.size() > 2) {
				throw new RuntimeException("The given Avro file contains a union that has more than two elements");
			}
			if(types.get(0).getType() == Type.UNION || types.get(1).getType() == Type.UNION ) {
				throw new RuntimeException("The given Avro file contains a nested union");
			}
			if(types.get(0).getType() == Type.NULL) {
				return types.get(1).getType();
			} else {
				if(types.get(1).getType() != Type.NULL) {
					throw new RuntimeException("The given Avro file is contains a union with two non-null types.");
				}
				return types.get(0).getType();
			}
		}
		return type;
	}

}
