package eu.stratosphere.nephele.jobmanager.accumulators;

import java.util.HashMap;
import java.util.Map;

import eu.stratosphere.nephele.services.accumulators.Accumulator;
import eu.stratosphere.nephele.services.accumulators.AccumulatorHelper;
import eu.stratosphere.nephele.types.StringRecord;

/**
 * Simple class wrapping a map of accumulators for a single job. Just for
 * better handling.
 */
public class JobAccumulators {
  
  private final Map<String, Accumulator<?, ?>> accumulators = new HashMap<String, Accumulator<?, ?>>();
  
  public Map<String, Accumulator<?, ?>> getAccumulators() {
    return this.accumulators;
  }
  
  public void processNew(Map<StringRecord, Accumulator<?, ?>> newAccumulators) {
    AccumulatorHelper.mergeIntoSerializable(this.accumulators, newAccumulators);
  }
}