package eu.stratosphere.pact.example.kmeans;

import com.google.common.collect.Maps;
import eu.stratosphere.pact.common.stubs.aggregators.ConvergenceCriterion;

import java.util.Map;

public class CentroidDistanceConvergenceCriterion implements ConvergenceCriterion<AverageClusterMovement> {

  @Override
  public boolean isConverged(int iteration, AverageClusterMovement movement) {
    return movement.getAverageMovement() < 0.0001;
  }

  @Override
  public String[] getVisualizationSeriesNames() {
    return new String[] { "Average Cluster movement" };
  }

  @Override
  public Map<String, Double> getVisualizationData(int iteration, AverageClusterMovement movement) {
    Map<String, Double> data = Maps.newHashMap();
    data.put("Average Cluster movement", movement.getAverageMovement());
    return data;
  }
}
