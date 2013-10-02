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

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ContainerManager;
import org.apache.hadoop.yarn.api.protocolrecords.GetContainerStatusRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetContainerStatusResponse;
import org.apache.hadoop.yarn.api.protocolrecords.StartContainerRequest;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
import org.apache.hadoop.yarn.ipc.YarnRPC;
import org.apache.hadoop.yarn.util.Records;

import eu.stratosphere.nephele.configuration.ConfigConstants;
import eu.stratosphere.nephele.util.StringUtils;

/**
 * Each allocated container is bootstrapped in a separated thread.
 * 
 * @author Tobias Herb
 */
final class YarnContainerBootstrapper extends Thread {

	/**
	 * The log object used to report debugging and error information.
	 */
	private static final Log LOG = LogFactory.getLog(YarnContainerBootstrapper.class);

	/**
	 * Name of the shell script launching the Nephele task manager inside the YARN container.
	 */
	private static final String NEPHELE_YARN_TM_LAUNCHER = "nephele-yarn-tm-launcher.sh";

	/**
	 * Handle to the allocated container.
	 */
	private final Container container;

	/**
	 * Handle to communicate with ContainerManager.
	 */
	private final ContainerManager containerManager;

	/**
	 * The path to Nephele in the cluster.
	 */
	private final String nepheleHome;

	/**
	 * The port of the job manager's RPC service.
	 */
	private final int rpcPort;

	YarnContainerBootstrapper(final Container container, final String nepheleHome, final int rpcPort,
			final YarnRPC yarnRPC, final YarnConfiguration yarnConf) {

		// sanity check
		if (container == null) {
			throw new IllegalArgumentException("Argument container must not be null");
		}
		if (nepheleHome == null) {
			throw new IllegalArgumentException("Argument nepheleHome must not be null");
		}
		if (rpcPort <= 0) {
			throw new IllegalArgumentException("Argument rpcPort must not be greater than 0");
		}
		if (yarnRPC == null) {
			throw new IllegalArgumentException("Argument yarnRPC must not be null");
		}
		if (yarnConf == null) {
			throw new IllegalArgumentException("Argument yarnConf must not be null");
		}

		this.container = container;
		this.nepheleHome = nepheleHome;
		this.rpcPort = rpcPort;

		// Connect to ContainerManager on the allocated container
		final String cmIpPortStr = container.getNodeId().getHost() + ":" + container.getNodeId().getPort();
		final InetSocketAddress cmAddress = NetUtils.createSocketAddr(cmIpPortStr);

		this.containerManager = (ContainerManager) yarnRPC.getProxy(ContainerManager.class, cmAddress, yarnConf);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		final ContainerLaunchContext launchContext = Records.newRecord(ContainerLaunchContext.class);
		launchContext.setContainerId(this.container.getId());
		launchContext.setResource(this.container.getResource());

		final String username = System.getenv(ApplicationConstants.Environment.USER.name());
		launchContext.setUser(username);

		// Set the env variables to be setup in the env where the
		// application master will be run.
		final Map<String, String> environment = new HashMap<String, String>();

		final String command = this.nepheleHome + File.separator + "bin" + File.separator + NEPHELE_YARN_TM_LAUNCHER;

		final Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();
		launchContext.setLocalResources(localResources);

		// Save the port configuration in the environment...
		environment.put(ConfigConstants.TASK_MANAGER_ID_ENV_KEY, this.container.getId().toString());
		environment.put(ConfigConstants.JOB_MANAGER_IPC_ADDRESS_ENV_KEY,
			System.getenv(ApplicationConstants.NM_HOST_ENV));
		environment.put(ConfigConstants.JOB_MANAGER_IPC_PORT_ENV_KEY, Integer.toString(this.rpcPort));
		launchContext.setEnvironment(environment);

		// Command setup.
		LOG.info("Completed setting up app master command " + command.toString());
		final List<String> commands = new ArrayList<String>();
		commands.add(command.toString());
		launchContext.setCommands(commands);
		final StartContainerRequest startRequest = Records.newRecord(StartContainerRequest.class);
		startRequest.setContainerLaunchContext(launchContext);

		try {
			this.containerManager.startContainer(startRequest);
			final GetContainerStatusRequest request = Records
				.newRecord(GetContainerStatusRequest.class);
			request.setContainerId(this.container.getId());
			final GetContainerStatusResponse response = this.containerManager.getContainerStatus(request);
			final ContainerStatus status = response.getStatus();

			LOG.info("container exit status = " + status.getExitStatus());
			LOG.info("container diagnostics = " + status.getDiagnostics());
			LOG.info("container state 		= " + status.getState().toString());

		} catch (YarnRemoteException e) {
			LOG.error(StringUtils.stringifyException(e));
			// TODO do we need to release this container?
		}
	}
}