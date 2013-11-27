package eu.stratosphere.scala

import eu.stratosphere.pact.generic.contract.Contract
import eu.stratosphere.scala.analysis.NewGlobalSchemaGenerator
import eu.stratosphere.pact.common.contract.GenericDataSink
import eu.stratosphere.pact.common.plan.Plan
import scala.collection.JavaConversions._
import java.util.Calendar

object query {
  def apply(block: => Seq[ScalaSink[_]]): ScalaPlan = {
    val sinks = block
    new ScalaPlan(sinks)
  }
}
