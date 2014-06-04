/***********************************************************************************************************************
 *
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
 *
 **********************************************************************************************************************/

package eu.stratosphere.example.java.ml;

import java.io.Serializable;
import java.util.Collection;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.IterativeDataSet;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;

/**
* This example implements a basic Linear Regression using SGD algorithm.
*
* <p>
* Linear Regression with SGD(Stochastic gradient descent) algorithm is an iterative clustering algorithm and works as follows:<br>
* Giving a data set and target set, the SGD try to find out the best parameters for the data set to fit the target set.
* In each iteration, the algorithm computes the gradient of the cost function and use it to update all the parameters.
* The algorithm terminates after a fixed number of iterations (as in this implementation)
* With enough iteration, the algorithm can minimize the cost function and find the best parameters
* This is the Wikipedia entry for the <a href = "http://en.wikipedia.org/wiki/Linear_regression">Linear regression</a> and <a href = "http://en.wikipedia.org/wiki/Stochastic_gradient_descent">SGD algorithm</a>.
* 
* <p>
* This implementation works on two-dimensional data.<br>
* It find the best Theta parameter to fit the target.
*
* <p>
* This example shows how to use:
* <ul>
* <li> Bulk iterations
* <li> Broadcast variables in bulk iterations
* <li> Custom Java objects (PoJos)
* </ul>
*/

/**
 * A linearRegression example to solve the y = theta0 + theta1*x problem.
 */
@SuppressWarnings("serial")
public class LinearRegression {
    
    
    // *************************************************************************
	//     PROGRAM
	// *************************************************************************
    
    
    public static void main(String[] arg) throws Exception{
        
        // set up execution environment
        
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        
        // get input x data from elements
		DataSet<Data> data = env.fromElements(
				new Data(0.5, 1.0),
				new Data(1.0,2.0));
        // get the parameters from elements
		DataSet<Params> parameters = env.fromElements(
				new Params(0.5, 1.0));
        // set number of bulk iterations for SGD linear Regression
		IterativeDataSet<Params> loop = parameters.iterate(700);
        
		DataSet<Params> new_parameters = data
        // compute a single step using every sample
				.map(new SubUpdate()).withBroadcastSet(loop, "parameters")
        // sum up all the steps
				.reduce(new UpdateAccumulator())
        // average the steps and update all parameters
				.map(new Update());
        
        // feed new parameters back into next iteration
		DataSet<Params> result = loop.closeWith(new_parameters);
        
        // emit result
		result.print();
        
        // execute program
		env.execute("Linear Regression example");
        
        
	}
    
    // *************************************************************************
	//     DATA TYPES
	// *************************************************************************


	/**
	 * A simple data sample, x means the input, and y means the target.
	 */
	public static class Data implements Serializable{
		public double x,y;

		public Data() {};


		public Data(double x ,double y){
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "(" + x + "|" + y + ")";
		}


	}
	
	/**
	 * A set of parameters -- theta0, theta1.
	 */
	public static class Params implements Serializable{

		private double theta0,theta1;

		public Params(){};

		public Params(double x0, double x1){
			this.theta0 = x0;
			this.theta1 = x1;
		}

		@Override
		public String toString() {
			return "(" + theta0 + "|" + theta1 + ")";
		}

		public double getTheta0() {
			return theta0;
		}

		public double getTheta1() {
			return theta1;
		}

		public void setTheta0(double theta0) {
			this.theta0 = theta0;
		}

		public void setTheta1(double theta1) {
			this.theta1 = theta1;
		}

		public Params add(Params other){
			this.theta0 = theta0 + other.theta0;
			this.theta1 = theta1 + other.theta1;
			return this;
		}



		public Params div(Integer a){
			this.theta0 = theta0 / a ;
			this.theta1 = theta1 / a ;
			return this;
		}

	}

    
    // *************************************************************************
	//     USER FUNCTIONS
	// *************************************************************************
	
	/**
	 * Compute a single SGD type update for every parameters.
	 */
	public static class SubUpdate extends MapFunction<Data,Tuple2<Params,Integer>>{


		private Collection<Params> parameters; 

		private Params parameter;

		private int index = 1;


		/** Reads the parameters from a broadcast variable into a collection. */
		@Override
		public void open(Configuration parameters) throws Exception {
			this.parameters = getRuntimeContext().getBroadcastVariable("parameters");
		}


		@Override
		public Tuple2<Params, Integer> map(Data in) throws Exception {


			for(Params p : parameters){
				this.parameter = p; 
			}

			double theta_0 = parameter.theta0 - 0.1*((parameter.theta0 + (parameter.theta1*in.x)) - in.y);
			double theta_1 = parameter.theta1 - 0.1*(((parameter.theta0 + (parameter.theta1*in.x)) - in.y) * in.x);


			return new Tuple2<Params,Integer>(new Params(theta_0,theta_1),index);
		}
	}


	/**  
	 * Accumulator all the update.
	 * */
	public static class UpdateAccumulator extends ReduceFunction<Tuple2<Params, Integer>> {

		@Override
		public Tuple2<Params, Integer> reduce(Tuple2<Params, Integer> val1, Tuple2<Params, Integer> val2) {
			return new Tuple2<Params, Integer>( val1.f0.add(val2.f0), val1.f1 + val2.f1);
		}
	}

	
	/**
	 * Compute the final update by average them.
	 */
	public static class Update extends MapFunction<Tuple2<Params, Integer>,Params>{

		@Override
		public Params map(Tuple2<Params, Integer> arg0) throws Exception {
			return arg0.f0.div(arg0.f1);
		}

	}




}
