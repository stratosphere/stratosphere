---
layout: inner_docs
title:  "Stratosphere On Yarn"
sublinks:
  - {anchor: "session", title: "Start Stratosphere Session"}
  - {anchor: "submission", title: "Job Submission"}
---

## Stratosphere on Yarn

Apache [Hadoop YARN](http://hadoop.apache.org/) is a cluster resource management framework. It allows to run various distributed applications on top of a cluster. Stratosphere runs on YARN next to other applications. So users do not have to setup or install anything if there is already a YARN setup.



<b>Requirements</b>
<ul>
    <li>Apache Hadoop 2.2</li>
    <li>HDFS</li>
</ul>


<section id="session">
### Start Stratosphere Session

Follow these instructions to learn how to lauch a Stratosphere Session within your YARN cluster. A session will start all required Stratosphere services (JobManager and TaskManagers) so that you can submit jobs to the cluster. Note that you can submit multiple jobs per session.

#### Get Stratosphere Uber-Jar

You only need one file to run Stratosphere on YARN, the <i>Stratosphere Uber-Jar</i>. Download the Uber-jar on the [download page]({{site.baseurl}}/downloads/#bin).


If you want to build the uber-jar from sources, follow the build instructions. Make sure to use the `-Dhadoop.profile=2` profile. You can find the Jar file in `stratosphere-dist/target/stratosphere-dist-0.4-SNAPSHOT-yarn-uberjar.jar` (*Note: The version might be different for you* ).

### Deploy

Invoke the Jar file using the following command:
```bash
java -jar stratosphere-dist-0.4-SNAPSHOT-yarn-uberjar.jar
```
This command will show you the following overview:

```bash
java -jar stratosphere-dist-0.4-SNAPSHOT-yarn-uberjar.jar 
Missing required option: [-n Number of Yarn container to allocate (=Number of TaskTrackers)]
Usage:
   Required
     -n,--container <arg>   Number of Yarn container to allocate (=Number of TaskTrackers)
   Optional
     -c,--conf <arg>                 Path to Stratosphere configuration file
     -g,--generateConf               Place default configuration file in current directory
     -j,--jar <arg>                  Path to Stratosphere jar file
     -jm,--jobManagerMemory <arg>    Memory for JobManager Container [in MB]
     -tm,--taskManagerMemory <arg>   Memory per TaskManager Container [in MB]
     -tmc,--taskManagerCores <arg>   Virtual CPU cores per TaskManager
     -v,--verbose                    Verbose debug mode

```

Please note that the Client requires the `HADOOP_HOME` (or `YARN_CONF_DIR` or `HADOOP_CONF_DIR`) environment variable to be set to read the YARN and HDFS configuration.

**Example:** Issue the following command to allocate 10 TaskTrackers, with 8GB of memory each:

```bash
java -jar stratosphere-dist-0.4-SNAPSHOT-yarn-uberjar.jar -n 10 -tm 8192
```


The jar will automatically create a `stratosphere-config.yml` in the local directory and use this, if no config file has been specified. Please follow our [configuration guide]({{site.baseurl}}/docs/setup/config.html) if you want to change something. Stratosphere on YARN will overwrite the following configuration parameters `jobmanager.rpc.address` (because the JobManager is always allocated at different machines) and `taskmanager.tmp.dirs` (we are using the tmp directories given by YARN).

The example invocation starts 11 containers, since there is one additional container for the ApplicationMaster and JobTracker.

Once Stratosphere is deployed in your YARN cluster, it will show you the connection details of the JobTracker.

The client has to remain open to keep the deployment intact. We suggest to use `screen`. It will start another shell that is detachable.
So open `screen`, start Stratosphere on YARN, use `CTRL+a` then press `d` to detach the screen session. Use `screen -r` to resume again.



</section>

<section id="submission">
### Submit Job to Stratosphere


Use the following command to submit a Stratosphere Job-jar to the YARN cluster:

```
java -cp stratosphere-dist-0.4-SNAPSHOT-yarn-uberjar.jar eu.stratosphere.pact.client.CliFrontend remote
```

The arguments are passed this way

```
Usage: [host:port] [jar] [class] [args]
```

**Example**

```
java -cp stratosphere-dist-0.4-SNAPSHOT-yarn-uberjar.jar eu.stratosphere.pact.client.CliFrontend remote cloud-13:6123 /home/rmetzger/stratosphere-tutorial-reference-0.1-SNAPSHOT.jar \
  eu.stratosphere.tutorial.task4.WeightVectorPlan \
  "hdfs:///user/robert/bigdataclass-wikipedia hdfs:///user/robert/bigdataclass-wikipedia-result $DOP"
```


You can use also the regular mechanisms to submit jobs (`bin/pact-client.sh`), but you have to configure the `stratosphere-conf.yaml` with the correct JobManager details.

</section>
