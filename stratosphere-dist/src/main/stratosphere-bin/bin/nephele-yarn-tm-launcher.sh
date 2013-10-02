#!/bin/bash
########################################################################################################################
# 
#  Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
# 
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
#  the License. You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
#  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
#  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
#  specific language governing permissions and limitations under the License.
# 
########################################################################################################################

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/nephele-config.sh

if [ "$NEPHELE_PID_DIR" = "" ]; then
	NEPHELE_PID_DIR=/tmp
fi

if [ "$NEPHELE_IDENT_STRING" = "" ]; then
	NEPHELE_IDENT_STRING="$USER"
fi

# auxilliary function to construct a lightweight classpath for the
# Nephele TaskManager
constructTaskManagerClassPath() {

	for jarfile in $NEPHELE_LIB_DIR/*.jar ; do

		add=0

		if [[ "$jarfile" =~ 'nephele-server' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-common' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-management' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-hdfs' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-s3' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-profiling' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-streaming' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'pact-common' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'pact-runtime' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'commons-codec' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'commons-httpclient' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'commons-cli' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'commons-logging' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'commons-configuration' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'commons-lang' ]]; then
                        add=1
		elif [[ "$jarfile" =~ 'log4j' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'hadoop-core' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'httpcore' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'httpclient' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'aws-java-sdk' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'guava' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'sopremo-common' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'fastutil' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'javolution' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'kryo' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'reflectasm' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'minlog' ]]; then
                        add=1
		elif [[ "$jarfile" =~ 'asm' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'objenesis' ]]; then
                        add=1
		fi

		if [[ "$add" = "1" ]]; then
			if [[ $NEPHELE_TM_CLASSPATH = "" ]]; then
				NEPHELE_TM_CLASSPATH=$jarfile;
			else
				NEPHELE_TM_CLASSPATH=$NEPHELE_TM_CLASSPATH:$jarfile
			fi
		fi
	done

	echo $NEPHELE_TM_CLASSPATH
}



NEPHELE_TM_CLASSPATH=$(constructTaskManagerClassPath)

log=$NEPHELE_LOG_DIR/nephele-$NEPHELE_IDENT_STRING-taskmanager-$HOSTNAME-$YARN_CONTAINER_ID.$$.log
out=$NEPHELE_LOG_DIR/nephele-$NEPHELE_IDENT_STRING-taskmanager-$HOSTNAME-$YARN_CONTAINER_ID.$$.out
log_setting="-Dlog.file="$log" -Dlog4j.configuration=file://"$NEPHELE_CONF_DIR"/log4j.properties"

JVM_ARGS="$JVM_ARGS -XX:+UseParNewGC -XX:NewRatio=8 -XX:PretenureSizeThreshold=64m -Xms"$NEPHELE_TM_HEAP"m -Xmx"$NEPHELE_TM_HEAP"m"

echo Starting Nephele task manager
$JAVA_HOME/bin/java $JVM_ARGS $NEPHELE_OPTS $log_setting -classpath $NEPHELE_TM_CLASSPATH eu.stratosphere.nephele.taskmanager.TaskManager -configDir $NEPHELE_CONF_DIR > "$out" 2>&1 < /dev/null
