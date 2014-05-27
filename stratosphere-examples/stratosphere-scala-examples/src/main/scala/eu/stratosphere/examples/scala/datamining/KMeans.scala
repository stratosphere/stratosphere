/**
 * *********************************************************************************************************************
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
 * ********************************************************************************************************************
 */

package eu.stratosphere.examples.scala.datamining

import eu.stratosphere.client.LocalExecutor
import eu.stratosphere.api.common.Program
import eu.stratosphere.api.common.ProgramDescription

import eu.stratosphere.api.scala._
import eu.stratosphere.api.scala.operators._


class KMeans extends Program with ProgramDescription with Serializable {

  case class Point(x: Double, y: Double) {

    def +(other: Point) = { Point(x + other.x, y + other.y) }

    def /(div: Int) = { Point(x / div, y / div) }

    def euclidianDistance(other: Point) = {
      math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))
    }
  }

  def formatCenterOutput = ((p: Point, cid: Int, d: Double) => "%d,%.1f,%.1f".format(cid, p.x, p.y)).tupled


  def getScalaPlan(dataPointInput: String, clusterInput: String, clusterOutput: String, numIterations: Int) = {
    
    val dataPoints = DataSource(dataPointInput, CsvInputFormat[(Double, Double)]("\n", ' '))
                       .map { case (x, y) => Point(x, y) }
  
    val clusterPoints = DataSource(clusterInput, CsvInputFormat[(Int, Double, Double)]("\n", ' '))
                       .map { case (id, x, y) => (id, Point(x, y)) }


    // iterate the K-Means function, starting with the initial cluster points
    val finalCenters = clusterPoints.iterate(numIterations, { clusterPoints =>

        // compute the distance between each point and all current centroids
        val distances = dataPoints cross clusterPoints map { (point, center) =>
            val (dataPoint, (cid, clusterPoint)) = (point, center)
            val distToCluster = dataPoint euclidianDistance clusterPoint
            (dataPoint, cid, distToCluster)
        }
      
        // pick for each point the closest centroid
        val nearestCenters = distances groupBy { case (point, _, _) => point} reduceGroup { ds => ds.minBy(_._3)}
        
        // for each centroid, average among all data points that have chosen it as the closest one
        // the average is computed as sum, count, finalized as sum/count
        val nextClusterPoints = nearestCenters
            .map { case (dataPoint, cid, _) => (cid, dataPoint, 1)}
            .groupBy { _._1 }.reduce { (a, b) => (a._1, a._2 + b._2, a._3 + b._3)}
            .map { case (cid, centerPoint, num) => (cid, centerPoint / num)}

        nextClusterPoints
    })

    // compute the final distances between each point and all current centroids
    val finalDistances = dataPoints cross finalCenters map { (point, center) =>
        val (dataPoint, (cid, clusterPoint)) = (point, center)
        val distToCluster = dataPoint euclidianDistance clusterPoint
        (dataPoint, cid, distToCluster)
    }

    // pick for each point the final closest centroid
    val finalNearestCenters = finalDistances groupBy { case (point, _, _) => point} reduceGroup { ds => ds.minBy(_._3)}

    val output = finalNearestCenters.write(clusterOutput, DelimitedOutputFormat(formatCenterOutput))

    new ScalaPlan(Seq(output), "KMeans Example")
  }

  
  /**
   * The program entry point for the packaged version of the program.
   * 
   * @param args The command line arguments, including, consisting of the following parameters:
   *             <numSubStasks> <dataPoints> <clusterCenters> <output> <numIterations>"
   * @return The program plan of the kmeans example program.
   */
  override def getPlan(args: String*) = {
    getScalaPlan(args(0), args(1), args(2), args(3).toInt)
  }
    
  override def getDescription() = {
    "Parameters: <dataPoints> <clusterCenters> <output> <numIterations>"
  }
}

/**
 * Entry point to make the example standalone runnable with the local executor
 */
object RunKMeans {

  def main(args: Array[String]) {
    val km = new KMeans
    if (args.size < 4) {
      println(km.getDescription)
      return
    }
    val plan = km.getScalaPlan(args(0), args(1), args(2), args(3).toInt)
    LocalExecutor.execute(plan)
  }
}