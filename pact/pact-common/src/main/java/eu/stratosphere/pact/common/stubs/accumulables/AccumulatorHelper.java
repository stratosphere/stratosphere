package eu.stratosphere.pact.common.stubs.accumulables;

import java.util.Map;

public class AccumulatorHelper {
	
	/**
	 * Compare both classes, throw {@link UnsupportedOperationException} if they differ
	 */
	public static void compareAccumulatorTypes(String name,
			@SuppressWarnings("rawtypes") Class<? extends Accumulable> first,
			@SuppressWarnings("rawtypes") Class<? extends Accumulable> second)
			throws UnsupportedOperationException {
		if (first != second) {
			throw new UnsupportedOperationException("The accumulator object '" 
					+ name + "' was created with two different types: " + first + " and " + second);
		}
	}

	/**
	 * Merge two collections of accumulable objects. The second will be merged
	 * into the first.
	 * 
	 * @param accumulables
	 *          The collection of accumulators that will be updated
	 * @param toMerge
	 *          The collection of accumulators that will be merged into the other
	 */
	public static void mergeInto(Map<String, Accumulable<?, ?>> accumulables,
			Map<String, Accumulable<?, ?>> toMerge) {
		for (Map.Entry<String, Accumulable<?,?>> otherEntry : toMerge.entrySet()) {
			Accumulable<?,?> ownAccumulator = accumulables.get(otherEntry.getKey());
			if (ownAccumulator == null) {
				// Take over counter from chained task
				accumulables.put(otherEntry.getKey(), otherEntry.getValue());
			} else {
				// Both should have the same type
				AccumulatorHelper.compareAccumulatorTypes(otherEntry.getKey(),
						ownAccumulator.getClass(), otherEntry.getValue().getClass());
				
				// Merge counter from chained task into counter from stub
				ownAccumulator.merge(otherEntry.getValue());
			}
		}
	}
}
