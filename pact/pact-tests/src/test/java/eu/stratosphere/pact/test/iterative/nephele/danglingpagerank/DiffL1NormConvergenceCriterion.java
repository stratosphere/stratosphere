package eu.stratosphere.pact.test.iterative.nephele.danglingpagerank;

import com.google.common.collect.Maps;
import eu.stratosphere.pact.common.stubs.aggregators.ConvergenceCriterion;

import eu.stratosphere.pact.common.type.base.PactLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class DiffL1NormConvergenceCriterion implements ConvergenceCriterion<PageRankStats> {

	private static final double EPSILON = 0.00005;

	private static final Log log = LogFactory.getLog(DiffL1NormConvergenceCriterion.class);

	@Override
	public boolean isConverged(int iteration, PageRankStats pageRankStats) {
		double diff = pageRankStats.diff();

		if (log.isInfoEnabled()) {
			log.info("Stats in iteration [" + iteration + "]: " + pageRankStats);
			log.info("L1 norm of the vector difference is [" + diff + "] in iteration [" + iteration + "]");
		}

		return diff < EPSILON;
	}

  @Override
  public String[] getVisualizationSeriesNames() {
    return new String[] { "L1 Norm of diff" };
  }

  @Override
  public Map<String, Double> getVisualizationData(int iteration, PageRankStats stats) {
    Map<String,Double> data = Maps.newHashMap();
    data.put("L1 Norm of diff", Double.valueOf(stats.diff()));
    return data;
  }
}
