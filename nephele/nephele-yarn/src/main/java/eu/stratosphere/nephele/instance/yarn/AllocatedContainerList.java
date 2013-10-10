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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.records.Container;

import eu.stratosphere.nephele.instance.AllocationID;
import eu.stratosphere.nephele.instance.HardwareDescription;
import eu.stratosphere.nephele.instance.InstanceConnectionInfo;
import eu.stratosphere.nephele.instance.InstanceType;
import eu.stratosphere.nephele.topology.NetworkTopology;
//import eu.stratosphere.nephele.rpc.RPCService;

final class AllocatedContainerList {

	private static final Log LOG = LogFactory.getLog(AllocatedContainerList.class);
	
	private final NetworkTopology networkTopology;

	private final Map<String, YarnInstance> instancesByContainerID = new HashMap<String, YarnInstance>();

	private final Map<AllocationID, YarnInstance> instancesByAllocationID = new HashMap<AllocationID, YarnInstance>();

	AllocatedContainerList() {

		this.networkTopology = NetworkTopology.createEmptyTopology();
	}

	NetworkTopology getNetworkTopology() {

		return this.networkTopology;
	}

	YarnInstance getInstanceByContainerID(final String containerID) {

		return this.instancesByContainerID.get(containerID);
	}

	YarnInstance removeInstanceByAllocationID(final AllocationID allocationID) {

		final YarnInstance instance = this.instancesByAllocationID.remove(allocationID);

		if (instance == null) {
			return null;
		}

		this.instancesByContainerID.remove(instance.getContainer().getId().toString());

		// Remove from network topology
		instance.remove();

		return instance;
	}

	YarnInstance createAndAddNewInstance(final Container container,
			final InstanceConnectionInfo instanceConnectionInfo, final HardwareDescription hardwareDescription,
			final InstanceType instanceType) {

		final YarnInstance instance = new YarnInstance(container, instanceType, instanceConnectionInfo,
			this.networkTopology.getRootNode(), this.networkTopology, hardwareDescription);

		final String containerID = container.getId().toString();
		if (this.instancesByContainerID.put(containerID, instance) != null) {
			throw new IllegalStateException("List already contains an entry for container ID " + containerID);
		}

		final AllocationID allocationID = instance.getAllocatedResource().getAllocationID();
		if (this.instancesByAllocationID.put(allocationID, instance) != null) {
			throw new IllegalStateException("List already contains an entry for allocation ID " + allocationID);
		}

		return instance;
	}

	boolean isEmpty() {

		return this.instancesByContainerID.isEmpty();
	}

	List<YarnInstance> removeStaleInstances(final long earliestCreationTime) {

		List<YarnInstance> staleInstances = null;
		final Iterator<YarnInstance> it = this.instancesByContainerID.values().iterator();

		while (it.hasNext()) {

			final YarnInstance instance = it.next();
			if (instance.getTimestampOfLastHeartbeat() < earliestCreationTime) {

				if (staleInstances == null) {
					staleInstances = new ArrayList<YarnInstance>();
				}

				it.remove();
				this.instancesByAllocationID.remove(instance.getAllocatedResource().getAllocationID());
				staleInstances.add(instance);
			}
		}

		return staleInstances;
	}

	YarnInstance getInstanceByName(final String name) {

		final Iterator<YarnInstance> it = this.instancesByContainerID.values().iterator();

		while (it.hasNext()) {

			final YarnInstance instance = it.next();
			if (name.equals(instance.getName())) {
				return instance;
			}
		}

		return null;
	}
	
	void shutdownAllYarnInstances() throws IOException {
		LOG.info("(4) AllocatedContainerList.shutdownAllYarnInstances");
		final Collection<AllocationID> allocationIds = instancesByAllocationID.keySet();
		final List<YarnInstance> instances = new ArrayList<YarnInstance>();
		for(AllocationID id : allocationIds) {
			instances.add(removeInstanceByAllocationID(id));
		}
		for(YarnInstance yi : instances) {
			yi.getTaskManagerProxy().shutdownTaskManager();
		}
	} 
}
