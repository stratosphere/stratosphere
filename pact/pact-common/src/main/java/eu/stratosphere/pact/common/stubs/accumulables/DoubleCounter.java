package eu.stratosphere.pact.common.stubs.accumulables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class DoubleCounter implements Accumulator<Double> {

  private double localValue = 0;
  
  @Override
  public void add(Double value) {
    localValue += value;
//    System.out.println("New value: " + localValue);
  }

  @Override
  public Double getLocalValue() {
    return localValue;
  }

//  @Override
//  public Double merge(Accumulator<Double> other) {
//    return this.localValue + other.getLocalValue();
//  }
  
  @Override
  public void merge(Accumulable<?, ?> other) {
  	AccumulatorHelper.compareAccumulatorTypes("unknown", this.getClass(), other.getClass());
  	this.localValue += ((DoubleCounter)other).getLocalValue();
  }

//	@Override
//	public Double merge(Accumulable<Double, Double> other) {
//    return this.localValue + other.getLocalValue();
//	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(localValue);
	}

	@Override
	public void read(DataInput in) throws IOException {
		this.localValue = in.readDouble();
	}

	@Override
	public void resetLocal() {
		this.localValue = 0;
	}

}
