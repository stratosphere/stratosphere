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

package eu.stratosphere.nephele.jobmanager.scheduler.local;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import eu.stratosphere.nephele.executiongraph.ExecutionGraph;
import eu.stratosphere.nephele.executiongraph.ExecutionGraphIterator;
import eu.stratosphere.nephele.executiongraph.ExecutionStage;
import eu.stratosphere.nephele.executiongraph.ExecutionStageListener;
import eu.stratosphere.nephele.executiongraph.ExecutionVertex;
import eu.stratosphere.nephele.executiongraph.InternalJobStatus;
import eu.stratosphere.nephele.executiongraph.JobStatusListener;
import eu.stratosphere.nephele.instance.InstanceException;
import eu.stratosphere.nephele.instance.InstanceManager;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.jobmanager.DeploymentManager;
import eu.stratosphere.nephele.jobmanager.scheduler.AbstractScheduler;
import eu.stratosphere.nephele.jobmanager.scheduler.SchedulingException;
import eu.stratosphere.util.StringUtils;

public class LocalScheduler extends AbstractScheduler implements JobStatusListener, ExecutionStageListener {

	/**
	 * The job queue of the scheduler
	 */
	private Deque<ExecutionGraph> jobQueue = new ArrayDeque<ExecutionGraph>();

	/**
	 * Constructs a new local scheduler.
	 * 
	 * @param deploymentManager
	 *        the deployment manager assigned to this scheduler
	 * @param instanceManager
	 *        the instance manager to be used with this scheduler
	 */
	public LocalScheduler(final DeploymentManager deploymentManager, final InstanceManager instanceManager) {
		super(deploymentManager, instanceManager);
	}

	void removeJobFromSchedule(final ExecutionGraph executionGraphToRemove) {

		boolean removedFromQueue = false;

		synchronized (this.jobQueue) {

			final Iterator<ExecutionGraph> it = this.jobQueue.iterator();
			while (it.hasNext()) {

				final ExecutionGraph executionGraph = it.next();
				// Field jobID of executionGraph is immutable, so no synchronization needed
				if (executionGraph.getJobID().equals(executionGraphToRemove.getJobID())) {
					removedFromQueue = true;
					it.remove();
					break;
				}

			}
		}

		if (!removedFromQueue) {
			LOG.error("Cannot find job " + executionGraphToRemove.getJobName() + " ("
				+ executionGraphToRemove.getJobID() + ") to remove");
		}

		// TODO: Remove vertices from restart map
	}


	@Override
	public void scheduleJob(final ExecutionGraph executionGraph) throws SchedulingException {

		final int availableNumberOfSlots = getInstanceManager().getNumberOfSlots();
		final int requiredNumberOfSlots = executionGraph.getRequiredSlots();

		if(availableNumberOfSlots < requiredNumberOfSlots){
			throw new SchedulingException("Unable to schedule job: Required number of slots " + requiredNumberOfSlots
					+ " exceeds available number of slots " + availableNumberOfSlots + ".");
		}

		// Subscribe to job status notifications
		executionGraph.registerJobStatusListener(this);

		// Set state of each vertex for scheduled
		final ExecutionGraphIterator it2 = new ExecutionGraphIterator(executionGraph, true);
		while (it2.hasNext()) {

			final ExecutionVertex vertex = it2.next();
			vertex.registerExecutionListener(new LocalExecutionListener(this, vertex));
		}

		// Register the scheduler as an execution stage listener
		executionGraph.registerExecutionStageListener(this);

		// Add job to the job queue (important to add job to queue before requesting instances)
		synchronized (this.jobQueue) {
			this.jobQueue.add(executionGraph);
		}

		// Request resources for the first stage of the job

		final ExecutionStage executionStage = executionGraph.getCurrentExecutionStage();
		try {
			requestInstances(executionStage);
		} catch (InstanceException e) {
			final String exceptionMessage = StringUtils.stringifyException(e);
			LOG.error(exceptionMessage);
			this.jobQueue.remove(executionGraph);
			throw new SchedulingException(exceptionMessage);
		}
	}


	@Override
	public ExecutionGraph getExecutionGraphByID(final JobID jobID) {

		synchronized (this.jobQueue) {

			final Iterator<ExecutionGraph> it = this.jobQueue.iterator();
			while (it.hasNext()) {

				final ExecutionGraph executionGraph = it.next();
				if (executionGraph.getJobID().equals(jobID)) {
					return executionGraph;
				}
			}
		}

		return null;
	}


	@Override
	public void shutdown() {

		synchronized (this.jobQueue) {
			this.jobQueue.clear();
		}

	}


	@Override
	public void jobStatusHasChanged(final ExecutionGraph executionGraph, final InternalJobStatus newJobStatus,
			final String optionalMessage) {

		if (newJobStatus == InternalJobStatus.FAILED || newJobStatus == InternalJobStatus.FINISHED
			|| newJobStatus == InternalJobStatus.CANCELED) {
			removeJobFromSchedule(executionGraph);
		}
	}


	@Override
	public void nextExecutionStageEntered(final JobID jobID, final ExecutionStage executionStage) {

		// Request new instances if necessary
		try {
			requestInstances(executionStage);
		} catch (InstanceException e) {
			// TODO: Handle this error correctly
			LOG.error(StringUtils.stringifyException(e));
		}

		// Deploy the assigned vertices
		deployAssignedInputVertices(executionStage.getExecutionGraph());

		// Initialize the replay of the previous stage's checkpoints
		replayCheckpointsFromPreviousStage(executionStage.getExecutionGraph());
	}
}