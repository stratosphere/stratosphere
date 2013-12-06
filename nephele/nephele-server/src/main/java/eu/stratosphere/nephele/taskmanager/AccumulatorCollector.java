package eu.stratosphere.nephele.taskmanager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.services.accumulators.Accumulator;

/**
 * TODO Refactor this (accumulators)
 * 
 * Just a simple way to get Accumulators from PACT layer () to Nephele layer
 * (TaskManager)
 * 
 */
public class AccumulatorCollector {

  private static final Log LOG = LogFactory.getLog(AccumulatorCollector.class);

  TaskManager taskManager;

  /** single instance */
  private static final AccumulatorCollector INSTANCE = new AccumulatorCollector();

  /**
   * Make constructor invisible, should be used as singleton only
   */
  private AccumulatorCollector() {
  }

  /** retrieve singleton instance */
  public static AccumulatorCollector instance() {
    return INSTANCE;
  }

  public void setTaskManager(TaskManager taskManager) {
    this.taskManager = taskManager;
  }

  public void reportAccumulators(JobID jobID, Accumulator<?,?> accumulator) {
    if (this.taskManager == null) {
      LOG.error("reportAccumulators() does not have a reference to the TaskManager but should have. Accumulators cannot be reported.");
    }
    this.taskManager.reportAccumulators(jobID, accumulator);
  }

}
