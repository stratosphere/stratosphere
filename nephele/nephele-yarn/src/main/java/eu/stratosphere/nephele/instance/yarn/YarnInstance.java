/***********************************************************************************************************************
 *
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
 *
 **********************************************************************************************************************/

package eu.stratosphere.nephele.instance.yarn;

import org.apache.hadoop.yarn.api.records.Container;

import eu.stratosphere.nephele.instance.AbstractInstance;
import eu.stratosphere.nephele.instance.AllocatedResource;
import eu.stratosphere.nephele.instance.AllocationID;
import eu.stratosphere.nephele.instance.HardwareDescription;
import eu.stratosphere.nephele.instance.InstanceConnectionInfo;
import eu.stratosphere.nephele.instance.InstanceType;
import eu.stratosphere.nephele.topology.NetworkNode;
import eu.stratosphere.nephele.topology.NetworkTopology;
//import eu.stratosphere.nephele.rpc.RPCService;

/**
 * Nephele representation of a YARN container. A YARN instance represents exactly one YARN container. Moreover, since
 * YARN instances are not shared across different jobs, a YARN instance also represents exactly once
 * {@link AllocatedResource}.
 * <p>
 * This class is thread-safe.
 * 
 * @author Tobias Herb
 */
public final class YarnInstance extends AbstractInstance {

	/**
	 * Handle to YARN container this instance runs in.
	 */
	private final Container container;

	/**
	 * The allocated resource of this YARN instance.
	 */
	private final AllocatedResource allocatedResource;

	/**
	 * Time stamp of the last received heart beat from the instance.
	 */
	private volatile long lastReceivedHeartbeat;

	/**
	 * Constructs a new YARN instance.
	 * 
	 * @param container
	 *        the YARN container this instance runs in
	 * @param instanceConnectionInfo
	 *        the instance connection info identifying the host
	 * @param rpcService
	 *        the RPC service to use when a proxy for a cluster instance shall be created
	 * @param capacity
	 *        capacity of this host
	 * @param parentNode
	 *        the parent node of this node in the network topology
	 * @param networkTopology
	 *        the network topology this node is part of
	 * @param hardwareDescription
	 *        the hardware description reported by the instance itself
	 */
	YarnInstance(final Container container, final InstanceType instanceType,
			final InstanceConnectionInfo instanceConnectionInfo,
			final NetworkNode parentNode, final NetworkTopology networkTopology,
			final HardwareDescription hardwareDescription) {

		super(instanceType, instanceConnectionInfo, parentNode, networkTopology, hardwareDescription);

		this.container = container;
		this.lastReceivedHeartbeat = System.currentTimeMillis();

		this.allocatedResource = new AllocatedResource(this, instanceType, AllocationID.generate());
	}

	/**
	 * Updates the time stamp of the last received heart beat to the current time.
	 */
	void updateHeartbeatTimestamp() {

		this.lastReceivedHeartbeat = System.currentTimeMillis();
	}

	/**
	 * Returns the time stamp of the last heart beat that has been received by this instance.
	 * 
	 * @return the time stamp of the last heart beat that has been received by this instance
	 */
	long getTimestampOfLastHeartbeat() {

		return this.lastReceivedHeartbeat;
	}

	/**
	 * Returns the YARN container this instance runs in.
	 * 
	 * @return the YARN container this instance runs in
	 */
	Container getContainer() {

		return this.container;
	}

	/**
	 * Returns the allocated resource of this YARN instance.
	 * 
	 * @return the allocated resource of this YARN instance
	 */
	AllocatedResource getAllocatedResource() {

		return this.allocatedResource;
	}
}
