/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
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

package eu.stratosphere.nephele.instance.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.nephele.configuration.ConfigConstants;
import eu.stratosphere.nephele.configuration.GlobalConfiguration;
import eu.stratosphere.nephele.discovery.DiscoveryException;
import eu.stratosphere.nephele.discovery.DiscoveryService;
import eu.stratosphere.nephele.instance.InstanceType;
import eu.stratosphere.nephele.util.ServerTestUtils;

/**
 * Tests for the {@link LocalInstanceManager}.
 * 
 * @author warneke
 */
public class LocalInstanceManagerTest {

	private DiscoveryService discoveryService;
	
	/**
	 * Starts the discovery service before the tests.
	 */
	@Before
	public void startDiscoveryService() {

		final String configDir = ServerTestUtils.getConfigDir();
		if (configDir == null) {
			fail("Cannot locate configuration directory");
		}

		GlobalConfiguration.loadConfiguration(configDir);

		/*final String address = GlobalConfiguration.getString(ConfigConstants.JOB_MANAGER_IPC_ADDRESS_KEY, null);
		InetAddress bindAddress = null;
		if (address != null) {
			try {
				bindAddress = InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				fail(e.getMessage());
			}
		}*/

		// Start discovery service
		final int discoveryPort = GlobalConfiguration.getInteger(ConfigConstants.DISCOVERY_PORT_KEY,
			ConfigConstants.DEFAULT_DISCOVERY_PORT);

		// Start RPC service
		final int rpcPort = GlobalConfiguration.getInteger(ConfigConstants.JOB_MANAGER_IPC_PORT_KEY,
			ConfigConstants.DEFAULT_JOB_MANAGER_IPC_PORT);
		
		try {
			this.discoveryService = new DiscoveryService(discoveryPort, rpcPort);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Stops the discovery service after the tests.
	 */
	@After
	public void stopDiscoveryService() {
		if (this.discoveryService != null) {
			this.discoveryService.shutdown();
		}
	}

	/**
	 * Checks if the local instance manager reads the default correctly from the configuration file.
	 */
	@Test
	public void testInstanceTypeFromConfiguration() {

		final String configDir = ServerTestUtils.getConfigDir();
		if (configDir == null) {
			fail("Cannot locate configuration directory");
		}

		final TestInstanceListener testInstanceListener = new TestInstanceListener();

		LocalInstanceManager lm = null;
		try {

			lm = new LocalInstanceManager();
			lm.setInstanceListener(testInstanceListener);

			final InstanceType defaultInstanceType = lm.getDefaultInstanceType();
			assertEquals("test", defaultInstanceType.getIdentifier());
			assertEquals(4, defaultInstanceType.getNumberOfComputeUnits());
			assertEquals(4, defaultInstanceType.getNumberOfCores());
			assertEquals(1024, defaultInstanceType.getMemorySize());
			assertEquals(160, defaultInstanceType.getDiskCapacity());
			assertEquals(0, defaultInstanceType.getPricePerHour());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Instantiation of LocalInstanceManager failed: " + e.getMessage());
		} finally {

			if (lm != null) {
				lm.shutdown();
			}
		}
	}
}
