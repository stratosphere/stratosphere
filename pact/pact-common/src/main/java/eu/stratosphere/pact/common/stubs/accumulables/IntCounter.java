package eu.stratosphere.pact.common.stubs.accumulables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class IntCounter implements Accumulator<Integer> {

  private int localValue = 0;
  
  @Override
  public void add(Integer value) {
    localValue += value;
  }

  @Override
  public Integer getLocalValue() {
    return localValue;
  }
  
  @Override
  public void merge(Accumulable<?, ?> other) {
  	// TODO Remove unknowns
  	AccumulatorHelper.compareAccumulatorTypes("unknown", this.getClass(), other.getClass());
  	this.localValue += ((IntCounter)other).getLocalValue();
  }

//  @Override
//  public Integer merge(Accumulable<Integer, Integer> other) {
//    return this.localValue + other.getLocalValue();
//  }

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(localValue);
	}

	@Override
	public void read(DataInput in) throws IOException {
		localValue = in.readInt();
	}

	@Override
	public void resetLocal() {
		this.localValue = 0;
	}

}
