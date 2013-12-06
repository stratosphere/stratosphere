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

package eu.stratosphere.nephele.protocols;

import java.io.IOException;

import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.services.accumulators.Accumulator;

/**
 * The accumulator protocol is implemented by the job manager and offers functionality
 * to task managers allowing them to send the collected accumulators during a job
 */
public interface AccumulatorProtocol extends VersionedProtocol {

  /**
   * TODO Would be nice to have the vertex/taskID, to know (and log) in
   * JobManager where the report came from and to check if all accumulators were
   * reported (assuming that each task reports some). Optional however
   * 
   * @param jobID
   * @throws IOException
   */
	void reportAccumulatorResult(JobID jobID, Accumulator<?, ?> accumulator) throws IOException;

}
