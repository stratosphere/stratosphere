package eu.stratosphere.pact.common.stubs.accumulables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;


/**
 * Histogram for discrete-data. Let's you populate a histogram distributedly.
 * Implemented as a Integer->Integer HashMap
 * 
 * Could be extended to continuous values later, but then we need to dynamically
 * decide about the bin size in an online algorithm (or ask the user)
 */
public class Histogram implements Accumulable<Integer, Map<Integer, Integer>> {

	private Map<Integer, Integer> hashMap = Maps.newHashMap();

	@Override
	public void add(Integer value) {
		Integer current = hashMap.get(value);
		Integer newValue = value;
		if (current != null) {
			newValue = current + newValue;
		}
		this.hashMap.put(value, newValue);
	}

	@Override
	public Map<Integer, Integer> getLocalValue() {
		return this.hashMap;
	}

	@Override
	public void resetLocal() {
		this.hashMap.clear();
	}

	/**
	 * TODO Write test case
	 */
	@Override
	public void merge(Accumulable<?, ?> other) {
		AccumulatorHelper.compareAccumulatorTypes("unknown", this.getClass(), other.getClass());
		// Merge the values into this map
		for (Map.Entry<Integer, Integer> entryFromOther : ((Histogram)other).getLocalValue()
				.entrySet()) {
			Integer ownValue = this.hashMap.get(entryFromOther.getKey());
			if (ownValue == null) {
				this.hashMap.put(entryFromOther.getKey(), entryFromOther.getValue());
			} else {
				this.hashMap.put(entryFromOther.getKey(), entryFromOther.getValue()
						+ ownValue);
			}
		}
	}
//	@Override
//	public Accumulable<Integer, Map<Integer, Integer>> merge(
//			Accumulable<Integer, Map<Integer, Integer>> other) {
//		// Merge the values into this map
//		for (Map.Entry<Integer, Integer> entryFromOther : other.getLocalValue()
//				.entrySet()) {
//			Integer ownValue = this.hashMap.get(entryFromOther.getKey());
//			if (ownValue == null) {
//				this.hashMap.put(entryFromOther.getKey(), entryFromOther.getValue());
//			} else {
//				this.hashMap.put(entryFromOther.getKey(), entryFromOther.getValue()
//						+ ownValue);
//			}
//		}
//
//		return this;
//	};
	
	
	@Override
	public void write(DataOutput out) throws IOException {
		// TODO See SerializableHashMap
	}

	@Override
	public void read(DataInput in) throws IOException {
		// TODO See SerializableHashMap
	}

}
