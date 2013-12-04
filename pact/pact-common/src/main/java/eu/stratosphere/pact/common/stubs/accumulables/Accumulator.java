package eu.stratosphere.pact.common.stubs.accumulables;


/**
 * Similar to Accumulable, but the type of items to add and the result value
 * must be the same.
 */
public interface Accumulator<T> extends Accumulable<T,T> {

}
