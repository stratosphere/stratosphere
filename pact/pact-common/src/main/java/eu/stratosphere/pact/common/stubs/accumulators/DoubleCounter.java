package eu.stratosphere.pact.common.stubs.accumulators;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class DoubleCounter implements SimpleAccumulator<Double> {

  private double localValue = 0;
  
  @Override
  public void add(Double value) {
    localValue += value;
  }

  @Override
  public Double getLocalValue() {
    return localValue;
  }
  
  @Override
  public void merge(Accumulator<?, ?> other) {
    // TODO Remove unknowns
  	AccumulatorHelper.compareAccumulatorTypes("unknown", this.getClass(), other.getClass());
  	this.localValue += ((DoubleCounter)other).getLocalValue();
  }

  @Override
  public void resetLocal() {
    this.localValue = 0;
  }

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(localValue);
	}

	@Override
	public void read(DataInput in) throws IOException {
		this.localValue = in.readDouble();
	}

}
