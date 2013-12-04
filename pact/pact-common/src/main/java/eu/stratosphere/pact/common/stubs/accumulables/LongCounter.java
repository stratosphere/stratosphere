package eu.stratosphere.pact.common.stubs.accumulables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class LongCounter implements Accumulator<Long> {

  private long localValue = 0;
  
  @Override
  public void add(Long value) {
    this.localValue += value;
  }

  @Override
  public Long getLocalValue() {
    return this.localValue;
  }

  @Override
  public void merge(Accumulable<?, ?> other) {
  	AccumulatorHelper.compareAccumulatorTypes("unknown", this.getClass(), other.getClass());
  	this.localValue += ((LongCounter)other).getLocalValue();
  }

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(this.localValue);
	}

	@Override
	public void read(DataInput in) throws IOException {
		this.localValue = in.readLong();
	}

	@Override
	public void resetLocal() {
		this.localValue = 0;
	}

}
