package eu.stratosphere.pact.example.kmeans;

import eu.stratosphere.pact.common.stubs.aggregators.Aggregator;

public class AverageMovementAggregator implements Aggregator<AverageClusterMovement> {

  AverageClusterMovement aggregate = new AverageClusterMovement();

  @Override
  public AverageClusterMovement getAggregate() {
    return aggregate;
  }

  @Override
  public void aggregate(AverageClusterMovement other) {
    aggregate.add(other);
  }

  @Override
  public void reset() {
    aggregate.reset();
  }
}
