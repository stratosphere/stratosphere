package eu.stratosphere.pact.common.stubs.aggregators;

public class DoubleCounter implements Accumulator<Double> {

  private double localValue = 0;
  
  @Override
  public void add(Double value) {
    localValue += value;
    System.out.println("New value: " + localValue);
  }

  @Override
  public Double getLocalValue() {
    return localValue;
  }

  @Override
  public Double merge(Accumulator<Double> other) {
    return this.localValue + other.getLocalValue();
  }

}
