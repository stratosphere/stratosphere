package eu.stratosphere.nephele.services.accumulators;

import java.io.Serializable;

import eu.stratosphere.nephele.io.IOReadableWritable;

/**
 * Interface for custom accumulator objects. Data are written to in a UDF,
 * merged by the system at the end of the job, and can be read at the end of the
 * job from the calling client. Inspired by Hadoop/MapReduce counters.
 * 
 * The type added to the object might differ from the type returned. This is the
 * case for a discrete HashMap: We add single integers, but the result is a
 * HashMap.
 * 
 * Note: I did not want that T requires to extend Value, since the user should
 * not work with wrappers in the UDF, but with the original data type.
 * 
 * @param <V>
 *          Type of values that are added to the accumulator
 * @param <R>
 *          Type of the accumulator result
 */
public interface Accumulator<V, R> extends IOReadableWritable, Serializable {

	/**
	 * @param value
	 *          to add to the accumulator object
	 */
	void add(V value);

	/**
	 * @return local value from the current UDF context
	 */
	R getLocalValue();

	/**
	 * Reset value locally. This only affects the current UDF context.
	 */
	void resetLocal();

  /**
   * Used by system internally to merge the collected parts of an accumulator at
   * the end of the job. It makes an check internally whether the accumulator
   * type is the same.
   * 
   * TODO 'Accumulator<V, A> other' would be nicer, but then we can't merge
   * generically (types would need to match exactly) ;-(
   * 
   * @param other
   *          reference to accumulator to merge in
   * @return Reference to this (for efficiency), after data from other were
   *         merged in
   */
	void merge(Accumulator<V, R> other);

}
