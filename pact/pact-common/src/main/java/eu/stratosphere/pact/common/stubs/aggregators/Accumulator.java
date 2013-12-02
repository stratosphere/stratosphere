package eu.stratosphere.pact.common.stubs.aggregators;

public interface Accumulator<T> {
  
  void add(T value);
  
  T getLocalValue();
  
  T merge(Accumulator<T> other);
  
}
