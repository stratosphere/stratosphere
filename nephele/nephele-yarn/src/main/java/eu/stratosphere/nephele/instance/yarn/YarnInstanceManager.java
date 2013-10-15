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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.yarn.api.AMRMProtocol;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateRequest;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterRequest;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
import org.apache.hadoop.yarn.ipc.YarnRPC;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import eu.stratosphere.nephele.client.YarnJobClient;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.instance.AbstractInstance;
import eu.stratosphere.nephele.instance.AllocatedResource;
import eu.stratosphere.nephele.instance.AllocationID;
import eu.stratosphere.nephele.instance.HardwareDescription;
import eu.stratosphere.nephele.instance.HardwareDescriptionFactory;
import eu.stratosphere.nephele.instance.InstanceConnectionInfo;
import eu.stratosphere.nephele.instance.InstanceException;
import eu.stratosphere.nephele.instance.InstanceListener;
import eu.stratosphere.nephele.instance.InstanceManager;
import eu.stratosphere.nephele.instance.InstanceRequestMap;
import eu.stratosphere.nephele.instance.InstanceType;
import eu.stratosphere.nephele.instance.InstanceTypeDescription;
import eu.stratosphere.nephele.instance.InstanceTypeDescriptionFactory;
import eu.stratosphere.nephele.instance.InstanceTypeFactory;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.jobmanager.JobManager;
import eu.stratosphere.nephele.topology.NetworkTopology;
import eu.stratosphere.nephele.util.StringUtils;


public final class YarnInstanceManager implements InstanceManager {

	/*-----------------------------------------------------------------------
	 * Inner Classes.
	 *-----------------------------------------------------------------------*/

	/**
	 * The HeartbeatTask sends in a configured interval vital signs to the ResourceManager component.
	 * 
	 * @author Tobias Herb
	 */
	private final class HeartbeatTask extends TimerTask {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {

			try {

				synchronized (YarnInstanceManager.this) {

					// Do not send heartbeat if other allocators are running.
					if (!runningAllocators.isEmpty()) {
						return;
					}

					// Construct and send heartbeat.
					final AllocateRequest request = Records.newRecord(AllocateRequest.class);
					request.setApplicationAttemptId(containerId.getApplicationAttemptId());
					request.setResponseId(generateRequestID());

					resourceManager.allocate(request);
				}
			} catch (YarnRemoteException e) {
				LOG.error(StringUtils.stringifyException(e));
			}
		}
	}

	/**
	 * The PeriodicCleanupTask is responsible for detecting stale containers.
	 * 
	 * @author warneke
	 */
	private final class PeriodicCleanupTask extends TimerTask {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {

			LOG.debug("Running periodic cleanup task");

			final long staleTimestamp = System.currentTimeMillis() - STALE_CONTAINER_INTERVAL;

			synchronized (YarnInstanceManager.this) {

				// Check for stale pending containers
				for (final Iterator<Map.Entry<JobID, PendingContainerList>> it = pendingContainerMap.entrySet()
					.iterator(); it.hasNext();) {

					final Map.Entry<JobID, PendingContainerList> entry = it.next();
					final List<Container> staleContainers = entry.getValue().removeStaleContainers(staleTimestamp);
					if (staleContainers == null) {
						continue;
					}
					
					final JobID jobID = entry.getKey();
					if (LOG.isInfoEnabled()) {
						LOG.info("Job " + jobID + " has " + staleContainers.size() + " stale pending containers");
						for( Container c : staleContainers ) {					
							LOG.info( "STALE CONTAINERS: " + c.getId() );						
						}
					}
					
					// Simply abort the job
					abortJob(jobID);
				}

				// Check for stale allocated containers
				for (final Iterator<Map.Entry<JobID, AllocatedContainerList>> it = allocatedContainerMap.entrySet()
					.iterator(); it.hasNext();) {

					final Map.Entry<JobID, AllocatedContainerList> entry = it.next();
					final List<YarnInstance> stalelInstances = entry.getValue().removeStaleInstances(staleTimestamp);
					if (stalelInstances == null) {
						continue;
					}

					final JobID jobID = entry.getKey();
					if (LOG.isInfoEnabled()) {
						LOG.info("Job " + jobID + " has " + stalelInstances.size() + " stale allocated containers");
					}

					// Simply abort the job
					abortJob(jobID);
				}
			}
		}
	}

	/*-----------------------------------------------------------------------
	 * Constants.
	 *-----------------------------------------------------------------------*/

	/**
	 * The log object used to report debugging and error information.
	 */
	private static final Log LOG = LogFactory.getLog(YarnInstanceManager.class);

	/**
	 * The environment variable for the job manager heap size.
	 */
	public static final String JM_HEAP_SIZE_ENV_KEY = "NEPHELE_JM_HEAP";

	/**
	 * The interval in which the periodic cleanup task is triggered in milliseconds.
	 */
	private static final long PERIODIC_CLEANUP_INTERVAL = 10000;

	/**
	 * The interval after which containers are considered to be stale in milliseconds.
	 */
	private static final long STALE_CONTAINER_INTERVAL = 60000; // 30000

	/*-----------------------------------------------------------------------
	 * Attributes.
	 *-----------------------------------------------------------------------*/

	/**
	 * The ID of the YARN container this application master is running in.
	 */
	private final ContainerId containerId;

	/**
	 * The YARN configuration object.
	 */
	private final YarnConfiguration yarnConf;

	/**
	 * The YARN RPC service.
	 */
	private final YarnRPC yarnRPC;

	/**
	 * The proxy to communicate with the YARN resource manager.
	 */
	private final AMRMProtocol resourceManager;

	/**
	 * The base directory of Nephele in the cluster.
	 */
	private final String nepheleHome;

	/**
	 * Set of instance types which can be queried by the job manager.
	 */
	private final Map<String, InstanceType> availableInstanceTypes;

	/**
	 * The default instance type.
	 */
	private final InstanceType defaultInstanceType;

	/**
	 * The interval in milliseconds in which we have to check back with the YARN
	 * resource manager in order not to get killed.
	 */
	private final long resourceManagerHeartbeatInterval;

	/**
	 * The heartbeat timer task to make sure we do not get killed by the YARN resource manager.
	 */
	private final HeartbeatTask heartbeatTask = new HeartbeatTask();

	/**
	 * The periodic cleanup task to make sure we detect stale containers.
	 */
	private final PeriodicCleanupTask periodicCleanupTask = new PeriodicCleanupTask();

	/**
	 * Object that is notified if instances become available or vanish.
	 */
	private InstanceListener instanceListener;

	/**
	 * Map storing currently running container allocators.
	 */
	private final Map<JobID, YarnContainerAllocator> runningAllocators = new HashMap<JobID, YarnContainerAllocator>();

	/**
	 * Mapping from container IDs to job IDs.
	 */
	private final Map<String, JobID> containerToJobIDMap = new HashMap<String, JobID>();

	/**
	 * Mapping from jobs to pending containers.
	 */
	private final Map<JobID, PendingContainerList> pendingContainerMap = new HashMap<JobID, PendingContainerList>();

	/**
	 * Mapping from jobs to allocated containers.
	 */
	private final Map<JobID, AllocatedContainerList> allocatedContainerMap = new HashMap<JobID, AllocatedContainerList>();

	/*-----------------------------------------------------------------------
	 * Constructor.
	 *-----------------------------------------------------------------------*/

	/**
	 * Constructor.
	 * 
	 * @throws YarnRemoteException
	 */
	public YarnInstanceManager() throws YarnRemoteException {
		
		LOG.info("Starting YARN instance manager");

		// Extract the necessary information from the environment
		final Map<String, String> envs = System.getenv();
		// Extract the container ID
		final String containerIDString = envs.get(ApplicationConstants.AM_CONTAINER_ID_ENV);
		if (containerIDString == null) {
			// Container id should always be set in the env by the framework.
			throw new IllegalArgumentException("Variable " + ApplicationConstants.AM_CONTAINER_ID_ENV
				+ " not set in the environment");
		}
		this.containerId = ConverterUtils.toContainerId(containerIDString);
		
		this.nepheleHome = envs.get(YarnJobClient.NEPHELE_HOME_ENV_KEY);
		if (this.nepheleHome == null) {
			throw new IllegalArgumentException("Variable " + YarnJobClient.NEPHELE_HOME_ENV_KEY
				+ " is not set in the environment");
		}

		final String host = envs.get(ApplicationConstants.NM_HOST_ENV);
		if (host == null) {
			throw new IllegalArgumentException("Variable " + ApplicationConstants.NM_HOST_ENV
				+ " is not set in the environment");
		}
		
		// Setup a connection to the resource manager.
		this.yarnConf = new YarnConfiguration();
		final InetSocketAddress rmAddress = NetUtils.createSocketAddr(this.yarnConf.get(
			YarnConfiguration.RM_SCHEDULER_ADDRESS, YarnConfiguration.DEFAULT_RM_ADDRESS));

		// Initialize RPC implementation.
		this.yarnRPC = YarnRPC.create(this.yarnConf);

		// Connect to RPC server on RM.
		LOG.info("Connecting to ResourceManager at " + rmAddress);
		this.resourceManager = (AMRMProtocol) this.yarnRPC.getProxy(AMRMProtocol.class, rmAddress, this.yarnConf);

		// Register the AM with the RM.
		final RegisterApplicationMasterRequest appMasterRequest = Records
			.newRecord(RegisterApplicationMasterRequest.class);

		// Set the required info into the registration request:
		// ApplicationAttemptId
		appMasterRequest.setApplicationAttemptId(this.containerId.getApplicationAttemptId());
		// Host on which the app master is running.
		appMasterRequest.setHost(host);
		
		// RPC port on which the app master accepts requests from the client		
		appMasterRequest.setRpcPort( JobManager.jobManagerIPCPort );
		
		// Tracking URL for the client to track app master progress.
		// appMasterRequest.setTrackingUrl(trackingUrl);

		// The registration response is useful as it provides information about
		// the cluster. Similar to the GetNewApplicationResponse in the client,
		// it provides information about the min/mx resource capabilities of the
		// cluster that would be needed by the ApplicationMaster when requesting
		// for containers.
		final RegisterApplicationMasterResponse response = this.resourceManager
			.registerApplicationMaster(appMasterRequest);

		this.availableInstanceTypes = constructAvailableInstanceTypes(response.getMinimumResourceCapability(),
			response.getMaximumResourceCapability());

		this.defaultInstanceType = findInstanceTypeWithSmallestMemory(this.availableInstanceTypes.values());

		// Determine the YARN heartbeat interval and divide by two just to make
		// sure we do not run late
		this.resourceManagerHeartbeatInterval = this.yarnConf.getLong(YarnConfiguration.RM_AM_EXPIRY_INTERVAL_MS,
			YarnConfiguration.DEFAULT_RM_AM_EXPIRY_INTERVAL_MS) / 2L;

		// Finally, launch the timer tasks.
		new Timer(true).schedule(this.heartbeatTask, this.resourceManagerHeartbeatInterval,
			this.resourceManagerHeartbeatInterval);
		new Timer(true).schedule(this.periodicCleanupTask, PERIODIC_CLEANUP_INTERVAL, PERIODIC_CLEANUP_INTERVAL);

	}

	private void checkSufficientClusterResources(final InstanceRequestMap instanceRequestMap) throws InstanceException {

		final AllocateRequest clusterUtilizationRequest = Records.newRecord(AllocateRequest.class);
		clusterUtilizationRequest.setApplicationAttemptId(this.containerId.getApplicationAttemptId());
		clusterUtilizationRequest.setResponseId(generateRequestID());

		final AllocateResponse clusterUtilizationResponse;
		try {
			clusterUtilizationResponse = this.resourceManager.allocate(clusterUtilizationRequest);
		} catch (YarnRemoteException e) {
			throw new InstanceException(StringUtils.stringifyException(e));
		}

		// Get the available resources in the cluster
		final Resource availableResources = clusterUtilizationResponse.getAMResponse().getAvailableResources();

		if (LOG.isInfoEnabled()) {
			LOG.info("cluster resources = " + "<cores:" + availableResources.getVirtualCores() + ", memory:"
				+ availableResources.getMemory() + ">");
		}

		// Minimum resource consumption
		Iterator<Map.Entry<InstanceType, Integer>> minIterator = instanceRequestMap.getMinimumIterator();
		// int minCoreConsumption = 0;
		int minMemoryConsumption = 0;

		while (minIterator.hasNext()) {
			final Map.Entry<InstanceType, Integer> entry = minIterator.next();
			// minCoreConsumption += entry.getKey().getNumberOfCores() * entry.getValue();
			minMemoryConsumption += entry.getKey().getMemorySize() * entry.getValue();
		}

		// Check if enough resources are available for the minimal
		// requirements...
		// TODO: Include CPU cores here
		if ( /*
			 * availableResources.getVirtualCores() <
			 * minCoreConsumption ||
			 */
		availableResources.getMemory() < minMemoryConsumption) {
			throw new InstanceException("Not enough resources available for running the job.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void requestInstance(final JobID jobID, final Configuration conf,
			final InstanceRequestMap instanceRequestMap, final List<String> splitAffinityList) throws InstanceException {

		// -----------------------------------------------------
		// Determine Containers Request.
		// -----------------------------------------------------

		LOG.info("(1) Determine container requests:");

		// Check if there are sufficient resources in the cluster
		checkSufficientClusterResources(instanceRequestMap);

		// -----------------------------------------------------
		// Allocate Containers.
		// -----------------------------------------------------
		LOG.info("(2) Allocate containers:");

		final YarnContainerAllocator allocator = new YarnContainerAllocator(jobID, this, this.resourceManager,
			instanceRequestMap, splitAffinityList, generateRequestID(), this.containerId.getApplicationAttemptId());

		if (this.runningAllocators.put(jobID, allocator) != null) {
			throw new IllegalStateException("Job " + jobID + " already has a running allocator");
		}

		allocator.start();
	}

	synchronized void markAllocatorAsFinished(final JobID jobID) {

		this.runningAllocators.remove(jobID);
	}

	synchronized void bootstrapContainer(final JobID jobID, final Container container, final InstanceType instanceType) {

		LOG.info("Bootstrap containers:");

		this.containerToJobIDMap.put(container.getId().toString(), jobID);
		PendingContainerList pendingContainerList = this.pendingContainerMap.get(jobID);
		if (pendingContainerList == null) {
			pendingContainerList = new PendingContainerList();
			this.pendingContainerMap.put(jobID, pendingContainerList);
		}

		// Add to list of pending containers
		pendingContainerList.add(container, instanceType);
		

		// Start bootstrap thread
		YarnContainerBootstrapper ycb = new YarnContainerBootstrapper(container, this.nepheleHome, JobManager.jobManagerIPCPort, this.yarnRPC,
			this.yarnConf, instanceType );		
		ycb.start();
	}

	private void releaseContainers(final List<ContainerId> containerIDs) {

		final AllocateRequest allocationRequest = Records.newRecord(AllocateRequest.class);
		allocationRequest.setResponseId(generateRequestID());
		allocationRequest.setApplicationAttemptId(this.containerId.getApplicationAttemptId());
		allocationRequest.addAllReleases(containerIDs);

		try {
			this.resourceManager.allocate(allocationRequest);
		} catch (YarnRemoteException e) {
			LOG.error(StringUtils.stringifyException(e));
		}

		for (final Iterator<ContainerId> it = containerIDs.iterator(); it.hasNext();) {
			this.containerToJobIDMap.remove(it.next().toString());
		}
	}

	synchronized void releaseContainer(final ContainerId containerID) {

		LOG.info("Releasing container " + containerID.toString());

		final List<ContainerId> containerIDs = new ArrayList<ContainerId>(1);
		containerIDs.add(containerID);

		releaseContainers(containerIDs);
	}

	synchronized void abortJob(final JobID jobID) {

		LOG.info("abortJob called");
		// TODO: Implement me
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void releaseAllocatedResource(final JobID jobID, final Configuration conf,
			final AllocatedResource allocatedResource) throws InstanceException {

		final AllocatedContainerList allocatedContainerList = this.allocatedContainerMap.get(jobID);
		if (allocatedContainerList == null) {
			LOG.error("Unable to release resources for unknown job " + jobID);
			return;
		}

		final AllocationID allocationID = allocatedResource.getAllocationID();
		final YarnInstance instance = allocatedContainerList.removeInstanceByAllocationID(allocationID);
		if (instance == null) {
			LOG.error("Unable to release resources for unknown allocation ID " + allocationID);
			return;
		}

		// TODO: Shutdown only for demo purposes.
		try {
			instance.getTaskManagerProxy().shutdownTaskManager();					
		} catch (IOException e) {
			LOG.error(StringUtils.stringifyException(e));
		}	

		// Clean up allocated container map
		if (allocatedContainerList.isEmpty()) {
			this.allocatedContainerMap.remove(jobID);
		}

		// Release the container back to YARN
		releaseContainer(instance.getContainer().getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceType getSuitableInstanceType(final int minNumComputeUnits, final int minNumCPUCores,
			final int minMemorySize, final int minDiskCapacity, final int maxPricePerHour) {

		// Note that instanceTypeDescriptionMap is immutable, so this method does not have to be synchronized.

		InstanceType suitableInstanceType = null;
		long memory = Long.MAX_VALUE;

		final Iterator<InstanceType> it = this.availableInstanceTypes.values().iterator();
		while (it.hasNext()) {

			final InstanceType instanceType = it.next();

			if (instanceType.getMemorySize() >= minMemorySize
				&& instanceType.getMemorySize() < memory
			// && instanceType.getNumberOfCores() >= minNumCPUCores
			// && instanceType.getNumberOfComputeUnits() >= minNumComputeUnits
			// && instanceType.getDiskCapacity() >= minDiskCapacity
			// && instanceType.getPricePerHour() <= maxPricePerHour
			) {
				memory = instanceType.getMemorySize();
				suitableInstanceType = instanceType;
			}
		}

		return suitableInstanceType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void reportHeartBeat(final InstanceConnectionInfo instanceConnectionInfo,
			final HardwareDescription hardwareDescription, final String taskManagerID) {

		if (taskManagerID == null) {
			throw new IllegalArgumentException("taskManagerID must not be null");
		}

		// Find the job ID
		final JobID jobID = this.containerToJobIDMap.get(taskManagerID);
		if (jobID == null) {
			LOG.error("Unable to determine job for container ID " + taskManagerID);
			return;
		}

		// Update time stamp of heartbeat if we now the instance already
		AllocatedContainerList allocatedContainerList = this.allocatedContainerMap.get(jobID);
		if (allocatedContainerList != null) {
			final YarnInstance yarnInstance = allocatedContainerList.getInstanceByContainerID(taskManagerID);
			if (yarnInstance != null) {
				yarnInstance.updateHeartbeatTimestamp();
				return;
			}
		}

		LOG.info("- new container detected -");
		LOG.info("inetaddress : " + instanceConnectionInfo.getAddress());
		LOG.info("hostname : " + instanceConnectionInfo.getHostName());
		LOG.info("domainname : " + instanceConnectionInfo.getDomainName());
		LOG.info("ipcport : " + instanceConnectionInfo.getIPCPort());
		LOG.info("dataport : " + instanceConnectionInfo.getDataPort());
		LOG.info("num cores : " + hardwareDescription.getNumberOfCPUCores());
		LOG.info("free memory : " + hardwareDescription.getSizeOfFreeMemory());
		LOG.info("physical memory : " + hardwareDescription.getSizeOfPhysicalMemory());

		// Find container in the list of pending containers
		final PendingContainerList pendingContainerList = this.pendingContainerMap.get(jobID);
		if (pendingContainerList == null) {
			LOG.error("Job " + jobID + " does not expect further resources");
			return;
		}

		final InstanceType instanceType = pendingContainerList.getInstanceType(taskManagerID);
		if (instanceType == null) {
			LOG.error("Job " + jobID + " does not expect container with ID " + taskManagerID);
			return;
		}

		final Container container = pendingContainerList.getContainer(taskManagerID);
		if (container == null) {
			LOG.error("Job " + jobID + " does not expect container with ID " + taskManagerID);
			return;
		}

		// Clean up pending container map
		pendingContainerList.remove(taskManagerID);
		if (pendingContainerList.isEmpty()) {
			this.pendingContainerMap.remove(jobID);
		}

		// Now we create a new instance from the new container
		LOG.info("Creating new instance for container " + taskManagerID + ", " + instanceConnectionInfo);

		if (allocatedContainerList == null) {
			allocatedContainerList = new AllocatedContainerList();
			this.allocatedContainerMap.put(jobID, allocatedContainerList);
		}

		final YarnInstance instance = allocatedContainerList.createAndAddNewInstance(container, instanceConnectionInfo,
			hardwareDescription, instanceType);

		// Notify the scheduler about the new instance.
		final List<AllocatedResource> allocatedResources = new ArrayList<AllocatedResource>(1);
		allocatedResources.add(instance.getAllocatedResource());
		new YarnInstanceNotifier(this.instanceListener, jobID, allocatedResources).start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceType getInstanceTypeByName(final String instanceTypeName) {

		// No synchronization is needed as availableInstanceTypes is immutable and final.

		// Sanity check
		if (instanceTypeName == null) {
			throw new IllegalArgumentException("Argument name must not be null");
		}

		return this.availableInstanceTypes.get(instanceTypeName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceType getDefaultInstanceType() {

		// No synchronization is needed as defaultInstanceType is immutable and final.
		return this.defaultInstanceType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized NetworkTopology getNetworkTopology(final JobID jobID) {

		final AllocatedContainerList allocatedContainerList = this.allocatedContainerMap.get(jobID);
		if (allocatedContainerList != null) {
			return allocatedContainerList.getNetworkTopology();
		}

		return NetworkTopology.createEmptyTopology();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setInstanceListener(final InstanceListener instanceListener) {

		this.instanceListener = instanceListener;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Map<InstanceType, InstanceTypeDescription> getMapOfAvailableInstanceTypes() {

		// Compute number of available instances on the fly
		final Map<InstanceType, InstanceTypeDescription> map = new HashMap<InstanceType, InstanceTypeDescription>();

		final AllocateRequest request = Records.newRecord(AllocateRequest.class);
		request.setApplicationAttemptId(this.containerId.getApplicationAttemptId());
		request.setResponseId(generateRequestID());

		final AllocateResponse response;
		try {
			response = this.resourceManager.allocate(request);
		} catch (YarnRemoteException e) {
			LOG.error(StringUtils.stringifyException(e));
			// Return the map in the current state
			return map;
		}

		final Resource ar = response.getAMResponse().getAvailableResources();
		LOG.info("Available resources are " + ar.getVirtualCores() + " CPU cores and " + ar.getMemory()
			+ " MB of memory");

		final Iterator<InstanceType> it = this.availableInstanceTypes.values().iterator();

		while (it.hasNext()) {

			final InstanceType instanceType = it.next();

			// TODO: Also include CPU here
			final int numberOfAvailableInstances = ar.getMemory() / instanceType.getMemorySize();
			map.put(
				instanceType, InstanceTypeDescriptionFactory.construct(instanceType,
					constructHardwareDescription(instanceType), numberOfAvailableInstances));
		}

		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized AbstractInstance getInstanceByName(final String name) {

		// Sanity check
		if (name == null) {
			throw new IllegalArgumentException("Argument name must not be null");
		}

		final Iterator<AllocatedContainerList> it = this.allocatedContainerMap.values().iterator();
		while (it.hasNext()) {

			final AllocatedContainerList list = it.next();
			final YarnInstance instance = list.getInstanceByName(name);
			if (instance != null) {
				return instance;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void cancelPendingRequests(final JobID jobID) {

		final YarnContainerAllocator allocator = this.runningAllocators.remove(jobID);
		if (allocator != null) {
			allocator.abort();
		}

		final PendingContainerList pendingContainerList = this.pendingContainerMap.remove(jobID);
		if (pendingContainerList != null) {
			final List<ContainerId> containersToRelease = pendingContainerList.removeAllContainers();
			releaseContainers(containersToRelease);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void shutdown() {

		LOG.info("Shutdown initiated");

		// Cancel the timer tasks.
		this.heartbeatTask.cancel();
		this.periodicCleanupTask.cancel();

		// Cancel all allocators and wait for them to finish
		final Collection<YarnContainerAllocator> allocators = this.runningAllocators.values();
		Iterator<YarnContainerAllocator> it = allocators.iterator();
		while (it.hasNext()) {
			it.next().abort();
		}

		it = allocators.iterator();
		while (it.hasNext()) {
			try {
				it.next().join();
				it.remove();
			} catch (InterruptedException e) {
				LOG.debug(StringUtils.stringifyException(e));
				break;
			}
		}
		
		if (this.yarnRPC != null && this.resourceManager != null) {
			this.yarnRPC.stopProxy(this.resourceManager, this.yarnConf);
		}

		// Some final sanity checks
		if (!this.runningAllocators.isEmpty()) {
			LOG.error(this.runningAllocators.size() + " container allocators are still running");
		}
		if (!this.containerToJobIDMap.isEmpty()) {
			LOG.error("Found " + this.containerToJobIDMap.size() + " orphaned container to job ID mappings");
		}
		if (!this.pendingContainerMap.isEmpty()) {
			LOG.error("Found " + this.pendingContainerMap.size() + " orphaned pending container map entries");
		}
		if (!this.allocatedContainerMap.isEmpty()) {
			LOG.error("Found " + this.allocatedContainerMap.size() + " orphaned allocated container map entries");
		}

		LOG.info("Shutdown complete");
	}

	/**
	 * Auxiliary method to generate random IDs for resource requests.
	 * 
	 * @return the randomly generated request ID.
	 */
	private static int generateRequestID() {
		return (int) (Math.random() * (double) Integer.MAX_VALUE);
	}

	/**
	 * @param minCapability
	 * @param maxCapability
	 * @return
	 */
	private static Map<String, InstanceType> constructAvailableInstanceTypes(final Resource minCapability,
			final Resource maxCapability) {

		final Map<String, InstanceType> instanceTypes = new HashMap<String, InstanceType>();
		int cores = minCapability.getVirtualCores();
		int memory = minCapability.getMemory();
		final int maxCores = maxCapability.getVirtualCores();
		final int maxMemory = maxCapability.getMemory();

		while (true) {
			if (cores >= maxCores || memory >= maxMemory) {
				break;
			}
			final InstanceType it = constructInstanceType(cores, memory);
			instanceTypes.put(it.getIdentifier(), it);
			cores *= 2;
			memory *= 2;
		}
		final InstanceType it = constructInstanceType(cores, memory);
		instanceTypes.put(it.getIdentifier(), it);

		return Collections.unmodifiableMap(instanceTypes);
	}

	/**
	 * @param numberOfCPUCores
	 * @param sizeOfMemory
	 * @return
	 */
	private static InstanceType constructInstanceType(final int numberOfCPUCores, final int sizeOfMemory) {

		final String identifier = "yarn_" + numberOfCPUCores + "_" + sizeOfMemory;
		final int diskCapacity = 100; // TODO: Find something smarter here
		final int pricePerHour = numberOfCPUCores; // TODO: Find something smarter here

		return InstanceTypeFactory.construct(identifier, numberOfCPUCores, numberOfCPUCores, sizeOfMemory,
			diskCapacity, pricePerHour);
	}

	private static HardwareDescription constructHardwareDescription(final InstanceType instanceType) {

		final long memoryInBytes = instanceType.getMemorySize() * 1024L * 1024L;
		return HardwareDescriptionFactory.construct(instanceType.getNumberOfCores(), memoryInBytes, memoryInBytes);
	}

	/**
	 * @param availableInstances
	 * @return
	 */
	private static InstanceType findInstanceTypeWithSmallestMemory(final Collection<InstanceType> availableInstanceTypes) {

		InstanceType smallestInstanceType = null;
		int smallestMemory = Integer.MAX_VALUE;
		final Iterator<InstanceType> it = availableInstanceTypes.iterator();

		while (it.hasNext()) {
			final InstanceType i = it.next();
			final int memorySize = i.getMemorySize();
			if (memorySize < smallestMemory) {
				smallestMemory = memorySize;
				smallestInstanceType = i;
			}
		}

		return smallestInstanceType;
	}
}
