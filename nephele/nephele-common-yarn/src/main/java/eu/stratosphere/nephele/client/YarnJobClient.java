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

package eu.stratosphere.nephele.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.YarnException;
import org.apache.hadoop.yarn.api.ClientRMProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationReportRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationReportResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetClusterMetricsRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetClusterMetricsResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.protocolrecords.SubmitApplicationRequest;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
import org.apache.hadoop.yarn.ipc.YarnRPC;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import eu.stratosphere.nephele.configuration.ConfigConstants;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.event.job.AbstractEvent;
import eu.stratosphere.nephele.event.job.JobEvent;
import eu.stratosphere.nephele.ipc.RPC;
import eu.stratosphere.nephele.jobgraph.JobGraph;
import eu.stratosphere.nephele.jobgraph.JobStatus;
import eu.stratosphere.nephele.net.NetUtils;
import eu.stratosphere.nephele.protocols.JobManagementProtocol;
import eu.stratosphere.nephele.types.IntegerRecord;
import eu.stratosphere.nephele.util.StringUtils;

public final class YarnJobClient {

	/*-----------------------------------------------------------------------
	 * Inner Classes.
	 *-----------------------------------------------------------------------*/

	/**
	 * Inner class used to perform clean up tasks when the
	 * job client is terminated.
	 * 
	 * @author warneke
	 */
	public static class JobCleanUp extends Thread {

		/**
		 * Stores a reference to the {@link JobClient} object this clean up object has been created for.
		 */
		private final YarnJobClient jobClient;

		/**
		 * Constructs a new clean up object which is used to perform clean up tasks
		 * when the job client is terminated.
		 * 
		 * @param jobClient
		 *        the job client this clean up object belongs to
		 */
		public JobCleanUp(final YarnJobClient jobClient) {
			this.jobClient = jobClient;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				// Terminate the running job if the configuration says so
				if (this.jobClient.getConfiguration().getBoolean(ConfigConstants.JOBCLIENT_SHUTDOWN_TERMINATEJOB_KEY, 
					ConfigConstants.DEFAULT_JOBCLIENT_SHUTDOWN_TERMINATEJOB))
				{
					System.out.println(AbstractEvent.timestampToString(System.currentTimeMillis())
						+ ":\tJobClient is shutting down, canceling job...");
										
					this.jobClient.cancelJob();
				}
				// Close the RPC object
				this.jobClient.close();
			} catch (IOException ioe) {
				LOG.warn(StringUtils.stringifyException(ioe));
			}
		}
	}
	
	/*-----------------------------------------------------------------------
	 * Constants.
	 *-----------------------------------------------------------------------*/
	
	public static final String YARN_APPLICATIONMASTER_USER_KEY = "yarn.applicationmaster.user";

	public static final String YARN_APPLICATIONMASTER_MEMORY_KEY = "yarn.applicationmaster.memory";

	public static final String NEPHELE_HOME_KEY = "yarn.nephele.home";

	public static final String NEPHELE_HOME_ENV_KEY = "NEPHELE_HOME";

	private static final String JM_HEAP_SIZE_ENV_KEY = "NEPHELE_JM_HEAP";

	private static final String YARN_APPLICATION_NAME_PREFIX = "nephele";

	private static final String NEPHELE_YARN_JM_LAUNCHER = "nephele-yarn-jm-launcher.sh";
	
	/*-----------------------------------------------------------------------
	 * Fields.
	 *-----------------------------------------------------------------------*/
	
	/**
	 * The logging object used for debugging.
	 */
	private static final Log LOG = LogFactory.getLog(YarnJobClient.class);

	/**
	 * The job management server stub.
	 */
	protected final JobManagementProtocol jobSubmitClient;

	/**
	 * The job graph assigned with this job client.
	 */
	private final JobGraph jobGraph;

	/**
	 * The configuration assigned with this job client.
	 */
	private final Configuration configuration;

	/**
	 * The shutdown hook which is executed if the user interrupts the job the job execution.
	 */
	private final JobCleanUp jobCleanUp;

	/**
	 * The sequence number of the last processed event received from the job manager.
	 */
	private long lastProcessedEventSequenceNumber = -1;


	private PrintStream console = System.out;

	/**
	 * The YARN RPC service.
	 */
	private final YarnRPC yarnRPC;

	/**
	 * YARN configuration.
	 */
	private final org.apache.hadoop.conf.Configuration yarnConf;

	/**
	 * Client's connection to the YARN resource manager.
	 */
	private final ClientRMProtocol clientRMProtocol;

	/**
	 * The ID of the application started by this client in the YARN cluster.
	 */
	private final ApplicationId applicationId;

	/*-----------------------------------------------------------------------
	 * Constructor.
	 *-----------------------------------------------------------------------*/
	
	public YarnJobClient(final JobGraph jobGraph, final Configuration configuration) throws IOException,
			InterruptedException, YarnException {

		this.jobGraph = jobGraph;
		this.configuration = configuration;
		this.jobCleanUp = new JobCleanUp(this);
		
		final String nepheleHome = configuration.getString(NEPHELE_HOME_KEY, null);
		if (nepheleHome == null) {
			throw new YarnException("Please set " + NEPHELE_HOME_KEY
				+ " to specify the location of your Stratosphere setup in the cluster");
		}

		final InetSocketAddress rmAddress = NetUtils.createSocketAddr(configuration.getString(
			YarnConfiguration.RM_ADDRESS, YarnConfiguration.DEFAULT_RM_ADDRESS));

		// Convert Nephele configuration into Hadoop configuration object
		this.yarnConf = toHadoopConfiguration(configuration);

		LOG.info("Connecting to ResourceManager at " + rmAddress);

		this.yarnRPC = YarnRPC.create(this.yarnConf);

		this.clientRMProtocol = (ClientRMProtocol) this.yarnRPC.getProxy(ClientRMProtocol.class, rmAddress,
			this.yarnConf);

		// Retrieve a new application ID
		int amMemory;
		try {

			// -----------------------------------------------------

			final GetClusterMetricsRequest metricsRequest = Records.newRecord(GetClusterMetricsRequest.class);

			final GetClusterMetricsResponse metricsResponse = this.clientRMProtocol.getClusterMetrics(metricsRequest);

			final YarnClusterMetrics clusterMetrics = metricsResponse.getClusterMetrics();

			LOG.info("YARN cluster metrics : number of node managers = " + clusterMetrics.getNumNodeManagers());

			// -----------------------------------------------------

			final GetNewApplicationRequest gnaRequest = Records.newRecord(GetNewApplicationRequest.class);
			final GetNewApplicationResponse gnaResponse = this.clientRMProtocol.getNewApplication(gnaRequest);

			this.applicationId = gnaResponse.getApplicationId();
			final int minMemoryCapability = gnaResponse.getMinimumResourceCapability().getMemory();
			final int maxMemoryCapability = gnaResponse.getMaximumResourceCapability().getMemory();

			if (LOG.isInfoEnabled()) {
				LOG.info("Received new YARN application ID " + gnaResponse.getApplicationId());
				LOG.info("Cluster's minimum resource capability is " + minMemoryCapability + " MB");
				LOG.info("Cluster's maximum resource capability is " + maxMemoryCapability + " MB");
			}

			amMemory = configuration.getInteger(YARN_APPLICATIONMASTER_MEMORY_KEY, minMemoryCapability);
			if (amMemory < minMemoryCapability) {
				LOG.warn("Configured application master memory is too low, setting it to " + minMemoryCapability
					+ " MB");
				amMemory = minMemoryCapability;
			} else if (amMemory > maxMemoryCapability) {
				LOG.warn("Configured application master memory is too high, setting it to " + maxMemoryCapability
					+ " MB");
				amMemory = maxMemoryCapability;
			}

		} catch (YarnRemoteException e) {
			close();
			throw e;
		}

		// Create the application submission context for Nephele's job manager
		final ApplicationSubmissionContext asc = Records.newRecord(ApplicationSubmissionContext.class);
		final String amUser = configuration.getString(YARN_APPLICATIONMASTER_USER_KEY, System.getProperty("user.name"));

		asc.setApplicationId(this.applicationId);
		asc.setApplicationName(YARN_APPLICATION_NAME_PREFIX);

		// Create and configure the container launch context
		final ContainerLaunchContext clc = Records.newRecord(ContainerLaunchContext.class);
		clc.setUser(amUser);

		final LocalResource dlr;
		try {
			dlr = createDummyLocalResource(this.yarnConf);
		} catch (IOException ioe) {
			close();
			throw ioe;
		}

		final Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();
		localResources.put("dummy", dlr);
		clc.setLocalResources(localResources);
		final Map<String, String> userEnvs = new HashMap<String, String>();
		userEnvs.put(JM_HEAP_SIZE_ENV_KEY, Integer.toString(amMemory));
		userEnvs.put(NEPHELE_HOME_ENV_KEY, nepheleHome);

		clc.setEnvironment(userEnvs);

		// Determine and set the amount of main memory dedicated to the application master
		final Resource amContainerResource = Records.newRecord(Resource.class);
		amContainerResource.setVirtualCores(1);
		amContainerResource.setMemory(amMemory);
		clc.setResource(amContainerResource);

		// Construct the command to be executed inside the application master container
		final List<String> commands = new ArrayList<String>();
		final String command = nepheleHome + File.separator + "bin" + File.separator + NEPHELE_YARN_JM_LAUNCHER;
		LOG.info("Command for application master: " + command);

		commands.add(command);
		clc.setCommands(commands);

		asc.setAMContainerSpec(clc);

		final SubmitApplicationRequest saRequest = Records.newRecord(SubmitApplicationRequest.class);
		saRequest.setApplicationSubmissionContext(asc);

		try {
			this.clientRMProtocol.submitApplication(saRequest);
		} catch (YarnRemoteException e) {
			close();
			throw e;
		}

		// Wait for application master to come up
		LOG.info("Waiting for application master " + this.applicationId + " to come up");
		final GetApplicationReportRequest garRequest = Records.newRecord(GetApplicationReportRequest.class);
		garRequest.setApplicationId(this.applicationId);

		InetSocketAddress jobManagerRPCAddress = null;
		while (true) {

			GetApplicationReportResponse reportResponse;
			try {
				reportResponse = this.clientRMProtocol.getApplicationReport(garRequest);
			} catch (YarnRemoteException e) {
				close();
				throw e;
			}

			final ApplicationReport report = reportResponse.getApplicationReport();

			// Application master could not be started, close all resources and throw exception
			if (report.getYarnApplicationState() == YarnApplicationState.FAILED) {
				close();
				throw new YarnException(report.getDiagnostics());
			}

			// Application master has been started properly, try to figure out the job manager's RPC address
			if (report.getYarnApplicationState() == YarnApplicationState.RUNNING) {
				jobManagerRPCAddress = new InetSocketAddress(report.getHost(), report.getRpcPort());
				LOG.info("Nephele JobManager host: " + report.getHost());
				LOG.info("Nephele JobManager RPC port: " + report.getRpcPort());
				break;
			}

			Thread.sleep(1000L);
		}

		// Start the Nephele RPC service and initialize the job manager proxy		
		jobSubmitClient = RPC.getProxy(JobManagementProtocol.class, jobManagerRPCAddress, NetUtils.getSocketFactory());
	}

	private static LocalResource createDummyLocalResource(final org.apache.hadoop.conf.Configuration hadoopConf)
			throws IOException {

		final File f = File.createTempFile("dummyLocalResource", ".dat");
		f.deleteOnExit();

		final FileSystem fs = FileSystem.getLocal(hadoopConf);
		final Path p = new Path(fs.getScheme() + "://" + f.getAbsolutePath());
		final FileStatus fileStatus = fs.getFileStatus(p);

		final LocalResource dlr = Records.newRecord(LocalResource.class);

		dlr.setSize(fileStatus.getLen());
		dlr.setType(LocalResourceType.FILE);
		dlr.setVisibility(LocalResourceVisibility.APPLICATION);
		dlr.setResource(ConverterUtils.getYarnUrlFromPath(p));
		dlr.setTimestamp(fileStatus.getModificationTime());

		return dlr;
	}

	/**
	 * Converts a Nephele configuration object into a Hadoop configuration object.
	 * 
	 * @param configuration
	 *        the Nephele configuration object
	 * @return the Hadoop configuration object containing all the key-value pairs of the original Nephele configuration
	 *         object
	 */
	private static org.apache.hadoop.conf.Configuration toHadoopConfiguration(final Configuration configuration) {

		if (configuration == null) {
			throw new IllegalArgumentException("Argument configuration must not be null");
		}

		final org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration();

		final Iterator<String> it = configuration.keySet().iterator();

		while (it.hasNext()) {

			final String key = it.next();
			final String value = configuration.getString(key, null);
			if (value != null) {
				hadoopConf.set(key, value);
			}
		}

		return hadoopConf;
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	public void close() {

		if(jobSubmitClient != null) {
			try {
				jobSubmitClient.shutdownSystem();
			} catch (IOException e1) {
				LOG.error(StringUtils.stringifyException(e1));
			}
		}
		
		// Force YARN to kill the application
		/*if (this.clientRMProtocol != null && this.applicationId != null) {
			final KillApplicationRequest killRequest = Records.newRecord(KillApplicationRequest.class);
			killRequest.setApplicationId(this.applicationId);
			try {
				this.clientRMProtocol.forceKillApplication(killRequest);
			} catch (Exception e) {
				logDebug(StringUtils.stringifyException(e));
			}
		}*/

		// Stop the local YARN proxies
		if (this.yarnRPC != null && this.clientRMProtocol != null) {
			this.yarnRPC.stopProxy(this.clientRMProtocol, this.yarnConf);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	protected void logInfo(final String msg) {
		LOG.info(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	protected void logWarn(final String msg) {
		LOG.warn(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	protected void logError(final String msg) {
		LOG.error(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	protected void logDebug(final String msg) {
		LOG.debug(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	//@Override
	protected JobManagementProtocol getJobClient() {

		return this.jobSubmitClient;
	}

	/**
	 * Returns the {@link Configuration} object which can include special configuration settings for the job client.
	 * 
	 * @return the {@link Configuration} object which can include special configuration settings for the job client
	 */
	public Configuration getConfiguration() {

		return this.configuration;
	}

	/**
	 * Submits the job assigned to this job client to the job manager.
	 * 
	 * @return a <code>JobSubmissionResult</code> object encapsulating the results of the job submission
	 * @throws IOException
	 *         thrown in case of submission errors while transmitting the data to the job manager
	 */
	public JobSubmissionResult submitJob() throws IOException {

		synchronized (this.jobSubmitClient) {

			return this.jobSubmitClient.submitJob(this.jobGraph);
		}
	}

	/**
	 * Cancels the job assigned to this job client.
	 * 
	 * @return a <code>JobCancelResult</code> object encapsulating the result of the job cancel request
	 * @throws IOException
	 *         thrown if an error occurred while transmitting the request to the job manager
	 */
	public JobCancelResult cancelJob() throws IOException {

		synchronized (this.jobSubmitClient) {
			return this.jobSubmitClient.cancelJob(this.jobGraph.getJobID());
		}
	}

	/**
	 * Retrieves the current status of the job assigned to this job client.
	 * 
	 * @return a <code>JobProgressResult</code> object including the current job progress
	 * @throws IOException
	 *         thrown if an error occurred while transmitting the request
	 */
	public JobProgressResult getJobProgress() throws IOException {

		synchronized (this.jobSubmitClient) {
			return this.jobSubmitClient.getJobProgress(this.jobGraph.getJobID());
		}
	}

	/**
	 * Submits the job assigned to this job client to the job manager and queries the job manager
	 * about the progress of the job until it is either finished or aborted.
	 * 
	 * @return the duration of the job execution in milliseconds
	 * @throws IOException
	 *         thrown if an error occurred while transmitting the request
	 * @throws JobExecutionException
	 *         thrown if the job has been aborted either by the user or as a result of an error
	 */
	public long submitJobAndWait() throws IOException, JobExecutionException {

		synchronized (this.jobSubmitClient) {

			final JobSubmissionResult submissionResult = this.jobSubmitClient.submitJob(this.jobGraph);
			if (submissionResult.getReturnCode() == AbstractJobResult.ReturnCode.ERROR) {
				LOG.error("ERROR: " + submissionResult.getDescription());
				throw new JobExecutionException(submissionResult.getDescription(), false);
			}

			// Make sure the job is properly terminated when the user shut's down the client
			Runtime.getRuntime().addShutdownHook(this.jobCleanUp);
		}

		long sleep = 0;
		try {
			final IntegerRecord interval = this.jobSubmitClient.getRecommendedPollingInterval();
			sleep = interval.getValue() * 1000;
		} catch (IOException ioe) {
			Runtime.getRuntime().removeShutdownHook(this.jobCleanUp);
			// Rethrow error
			throw ioe;
		}

		try {
			Thread.sleep(sleep / 2);
		} catch (InterruptedException e) {
			Runtime.getRuntime().removeShutdownHook(this.jobCleanUp);
			logErrorAndRethrow(StringUtils.stringifyException(e));
		}

		long startTimestamp = -1;

		while (true) {

			if (Thread.interrupted()) {
				logErrorAndRethrow("Job client has been interrupted");
			}

			JobProgressResult jobProgressResult = null;
			try {
				jobProgressResult = getJobProgress();
			} catch (IOException ioe) {
				Runtime.getRuntime().removeShutdownHook(this.jobCleanUp);
				// Rethrow error
				throw ioe;
			}

			if (jobProgressResult == null) {
				logErrorAndRethrow("Returned job progress is unexpectedly null!");
			}

			if (jobProgressResult.getReturnCode() == AbstractJobResult.ReturnCode.ERROR) {
				logErrorAndRethrow("Could not retrieve job progress: " + jobProgressResult.getDescription());
			}

			final Iterator<AbstractEvent> it = jobProgressResult.getEvents();
			while (it.hasNext()) {

				final AbstractEvent event = it.next();

				// Did we already process that event?
				if (this.lastProcessedEventSequenceNumber >= event.getSequenceNumber()) {
					continue;
				}

				this.console.println(event.toString());

				this.lastProcessedEventSequenceNumber = event.getSequenceNumber();

				// Check if we can exit the loop
				if (event instanceof JobEvent) {
					final JobEvent jobEvent = (JobEvent) event;
					final JobStatus jobStatus = jobEvent.getCurrentJobStatus();
					if (jobStatus == JobStatus.SCHEDULED) {
						startTimestamp = jobEvent.getTimestamp();
					}
					if (jobStatus == JobStatus.FINISHED) {
						//Runtime.getRuntime().removeShutdownHook(this.jobCleanUp);
						final long jobDuration = jobEvent.getTimestamp() - startTimestamp;
						this.console.println("Job duration (in ms): " + jobDuration);
						return jobDuration;
					} else if (jobStatus == JobStatus.CANCELED || jobStatus == JobStatus.FAILED) {
						//Runtime.getRuntime().removeShutdownHook(this.jobCleanUp);
						LOG.info(jobEvent.getOptionalMessage());
						if (jobStatus == JobStatus.CANCELED) {
							throw new JobExecutionException(jobEvent.getOptionalMessage(), true);
						} else {
							throw new JobExecutionException(jobEvent.getOptionalMessage(), false);
						}
					}
				}
			}

			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				logErrorAndRethrow(StringUtils.stringifyException(e));
			}
		}
	}

	/**
	 * Returns the recommended interval in seconds in which a client
	 * is supposed to poll for progress information.
	 * 
	 * @return the interval in seconds
	 * @throws IOException
	 *         thrown if an error occurred while transmitting the request
	 */
	public int getRecommendedPollingInterval() throws IOException {

		synchronized (this.jobSubmitClient) {
			return this.jobSubmitClient.getRecommendedPollingInterval().getValue();
		}
	}

	/**
	 * Writes the given error message to the log and throws it in an {@link IOException}.
	 * 
	 * @param errorMessage
	 *        the error message to write to the log
	 * @throws IOException
	 *         thrown after the error message is written to the log
	 */
	private void logErrorAndRethrow(final String errorMessage) throws IOException {
		LOG.error(errorMessage);
		throw new IOException(errorMessage);
	}

	public void setConsoleStreamForReporting(PrintStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("Console stream must not be null.");
		}
		
		this.console = stream;
	}
}
