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

package eu.stratosphere.nephele.instance;

import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.topology.NetworkTopology;

/**
 * In Nephele an instance manager maintains the set of available compute resources. It is responsible for allocating new
 * compute resources,
 * provisioning available compute resources to the JobManager and keeping track of the availability of the utilized
 * compute resources in order
 * to report unexpected resource outages.
 * 
 */
public interface InstanceManager {

	/**
	 * Requests an instance of the provided instance type from the instance manager.
	 * 
	 * @param jobID
	 *        the ID of the job this instance is requested for
	 * @param conf
	 *        a configuration object including additional request information (e.g. credentials)
	 * @param requiredSlots
	 *        the number of required slots
	 * @throws InstanceException
	 *         thrown if an error occurs during the instance request
	 */
	void requestInstance(JobID jobID, Configuration conf, int requiredSlots) throws InstanceException;

	/**
	 * Releases an allocated resource from a job.
	 * 
	 * @param jobID
	 *        the ID of the job the instance has been used for
	 * @param conf
	 *        a configuration object including additional release information (e.g. credentials)
	 * @param allocatedResource
	 *        the allocated resource to be released
	 * @throws InstanceException
	 *         thrown if an error occurs during the release process
	 */
	void releaseAllocatedResource(JobID jobID, Configuration conf, AllocatedResource allocatedResource)
			throws InstanceException;

	/**
	 * Reports a heart beat message of an instance.
	 * 
	 * @param instanceConnectionInfo
	 *        the {@link InstanceConnectionInfo} object attached to the heart beat message
	 */
	void reportHeartBeat(InstanceConnectionInfo instanceConnectionInfo);

	/**
	 * Registers a task manager at the instance manager
	 *
	 * @param instanceConnectionInfo the {@link InstanceConnectionInfo} object attached to the register task manager
	 *                                  message
	 * @param hardwareDescription the {@link eu.stratosphere.nephele.instance.HardwareDescription} object attached to
	 *                               the register task manager message
	 * @param numberOfSlots number of available slots on the instance
	 */
	void registerTaskManager(InstanceConnectionInfo instanceConnectionInfo, HardwareDescription hardwareDescription,
							 int numberOfSlots);

	/**
	 * Returns the network topology for the job with the given ID. The network topology
	 * for the job might only be an excerpt of the overall network topology. It only
	 * includes those instances as leaf nodes which are really allocated for the
	 * execution of the job.
	 * 
	 * @param jobID
	 *        the ID of the job to get the topology for
	 * @return the network topology for the job
	 */
	NetworkTopology getNetworkTopology(JobID jobID);

	/**
	 * Sets the {@link InstanceListener} object which is supposed to be
	 * notified about instance availability and deaths.
	 * 
	 * @param instanceListener
	 *        the instance listener to set for this instance manager
	 */
	void setInstanceListener(InstanceListener instanceListener);

	/**
	 * Returns the number of task slots of all registered task managers
	 * @return
	 */
	int getNumberOfSlots();

	/**
	 * Returns the {@link AbstractInstance} with the given name.
	 * 
	 * @param name
	 *        the name of the instance
	 * @return the instance with the given name or <code>null</code> if no such instance could be found
	 */
	AbstractInstance getInstanceByName(String name);

	/**
	 * Shuts the instance manager down and stops all its internal processes.
	 */
	void shutdown();

	/**
	 * 
	 * @return the number of available (registered) TaskTrackers
	 */
	int getNumberOfTaskTrackers();
}
