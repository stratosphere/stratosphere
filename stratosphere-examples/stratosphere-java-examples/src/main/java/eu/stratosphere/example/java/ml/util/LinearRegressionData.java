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

package eu.stratosphere.example.java.ml.util;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.example.java.ml.LinearRegression.Data;
import eu.stratosphere.example.java.ml.LinearRegression.Params;



/**
 * Provides the default data sets used for the Linear Regression example program.
 * The default data sets are used, if no parameters are given to the program.
 *
 */
public class LinearRegressionData{

	public static DataSet<Params> getDefaultParamsDataSet(ExecutionEnvironment env){

		return env.fromElements(
			new Params(0.5,1.0)
			);
	}

	public static DataSet<Data> getDefaultDataDataSet(ExecutionEnvironment env){

		return env.fromElements(
			new Data(0.5,1.0),
			new Data(1.0,2.0),
			new Data(2.0,4.0),
			new Data(3.0,6.0),
			new Data(4.0,8.0),
			new Data(5.0,10.0)
			);
	}

}