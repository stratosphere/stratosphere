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

package eu.stratosphere.nephele.instance.cluster;


import eu.stratosphere.nephele.instance.AbstractInstance;
import eu.stratosphere.nephele.instance.HardwareDescription;
import eu.stratosphere.nephele.instance.InstanceConnectionInfo;
import eu.stratosphere.nephele.topology.NetworkNode;
import eu.stratosphere.nephele.topology.NetworkTopology;

/**
 * Representation of a host of a compute cluster.
 * <p>
 * This class is thread-safe.
 * 
 */
class ClusterInstance extends AbstractInstance {
	/**
	 * Time when last heat beat has been received from the task manager running on this instance.
	 */
	private long lastReceivedHeartBeat = System.currentTimeMillis();

	/**
	 * Constructs a new cluster instance.
	 * 
	 * @param instanceConnectionInfo
	 *        the instance connection info identifying the host
	 * @param parentNode
	 *        the parent node of this node in the network topology
	 * @param networkTopology
	 *        the network topology this node is part of
	 * @param hardwareDescription
	 *        the hardware description reported by the instance itself
	 */
	public ClusterInstance(final InstanceConnectionInfo instanceConnectionInfo,
			final NetworkNode parentNode, final NetworkTopology networkTopology,
			final HardwareDescription hardwareDescription, int numberOfSlots) {

		super(instanceConnectionInfo, parentNode, networkTopology, hardwareDescription, numberOfSlots);
	}

	/**
	 * Updates the time of last received heart beat to the current system time.
	 */
	synchronized void reportHeartBeat() {
		this.lastReceivedHeartBeat = System.currentTimeMillis();
	}

	/**
	 * Returns whether the host is still alive.
	 * 
	 * @param cleanUpInterval
	 *        duration (in milliseconds) after which a host is
	 *        considered dead if it has no received heat-beats.
	 * @return <code>true</code> if the host has received a heat-beat before the <code>cleanUpInterval</code> duration
	 *         has expired, <code>false</code> otherwise
	 */
	synchronized boolean isStillAlive(final long cleanUpInterval) {

		if (this.lastReceivedHeartBeat + cleanUpInterval < System.currentTimeMillis()) {
			return false;
		}
		return true;
	}
}
