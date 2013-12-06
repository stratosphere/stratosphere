package eu.stratosphere.nephele.services.accumulators;

import java.util.Map;


public class AccumulatorHelper {
	
	/**
	 * Compare both classes and throw {@link UnsupportedOperationException} if they differ
	 */
	public static void compareAccumulatorTypes(String name,
			@SuppressWarnings("rawtypes") Class<? extends Accumulator> first,
			@SuppressWarnings("rawtypes") Class<? extends Accumulator> second)
			throws UnsupportedOperationException {
		if (first != second) {
			throw new UnsupportedOperationException("The accumulator object '" 
					+ name + "' was created with two different types: " + first + " and " + second);
		}
	}

	/**
	 * Merge two collections of accumulators. The second will be merged
	 * into the first.
	 * 
	 * @param target
	 *          The collection of accumulators that will be updated
	 * @param toMerge
	 *          The collection of accumulators that will be merged into the other
	 */
	public static void mergeInto(Map<String, Accumulator<?, ?>> target,
			Map<String, Accumulator<?, ?>> toMerge) {
		for (Map.Entry<String, Accumulator<?,?>> otherEntry : toMerge.entrySet()) {
			Accumulator<?,?> ownAccumulator = target.get(otherEntry.getKey());
			if (ownAccumulator == null) {
				// Take over counter from chained task
				target.put(otherEntry.getKey(), otherEntry.getValue());
			} else {
				// Both should have the same type
				AccumulatorHelper.compareAccumulatorTypes(otherEntry.getKey(),
						ownAccumulator.getClass(), otherEntry.getValue().getClass());
				
				// Merge counter from chained task into counter from stub
				mergeSingle(ownAccumulator, otherEntry.getValue());
			}
		}
	}
	
	private static final <V, R> void mergeSingle(Accumulator<?, ?> target, Accumulator<?, ?> toMerge) {
	  @SuppressWarnings("unchecked")
	  Accumulator<V, R> typedTarget = (Accumulator<V, R>) target;
	  
	  @SuppressWarnings("unchecked")
	  Accumulator<V, R> typedToMerge = (Accumulator<V, R>) toMerge;
	  
	  typedTarget.merge(typedToMerge);
	 }
}
