package eu.stratosphere.pact.common.stubs.accumulables;

import eu.stratosphere.pact.common.type.Value;

/**
 * Interface for custom accumulable objects. Data are written to in a UDF,
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
 *          Type of the value to add (e.g. a integer)
 * @param <A>
 *          Type of the final accumulable object (e.g. discrete histogram)
 */
public interface Accumulable<V, A> extends Value {

	/**
	 * @param value
	 *          to add to the accumulator object
	 */
	void add(V value);

	/**
	 * @return local value from the current UDF context
	 */
	A getLocalValue();

	/**
	 * Reset value locally. This only affects the current UDF context.
	 */
	void resetLocal();

	/**
	 * Internally used by system to merge the collected parts of an accumulator at
	 * the end of the job. It makes an check internally whether the accumulator type
	 * is the same.
	 * 
	 * TODO Return A???
	 * 
	 * TODO Accumulable<V, A> other would be nicer, but can't merge then generically!
	 * 
	 * @param other
	 *          reference to accumulator to merge in
	 * @return Reference to this (for efficiency), after data from other were
	 *         merged in
	 */
	void merge(Accumulable<?, ?> other);
//	A merge(Accumulable<V, A> other);

}
