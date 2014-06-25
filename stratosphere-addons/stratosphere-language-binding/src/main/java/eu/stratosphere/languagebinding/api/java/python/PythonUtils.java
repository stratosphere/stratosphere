/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ********************************************************************************************************************/
package eu.stratosphere.languagebinding.api.java.python;

import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.core.fs.Path;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_DIR;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_EXECUTOR_ID;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_EXECUTOR_HDFS_PATH;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_EXECUTOR_PATH_SUFFIX;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_GOOGLE_ID;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_GOOGLE_HDFS_PATH;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_GOOGLE_PATH_SUFFIX;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_HDFS_PATH;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_PYTHON_ID;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_PYTHON_HDFS_PATH;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_PYTHON_PATH_PREFIX;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_PYTHON_PATH_SUFFIX;
import static eu.stratosphere.languagebinding.api.java.python.PythonExecutor.STRATOSPHERE_USER_ID;
import eu.stratosphere.pact.runtime.cache.FileCache;
import java.io.IOException;

public class PythonUtils {
	public static void preparePythonEnvironment(ExecutionEnvironment env, String packagePath) throws IOException {
		//replace this with definite path for testing purposes
		String stratospherePath = System.getenv(STRATOSPHERE_DIR);
		
		//copy stratosphere package to hdfs
		FileCache.copy(
				new Path(stratospherePath + STRATOSPHERE_PYTHON_PATH_SUFFIX),
				new Path(STRATOSPHERE_PYTHON_HDFS_PATH),
				false);
		FileCache.copy(
				new Path(stratospherePath + STRATOSPHERE_GOOGLE_PATH_SUFFIX),
				new Path(STRATOSPHERE_GOOGLE_HDFS_PATH),
				false);
		FileCache.copy(
				new Path(stratospherePath + STRATOSPHERE_EXECUTOR_PATH_SUFFIX),
				new Path(STRATOSPHERE_EXECUTOR_HDFS_PATH),
				false);

		if (packagePath.endsWith("/")) {
			packagePath = packagePath.substring(0, packagePath.length() - 1);
		}
		//move user package to the same folder as executor (for pythonpath reasons)
		String tmpPackagePath = stratospherePath + STRATOSPHERE_PYTHON_PATH_PREFIX 
				+ packagePath.substring(packagePath.lastIndexOf("/"));
		FileCache.copy(
				new Path(packagePath),
				new Path(tmpPackagePath),
				false);
		
		//copy user package to hdfs
		String distributedPackagePath = STRATOSPHERE_HDFS_PATH + packagePath.substring(packagePath.lastIndexOf("/"));
		FileCache.copy(
				new Path(tmpPackagePath),
				new Path(distributedPackagePath),
				false);
		
		//register packages in distributed cache
		env.registerCachedFile(STRATOSPHERE_PYTHON_HDFS_PATH, STRATOSPHERE_PYTHON_ID);
		env.registerCachedFile(STRATOSPHERE_GOOGLE_HDFS_PATH, STRATOSPHERE_GOOGLE_ID);
		env.registerCachedFile(STRATOSPHERE_EXECUTOR_HDFS_PATH, STRATOSPHERE_EXECUTOR_ID);
		env.registerCachedFile(distributedPackagePath, STRATOSPHERE_USER_ID);
	}
}
