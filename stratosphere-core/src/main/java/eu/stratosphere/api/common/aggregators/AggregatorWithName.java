/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/
package eu.stratosphere.api.common.aggregators;

import eu.stratosphere.types.Value;

/**
 * Simple utility class holding an {@link Aggregator} with the name it is registered under.
 */
public class AggregatorWithName<T extends Value> {

	private final String name;
	
	private final Class<? extends Aggregator<T>> aggregator;

	/**
	 * Creates a new instance for the given aggregator and name.
	 * 
	 * @param name The name that the aggregator is registered under.
	 * @param aggregator The aggregator.
	 */
	public AggregatorWithName(String name, Class<Aggregator<T>> aggregator) {
		this.name = name;
		this.aggregator = aggregator;
	}
	
	/**
	 * Gets the name that the aggregator is registered under.
	 * 
	 * @return The name that the aggregator is registered under.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the aggregator.
	 * 
	 * @return The aggregator.
	 */
	public Class<? extends Aggregator<T>> getAggregator() {
		return aggregator;
	}
}
