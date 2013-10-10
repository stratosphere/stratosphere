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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;

import eu.stratosphere.nephele.instance.InstanceType;

/**
 * The pending container list contains all those YARN containers of a job which have been reported to be allocated by
 * the YARN resource manager but whose task managers have not yet sent a heartbeat.
 * <p>
 * This class is not thread-safe.
 * 
 * @author warneke
 */
final class PendingContainerList {

	/**
	 * The log object used to report debugging and error information.
	 */
	private static final Log LOG = LogFactory.getLog(PendingContainerList.class);
	
	/**
	 * A pending container list entry contains all information necesasry to either detect stale containers or to
	 * transform the container into a YARN instance later on.
	 * <p>
	 * This class is thread-safe.
	 * 
	 * @author warneke
	 */
	private static final class PendingContainerListEntry {

		/**
		 * The allocated YARN container.
		 */
		private final Container container;

		/**
		 * The instance type this YARN container represents.
		 */
		private final InstanceType instanceType;

		/**
		 * The time the container has been allocated.
		 */
		private final long creationTime;

		/**
		 * Constructs a new pending container list entry.
		 * 
		 * @param container
		 *        the allocated YARN container
		 * @param instanceType
		 *        the instance type this YARN container represents
		 */
		private PendingContainerListEntry(final Container container, final InstanceType instanceType) {
			this.container = container;
			this.instanceType = instanceType;
			this.creationTime = System.currentTimeMillis();
		}
	}

	/**
	 * The entries of this pending container list.
	 */
	private final Map<String, PendingContainerListEntry> entries = new HashMap<String, PendingContainerListEntry>();

	/**
	 * Adds a new container to the pending container list.
	 * 
	 * @param container
	 *        the allocated YARN container
	 * @param instanceType
	 *        the instance type the YARN container represents
	 */
	void add(final Container container, final InstanceType instanceType) {

		final String containerID = container.getId().toString();
		if (this.entries.put(containerID, new PendingContainerListEntry(container, instanceType)) != null) {
			throw new IllegalStateException("Container ID " + containerID
				+ " already had an entry in pending container list");
		}
	}

	/**
	 * Returns the YARN container with the given ID.
	 * 
	 * @param containerID
	 *        the ID to identify the YARN container
	 * @return the YARN container with the given ID or <code>null</code> if no such container is in the list
	 */
	Container getContainer(final String containerID) {

		final PendingContainerListEntry entry = this.entries.get(containerID);
		if (entry == null) {
			return null;
		}

		return entry.container;
	}

	/**
	 * Returns the instance type of the YARN container with the given ID.
	 * 
	 * @param containerID
	 *        the ID to identify the YARN container
	 * @return the instance type of YARN container with the given ID or <code>null</code> if no such container is in the
	 *         list
	 */
	InstanceType getInstanceType(final String containerID) {

		final PendingContainerListEntry entry = this.entries.get(containerID);
		if (entry == null) {
			return null;
		}

		return entry.instanceType;
	}

	/**
	 * Removes the container with the given ID from the list.
	 * 
	 * @param containerID
	 *        the ID to identify the YARN container
	 */
	void remove(final String containerID) {

		this.entries.remove(containerID);
	}

	/**
	 * Checks if the list is empty.
	 * 
	 * @return <code>true</code> if the list is empty, <code>false</code> otherwise
	 */
	boolean isEmpty() {

		return this.entries.isEmpty();
	}

	/**
	 * Returns a list of state containers, i.e. containers which have been allocated before the given creation time.
	 * 
	 * @param earliestCreationTime
	 *        the creation time before which containers must have been allocated in order to be considered stale
	 * @return a list of stale containers or <code>null</code> if no container in the list is considered stale
	 */
	List<Container> removeStaleContainers(final long earliestCreationTime) {

		List<Container> staleContainers = null;
		final Iterator<PendingContainerListEntry> it = this.entries.values().iterator();

		while (it.hasNext()) {

			final PendingContainerListEntry entry = it.next();
			if (entry.creationTime < earliestCreationTime) {

				if (staleContainers == null) {
					staleContainers = new ArrayList<Container>();
				}

				it.remove();
				staleContainers.add(entry.container);
			}
		}

		return staleContainers;
	}

	/**
	 * Removes all containers from this pending container list.
	 * 
	 * @return a list of IDs of all the removed containers.
	 */
	List<ContainerId> removeAllContainers() {

		final List<ContainerId> removedContainers = new ArrayList<ContainerId>(this.entries.size());
		final Iterator<PendingContainerListEntry> it = this.entries.values().iterator();

		while (it.hasNext()) {
			removedContainers.add(it.next().container.getId());
		}

		this.entries.clear();

		return removedContainers;
	}
}
