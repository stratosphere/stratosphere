package eu.stratosphere.pact.generic.stub.accumulators;

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
public class Histogram implements Accumulator<Integer, Map<Integer, Integer>> {

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

	/**
	 * TODO Write test case
	 */
	@Override
	public void merge(Accumulator<?, ?> other) {
    // TODO Remove unknowns
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

  @Override
  public void resetLocal() {
    this.hashMap.clear();
  }
	
	@Override
	public void write(DataOutput out) throws IOException {
		// TODO See SerializableHashMap
	}

	@Override
	public void read(DataInput in) throws IOException {
		// TODO See SerializableHashMap
	}

}
