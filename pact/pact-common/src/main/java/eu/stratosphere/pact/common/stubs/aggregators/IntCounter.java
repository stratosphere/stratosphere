package eu.stratosphere.pact.common.stubs.aggregators;

public class IntCounter implements Accumulator<Integer> {

  private int localValue = 0;
  
  @Override
  public void add(Integer value) {
    localValue += value;
//    System.out.println("New value: " + localValue);
  }

  @Override
  public Integer getLocalValue() {
    return localValue;
  }

  @Override
  public Integer merge(Accumulator<Integer> other) {
    return this.localValue + other.getLocalValue();
  }

}
