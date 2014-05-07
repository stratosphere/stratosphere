package eu.stratosphere.api.java.typeutils.runtime;

import java.io.IOException;

import org.apache.hadoop.io.Writable;

import eu.stratosphere.api.common.typeutils.TypeComparator;
import eu.stratosphere.core.memory.DataInputView;
import eu.stratosphere.core.memory.DataOutputView;
import eu.stratosphere.core.memory.MemorySegment;
import eu.stratosphere.util.InstantiationUtil;

public class WritableComparator<T extends Writable & Comparable<T>> extends TypeComparator<T> {
	
	private static final long serialVersionUID = 1L;
	
	private Class<T> typeClass;
	
	private transient T reference;
	
	private final boolean ascendingComparison;
	
	public WritableComparator(Class<T> typeClass, boolean ascending) {
		this.ascendingComparison = ascending;
		this.typeClass = typeClass;
	}
	
	@Override
	public int hash(T record) {
		return record.hashCode();
	}
	
	@Override
	public void setReference(T toCompare) {
		this.reference = toCompare;
	}
	
	@Override
	public boolean equalToReference(T candidate) {
		return candidate.equals(reference);
	}
	
	@Override
	public int compareToReference(TypeComparator<T> referencedComparator) {
		int comp = ((WritableComparator<T>) referencedComparator).reference.compareTo(reference);
		return ascendingComparison ? comp : -comp;
	}
	
	@Override
	public int compare(DataInputView firstSource, DataInputView secondSource) throws IOException {
		T wc1 = InstantiationUtil.instantiate(typeClass, Writable.class);
		T wc2 = InstantiationUtil.instantiate(typeClass, Writable.class);
		
		wc1.readFields(firstSource);
		wc2.readFields(firstSource);
		
		int comp = wc1.compareTo(wc2);
		return ascendingComparison ? comp : -comp;
	}
	
	@Override
	public boolean supportsNormalizedKey() {
		return false;
	}
	
	@Override
	public boolean supportsSerializationWithKeyNormalization() {
		return false;
	}
	
	@Override
	public int getNormalizeKeyLen() {
		return 0;
	}
	
	@Override
	public boolean isNormalizedKeyPrefixOnly(int keyBytes) {
		return true;
	}
	
	@Override
	public void putNormalizedKey(T record, MemorySegment target, int offset, int numBytes) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void writeWithKeyNormalization(T record, DataOutputView target) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public T readWithKeyDenormalization(T reuse, DataInputView source) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean invertNormalizedKey() {
		return !ascendingComparison;
	}
	
	@Override
	public TypeComparator<T> duplicate() {
		throw new UnsupportedOperationException();
	}
	
}
