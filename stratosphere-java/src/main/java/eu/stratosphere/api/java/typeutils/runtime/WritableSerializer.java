package eu.stratosphere.api.java.typeutils.runtime;

import java.io.IOException;

import org.apache.hadoop.io.Writable;

import eu.stratosphere.api.common.typeutils.TypeSerializer;
import eu.stratosphere.core.memory.DataInputView;
import eu.stratosphere.core.memory.DataOutputView;
import eu.stratosphere.util.InstantiationUtil;

public class WritableSerializer<T extends Writable> extends TypeSerializer<T> {
	
	private static final long serialVersionUID = 1L;
	
	private final Class<T> typeClass;
	
	public WritableSerializer(Class<T> typeClass) {
		this.typeClass = typeClass;
	}

	@Override
	public T createInstance() {
		return InstantiationUtil.instantiate(typeClass, Writable.class);
	}

	@Override
	public T copy(T from, T reuse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLength() {
		return -1;
	}

	@Override
	public void serialize(T record, DataOutputView target) throws IOException {
		Writable w = (Writable) record;
		w.write(target);		
	}

	@Override
	public T deserialize(T reuse, DataInputView source) throws IOException {
		Writable w = (Writable) reuse;
		w.readFields(source);
		return reuse;
	}

	@Override
	public void copy(DataInputView source, DataOutputView target) throws IOException {
		Writable w = createInstance();
		w.readFields(source);
		w.write(target);
	}

	@Override
	public boolean isImmutableType() {
		return true;
	}

	@Override
	public boolean isStateful() {
		return false;
	}
}
