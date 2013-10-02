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

JVM_ARGS="$JVM_ARGS -Xms"$NEPHELE_JM_HEAP"m -Xmx"$NEPHELE_JM_HEAP"m"

if [ "$NEPHELE_PID_DIR" = "" ]; then
	NEPHELE_PID_DIR=/tmp
fi

if [ "$NEPHELE_IDENT_STRING" = "" ]; then
	NEPHELE_IDENT_STRING="$USER"
fi

# auxilliary function to construct a lightweight classpath for the
# Nephele JobManager
constructJobManagerClassPath() {

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
		elif [[ "$jarfile" =~ 'nephele-queuescheduler' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-yarn' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'nephele-streaming' ]]; then
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
		elif [[ "$jarfile" =~ 'pact-common' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'pact-runtime' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'protobuf' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'jackson' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'log4j' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'hadoop-annotations' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'hadoop-auth' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'hadoop-common' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'hadoop-yarn' ]]; then
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
		elif [[ "$jarfile" =~ 'slf4j' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'fastutil' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'javolution' ]]; then
			add=1
		elif [[ "$jarfile" =~ 'commons' ]]; then			
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
			if [[ $NEPHELE_JM_CLASSPATH = "" ]]; then
				NEPHELE_JM_CLASSPATH=$jarfile;
			else
				NEPHELE_JM_CLASSPATH=$NEPHELE_JM_CLASSPATH:$jarfile
			fi
		fi
	done

	echo $NEPHELE_JM_CLASSPATH
}

NEPHELE_JM_CLASSPATH=$(constructJobManagerClassPath)

log=$NEPHELE_LOG_DIR/nephele-$NEPHELE_IDENT_STRING-jobmanager-$HOSTNAME.log
out=$NEPHELE_LOG_DIR/nephele-$NEPHELE_IDENT_STRING-jobmanager-$HOSTNAME.out
log_setting="-Dlog.file="$log" -Dlog4j.configuration=file://"$NEPHELE_CONF_DIR"/log4j.properties"

echo Starting Nephele job manager
$JAVA_HOME/bin/java $JVM_ARGS $NEPHELE_OPTS $log_setting -classpath $NEPHELE_JM_CLASSPATH eu.stratosphere.nephele.jobmanager.JobManager -executionMode yarn -configDir $NEPHELE_CONF_DIR  > "$out" 2>&1 < /dev/null
