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
import org.apache.hadoop.yarn.api.AMRMProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateRequest;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.AMResponse;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
import org.apache.hadoop.yarn.util.Records;

import eu.stratosphere.nephele.instance.InstanceException;
import eu.stratosphere.nephele.instance.InstanceRequestMap;
import eu.stratosphere.nephele.instance.InstanceType;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.util.StringUtils;

final class YarnContainerAllocator extends Thread {

	public final class BootstrapParameter {
	
		public BootstrapParameter(InstanceType instanceType, Container container) {
			
			this.instanceType = instanceType;
		
			this.container = container;
		}
		
		public final InstanceType instanceType;
		
		public final Container container;
	}

	/**
	 * The log object used to report debugging and error information.
	 */
	private static final Log LOG = LogFactory.getLog(YarnContainerAllocator.class);

	/**
	 * The interval in which we poll for new containers in milliseconds.
	 */
	private static final long POLLING_INTERVAL = 500;

	/**
	 * The maximum amount of time in milliseconds we are willing to wait for a single container before we consider the
	 * allocation to be failed.
	 */
	private static final long MAXIMUM_IDLE_TIME = 10000;

	private final JobID jobID;

	private final YarnInstanceManager instanceManager;

	private final AMRMProtocol yarnResourceManager;

	private final InstanceRequestMap instanceRequestMap;

	private final List<String> splitAffinityList;

	private final int requestID;

	private final ApplicationAttemptId applicationAttemptID;

	private volatile boolean abortRequested = false;

	YarnContainerAllocator(final JobID jobID, final YarnInstanceManager instanceManager,
			final AMRMProtocol yarnResourceManager, final InstanceRequestMap instanceRequestMap,
			final List<String> splitAffinityList, final int requestID, final ApplicationAttemptId applicationAttemptID) {
		super("YARN container allocator for job " + jobID);

		this.jobID = jobID;
		this.instanceManager = instanceManager;
		this.yarnResourceManager = yarnResourceManager;
		this.instanceRequestMap = instanceRequestMap;
		this.splitAffinityList = splitAffinityList;
		this.requestID = requestID;
		this.applicationAttemptID = applicationAttemptID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		final List<ResourceRequest> resourcesToRequest = new ArrayList<ResourceRequest>();
		final Map<InstanceType, Integer> pendingContainers = new HashMap<InstanceType, Integer>();

		int totalNumberOfContainersToRequest = 0;

		LOG.info("instance allocation:");

		for (final Iterator<Map.Entry<InstanceType, Integer>> it = this.instanceRequestMap.getMaximumIterator(); it
			.hasNext();) {

			final Map.Entry<InstanceType, Integer> entry = it.next();
			final InstanceType instanceType = entry.getKey();
			final int numberOfInstances = entry.getValue().intValue();
			pendingContainers.put(instanceType, entry.getValue());

			final ResourceRequest resourceRequest = Records.newRecord(ResourceRequest.class);
			if (this.splitAffinityList == null) {
				resourceRequest.setHostName("*");
			} else if (this.splitAffinityList.isEmpty()) {
				resourceRequest.setHostName("*");
			} else {
				resourceRequest.setHostName("*"); // TODO: Add support for split affinity list
			}

			final Priority pri = Records.newRecord(Priority.class);
			pri.setPriority(0);
			resourceRequest.setPriority(pri);

			final Resource resource = Records.newRecord(Resource.class);
			resource.setMemory(instanceType.getMemorySize());
			resource.setVirtualCores(instanceType.getNumberOfCores());
			resourceRequest.setCapability(resource);
			resourceRequest.setNumContainers(numberOfInstances);

			resourcesToRequest.add(resourceRequest);
			totalNumberOfContainersToRequest += numberOfInstances;

			// log the resource/container request.
			LOG.info("instance type: " + instanceType + " | " + "instance quantity: " + numberOfInstances);
		}

		// Construct an AllocateRequest and deliver it to the resource manager.
		final AllocateRequest allocationRequest = Records.newRecord(AllocateRequest.class);
		allocationRequest.setResponseId(this.requestID);
		allocationRequest.setApplicationAttemptId(this.applicationAttemptID);
		allocationRequest.addAllAsks(resourcesToRequest);

		int allocatedContainers = 0;

		long timestampOfLastAllocation = System.currentTimeMillis();
	
		final List<BootstrapParameter> bootstrapParams = new ArrayList<BootstrapParameter>();
		
		try {

			while (allocatedContainers < totalNumberOfContainersToRequest) {

				try {
					Thread.sleep(POLLING_INTERVAL);
				} catch (InterruptedException e) {
				}

				if (this.abortRequested) {
					return;
				}

				// Assuming the ApplicationMaster can track its progress
				final float currentProgress = ((float) allocatedContainers / (float) totalNumberOfContainersToRequest);
				allocationRequest.setProgress(currentProgress);

				final AllocateResponse allocationResponse;
				try {
					synchronized (this.instanceManager) {
						allocationResponse = this.yarnResourceManager.allocate(allocationRequest);
					}
				} catch (YarnRemoteException e) {
					throw new InstanceException("An error occured while communicating with YARN: "
						+ StringUtils.stringifyException(e));
				}

				final AMResponse amResponse = allocationResponse.getAMResponse();

				// Retrieve list of allocated containers from the response
				// and on each allocated container.
				final List<Container> newAllocatedContainers = amResponse.getAllocatedContainers();

				final long now = System.currentTimeMillis();
				if (!newAllocatedContainers.isEmpty()) {
					timestampOfLastAllocation = now;
				} else {
					if (timestampOfLastAllocation + MAXIMUM_IDLE_TIME < now) {
						throw new InstanceException("Allocation of containers for job " + this.jobID + " took too long");
					}
				}

				for (Container newContainer : newAllocatedContainers) {

					LOG.info("new container" + "\n containerId = "
						+ newContainer.getId() + ",\n containerNode = "
						+ newContainer.getNodeId().getHost() + ":"
						+ newContainer.getNodeId().getPort()
						+ ",\n containerNodeURI = "
						+ newContainer.getNodeHttpAddress()
						+ ",\n containerState = "
						+ newContainer.getState()
						+ ",\n containerResourceMemory = "
						+ newContainer.getResource().getMemory());

					// Fit received container to InstanceType.
					InstanceType fittedInstanceType = null;
					for (InstanceType it : pendingContainers.keySet()) {
						if (it.getMemorySize() == newContainer.getResource().getMemory()
							&& it.getNumberOfCores() == newContainer.getResource().getVirtualCores()) {
							fittedInstanceType = it;
							break;
						}
					}

					if (fittedInstanceType == null) {
						// If no instance type fits to the allocated container, we abort.
						this.instanceManager.releaseContainer(newContainer.getId());
						throw new InstanceException("Could not find matching instance type for container "
							+ newContainer.getId());
					}

					// Update bookkeeping
					final Integer val = pendingContainers.get(fittedInstanceType);
					if (val == null) {
						this.instanceManager.releaseContainer(newContainer.getId());
						throw new InstanceException("Received container with instance type "
							+ fittedInstanceType.getIdentifier() + " although we did not expect it");
					}

					if (val.intValue() == 1) {
						pendingContainers.remove(fittedInstanceType);
					} else {
						pendingContainers.put(fittedInstanceType, Integer.valueOf(val.intValue() - 1));
					}

					// Bootstrap container
					//this.instanceManager.bootstrapContainer(this.jobID, newContainer, fittedInstanceType);
					bootstrapParams.add(new BootstrapParameter(fittedInstanceType, newContainer));
					
					// Increase number of allocated containers
					++allocatedContainers;
				}
			}
		} catch (InstanceException e) {
			LOG.error(StringUtils.stringifyException(e));
			this.instanceManager.abortJob(this.jobID);
		}
		
		// Begin to Bootstrap the TaskManagers after all Containers are allocated by YARN! 
		for(BootstrapParameter bp : bootstrapParams) {
			this.instanceManager.bootstrapContainer(jobID, bp.container, bp.instanceType);
		}

		this.instanceManager.markAllocatorAsFinished(this.jobID);
	}

	void abort() {
		this.abortRequested = true;
		interrupt();
	}
}
