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

package eu.stratosphere.nephele.instance.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.configuration.ConfigConstants;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.configuration.GlobalConfiguration;
import eu.stratosphere.nephele.instance.AbstractInstance;
import eu.stratosphere.nephele.instance.AllocatedResource;
import eu.stratosphere.nephele.instance.HardwareDescription;
import eu.stratosphere.nephele.instance.HardwareDescriptionFactory;
import eu.stratosphere.nephele.instance.InstanceConnectionInfo;
import eu.stratosphere.nephele.instance.InstanceException;
import eu.stratosphere.nephele.instance.InstanceListener;
import eu.stratosphere.nephele.instance.InstanceManager;
import eu.stratosphere.nephele.instance.InstanceType;
import eu.stratosphere.nephele.instance.InstanceTypeFactory;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.taskmanager.TaskManager;
import eu.stratosphere.nephele.topology.NetworkTopology;

/**
 * The local instance manager is designed to manage instance allocation/deallocation for a single-node setup. It spans a
 * task manager which is executed within the same process as the job manager. Moreover, it determines the hardware
 * characteristics of the machine it runs on and generates a default instance type with the identifier "default". If
 * desired this default instance type can also be overwritten.
 */
public class LocalInstanceManager implements InstanceManager {

	/**
	 * The log object used to report events and errors.
	 */
	private static final Log LOG = LogFactory.getLog(LocalInstanceManager.class);

	/**
	 * The instance listener registered with this instance manager.
	 */
	private InstanceListener instanceListener;

	/**
	 * A synchronization object to protect critical sections.
	 */
	private final Object synchronizationObject = new Object();

	/**
	 * The local instance encapsulating the task manager
	 */
	private LocalInstance localInstance;

	/**
	 * The thread running the local task manager.
	 */
	private final TaskManager taskManager;

	/**
	 * The network topology the local instance is part of.
	 */
	private final NetworkTopology networkTopology;

	/**
	 * Constructs a new local instance manager.
	 */
	public LocalInstanceManager() throws Exception {

		final Configuration config = GlobalConfiguration.getConfiguration();

		this.networkTopology = NetworkTopology.createEmptyTopology();

		this.taskManager = new TaskManager();
	}

	@Override
	public void releaseAllocatedResource(JobID jobID, Configuration conf, AllocatedResource allocatedResource)
			throws InstanceException
	{
		synchronized (this.synchronizationObject) {

			if(allocatedResource.getInstance().equals(this.localInstance)){
				localInstance.releaseSlot(allocatedResource.getAllocationID());
			}else{
				throw new InstanceException("Resource with allocation ID " + allocatedResource.getAllocationID()
						+ " has not been allocated to job with ID " + jobID
						+ " according to the local instance manager's internal bookkeeping");
			}
		}
	}


	@Override
	public void reportHeartBeat(final InstanceConnectionInfo instanceConnectionInfo) {
	}

	@Override
	public void registerTaskManager(final InstanceConnectionInfo instanceConnectionInfo,
									final HardwareDescription hardwareDescription, int numberOfSlots){
		synchronized(this.synchronizationObject){
			if(this.localInstance == null){
				this.localInstance = new LocalInstance(instanceConnectionInfo, this.networkTopology.getRootNode(),
						this.networkTopology, hardwareDescription, numberOfSlots);
			}
		}
	}


	@Override
	public void shutdown() {
		// Stop the internal instance of the task manager
		if (this.taskManager != null) {
			this.taskManager.shutdown();
		}

		// Destroy local instance
		synchronized (this.synchronizationObject) {
			if (this.localInstance != null) {
				this.localInstance.destroyProxies();
				this.localInstance = null;
			}
		}
	}


	@Override
	public NetworkTopology getNetworkTopology(final JobID jobID) {
		return this.networkTopology;
	}


	@Override
	public void setInstanceListener(final InstanceListener instanceListener) {
		this.instanceListener = instanceListener;
	}


	@Override
	public void requestInstance(final JobID jobID, final Configuration conf, final int requiredSlots) throws
			InstanceException {

		if(localInstance != null){
			synchronized(this.synchronizationObject){
				List<AllocatedResource> allocatedResources = new ArrayList<AllocatedResource>();
				if(localInstance.getNumberOfAvailableSlots() >= requiredSlots){
					for(int i = 0; i< requiredSlots; i++){
						AllocatedResource allocatedResource = localInstance.allocateSlot(jobID);
						allocatedResources.add(allocatedResource);
					}

					new LocalInstanceNotifier(this.instanceListener, jobID, allocatedResources).start();
				}else{
					throw new InstanceException("Not enough slots available to schedule job " + jobID + ".");
				}
			}
		}else{
			throw new InstanceException("No local instance availabe.");
		}
	}

	@Override
	public AbstractInstance getInstanceByName(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("Argument name must not be null");
		}

		synchronized (this.synchronizationObject) {

			if (this.localInstance != null) {
				if (name.equals(this.localInstance.getName())) {
					return this.localInstance;
				}
			}
		}
		return null;
	}

	@Override
	public int getNumberOfTaskTrackers() {
		return (this.localInstance == null) ? 0 : 1; // this instance manager can have at most one TaskTracker
	}

	@Override
	public int getNumberOfSlots() {
		if(this.localInstance != null){
			return localInstance.getNumberOfSlots();
		}else{
			return 0;
		}
	}
}
