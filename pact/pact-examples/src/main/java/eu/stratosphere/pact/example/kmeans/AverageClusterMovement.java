package eu.stratosphere.pact.example.kmeans;

import eu.stratosphere.pact.common.type.Value;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AverageClusterMovement implements Value {

  private int numClusters;
  private double distance;

  public AverageClusterMovement() {}

  public AverageClusterMovement(int numClusters, double distance) {
    this.numClusters = numClusters;
    this.distance = distance;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(numClusters);
    out.writeDouble(distance);
  }

  @Override
  public void read(DataInput in) throws IOException {
    numClusters = in.readInt();
    distance = in.readDouble();
  }

  public void add(AverageClusterMovement other) {
    numClusters += other.numClusters;
    distance += other.distance;
  }

  public void reset() {
    numClusters = 0;
    distance = 0;
  }

  public double getAverageMovement() {
    return distance / numClusters;
  }
}
