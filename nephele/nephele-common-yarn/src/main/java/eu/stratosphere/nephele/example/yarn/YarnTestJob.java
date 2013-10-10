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

package eu.stratosphere.nephele.example.yarn;

import org.mortbay.log.Log;

import eu.stratosphere.nephele.client.YarnJobClient;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.io.DistributionPattern;
import eu.stratosphere.nephele.io.channels.ChannelType;
import eu.stratosphere.nephele.jobgraph.JobGenericInputVertex;
import eu.stratosphere.nephele.jobgraph.JobGenericOutputVertex;
import eu.stratosphere.nephele.jobgraph.JobGraph;
import eu.stratosphere.nephele.jobgraph.JobGraphDefinitionException;
import eu.stratosphere.nephele.util.StringUtils;

public final class YarnTestJob {

	/**
	 * The logging object used for debugging.
	 */
	//private static final Log LOG = LogFactory.getLog(YarnTestJob.class);
	
	private static JobGraph generateJobGraph(final int degreeOfParallelism) throws JobGraphDefinitionException {

		final JobGraph jobGraph = new JobGraph("Yarn Test Job");

		final JobGenericInputVertex producer = new JobGenericInputVertex("Yarn Test Producer", jobGraph);
		producer.setInputClass(YarnTestProducer.class);
		producer.setNumberOfSubtasks(degreeOfParallelism);
		producer.setNumberOfSubtasksPerInstance(1);

		final JobGenericOutputVertex consumer = new JobGenericOutputVertex("Yarn Test Consumer", jobGraph);
		consumer.setOutputClass(YarnTestConsumer.class);
		consumer.setNumberOfSubtasks(degreeOfParallelism);
		consumer.setNumberOfSubtasksPerInstance(1);

		// Set vertex sharing
		producer.setVertexToShareInstancesWith(consumer);

		// Connect the vertices
		producer.connectTo(consumer, ChannelType.NETWORK,
			DistributionPattern.BIPARTITE);

		return jobGraph;
	}

	public static void main(final String[] args) throws Exception {
		
		// Configure default logging format
		org.apache.log4j.BasicConfigurator.configure();

		Log.info("-- YarnTestJob --");
		
		// Generate test job graph with given degree of parallelism
		final JobGraph jobGraph = generateJobGraph(4);

		// Set configuration
		final Configuration conf = new Configuration();
		conf.setInteger(YarnJobClient.YARN_APPLICATIONMASTER_MEMORY_KEY, 256);
		conf.setString(YarnJobClient.NEPHELE_HOME_KEY, "/home/tobias/stratosphere-0.4-ozone-SNAPSHOT");
		conf.setString("jobmanager.rpc.address", "localhost");

		// Create job client and launch job
		final YarnJobClient jobClient = new YarnJobClient(jobGraph, conf);
		try {
			jobClient.submitJobAndWait();
		} catch( Exception ex ) {			
			System.out.println( StringUtils.stringifyException( ex ) );			
		}finally {
			jobClient.close();
		}	
	}
}
