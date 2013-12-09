---
layout: inner_docs_pre04
title:  "Configuration"
sublinks:
  - {anchor: "common", title: "Most Common Options"}
  - {anchor: "jm_tm", title: "Job &amp; TaskManager"}
  - {anchor: "compiler", title: "Compiler"}
  - {anchor: "web_frontend", title: "Web Frontend"}
  - {anchor: "hdfs", title: "HDFS"}
  - {anchor: "profiling", title: "Profiling"}
  - {anchor: "visualization", title: "Visualization"}
---

## Configuration

<p class="lead">This documentation provides an overview of possible settings for Stratosphere.</p>

All configuration is done in `conf/stratosphere-conf.yaml`, which is expected to be a flat collection of [YAML key value pairs](http://www.yaml.org/spec/1.2/spec.html) with format `key: value`.

The system and run scripts parse the config at startup and override the respective default values with the given values for every that has been set. This page contains a reference for all configuration keys used in the system.

<section id="common">
### Most Common Options

<table class="table table-striped">
<thead>
<tr class="header">
<th>Key</th>
<th>Description</th>
<th>Default Value</th>
</tr>
</thead>
<tbody>
<tr>
<td>env.java.home</td>
<td>The path to the Java installation to use.</td>
<td>The system's default Java installation</td>
</tr>
<tr>
<td>jobmanager.rpc.address</td>
<td>The IP address of the JobManager.</td>
<td>localhost</td>
</tr>
<tr>
<td>jobmanager.rpc.port</td>
<td>The port number of the JobManager.</td>
<td>6123</td>
</tr>
<tr>
<td>jobmanager.profiling.enable</td>
<td>The key to check if the job manager's profiling component should be enabled (required for load charts in NepheleGUI).</td>
<td>false</td>
</tr>
<tr>
<td>jobmanager.web.port</td>
<td>Port of the JobManager's web interface.</td>
<td>8081</td>
</tr>
<tr>
<td>jobmanager.heap.mb</td>
<td>JVM heap size in megabytes of the JobManager.</td>
<td>256</td>
</tr>
<tr>
<td>taskmanager.heap.mb</td>
<td>JVM heap size in megabytes of the TaskManager.</td>
<td>512</td>
</tr>
<tr>
<td>taskmanager.tmp.dirs</td>
<td>The directory for temporary files, or a list of directories separated by the systems directory delimiter (for example ':' (colon) on Linux/Unix). If multiple directories are specified then the temporary files will be distributed across the directories in a round robin fashion. The I/O manager component will spawn one reading and one writing thread per directory. A directory may be listed multiple times to have the I/O manager use multiple threads for it (for example if it is physically stored on a very fast disc or RAID).</td>
<td>The system's tmp dir</td>
</tr>
<tr>
<td>taskmanager.memory.size</td>
<td>The amount of memory available for the task manager's memory manager, in megabytes. The memory manager distributes memory among the different tasks, which need it for sorting, hash tables, or result caching. If unspecified (-1), the memory manager will take a fixed ratio of the heap memory available to the JVM after all Nephele services have started (0.8).</td>
<td>-1</td>
</tr>
<tr>
<td>channel.network.numberOfBuffers</td>
<td>The default number of buffers available to the system. This number determines how many channels a TaskManager can have at the same time and how well buffered the channels are. If a job is rejected or you get a warning that the system has too little buffers available, increase this value.</td>
<td>256</td>
</tr>
<tr>
<td>pact.parallelization.degree</td>
<td>The default degree of parallelism to use for pact programs that have no degree of parallelism specified. A value of -1 indicates no limit, in which the degree of parallelism is set to the number of available instances (at the time of compilation) times the intra-node parallelism of each instance.</td>
<td>-1</td>
</tr>
<tr>
<td>pact.parallelization.max-intra-node-degree</td>
<td>The maximal number of parallel instances of the user function that are assigned to a single computing instance. A value of -1 indicates no limit. If the desired degree of parallelism is not achievable with the given number of instances and the given upper limit per instance, then the degree of parallelism may be reduced by the compiler.</td>
<td>-1</td>
</tr>
</tbody>
</table>
</section>

<section id="jm_tm">
### Job & TaskManager

The following parameters configure Nephele's JobManager, TaskManager,
and runtime channel management.

<table class="table table-striped">
<thead>
<tr class="header">
<th>Key</th>
<th>Description</th>
<th>Default Value</th>
</tr>
</thead>
<tbody>
<tr>
<td>job.execution.retries</td>
<td>The default number of times of vertex shall be reexecuted before its execution is considered as failed.</td>
<td>2</td>
</tr>
<tr>
<td>jobclient.polling.interval</td>
<td>The recommended client polling interval (seconds).</td>
<td>5</td>
</tr>
<tr>
<td>jobmanager.rpc.address</td>
<td>The IP address of the JobManager.</td>
<td>localhost</td>
</tr>
<tr>
<td>jobmanager.rpc.port</td>
<td>The port number of the JobManager.</td>
<td>6123</td>
</tr>
<tr>
<td>jobmanager.rpc.numhandler</td>
<td>The number of RPC threads for the JobManager.</td>
<td>3</td>
</tr>
<tr>
<td>jobmanager.profiling.enable</td>
<td>Decide whether the Profiler is used or not.</td>
<td>false</td>
</tr>
<tr>
<td>jobmanager.web.port</td>
<td>Port of the JobManager's web interface.</td>
<td>8081</td>
</tr>
<tr>
<td>jobmanager.heap.mb</td>
<td>JVM heap size in megabytes of the JobManager.</td>
<td>256</td>
</tr>
<tr>
<td>taskmanager.heap.mb</td>
<td>JVM heap size in megabytes of the TaskManager.</td>
<td>512</td>
</tr>
<tr>
<td>taskmanager.rpc.port</td>
<td>The task manager's IPC port.</td>
<td>6122</td>
</tr>
<tr>
<td>taskmanager.data.port</td>
<td>The task manager's data port used for NETWORK channels.</td>
<td>6121</td>
</tr>
<tr>
<td>taskmanager.setup.periodictaskinterval</td>
<td>The interval of periodic tasks (Heartbeat, check Task Execution) by the TaskManager. The interval is measured in milliseconds.</td>
<td>1000</td>
</tr>
<tr>
<td>taskmanager.memory.size</td>
<td>The amount of memory available for the task manager's memory manager, in megabytes. The memory manager distributes memory among the different tasks, which need it for sorting, hash tables, or result caching. If unspecified (-1), the memory manager will take a fixed ratio of the heap memory available to the JVM after all Nephele services have started (0.8).</td>
<td>-1</td>
</tr>
<tr>
<td>taskmanager.tmp.dirs</td>
<td>The directory for temporary files, or a list of directories separated by the systems directory delimiter (for example ':' (colon) on Linux/Unix). If multiple directories are specified then the temporary files will be distributed across the directories in a round robin fashion. The I/O manager component will spawn one reading and one writing thread per directory. A directory may be listed multiple times to have the I/O manager use multiple threads for it (for example if it is physically stored on a very fast disc or RAID).</td>
<td>The system's tmp dir</td>
</tr>
<tr>
<td>channel.network.numberOfBuffers</td>
<td>The default number of buffers available to the system. This number determines how many channels a TaskManager can have at the same time and how well buffered the channels are. If a job is rejected or you get a warning that the system has too little buffers available, increase this value.</td>
<td>256</td>
</tr>
<tr>
<td>channel.network.bufferSizeInBytes</td>
<td>The default number of read buffers.</td>
<td>64 * 1024 (64 k)</td>
</tr>
<tr>
<td>channel.network.numberOfOutgoingConnectionThreads</td>
<td>The default number of outgoing connection threads.</td>
<td>1</td>
</tr>
<tr>
<td>channel.network.numberOfConnectionRetries</td>
<td>The default number of connection retries.</td>
<td>10</td>
</tr>
<tr>
<td>channel.network.allowSenderSideSpilling</td>
<td>!Enables/Disables spilling of network channels. <br /> Spilling happens if a task does not consume data as fast as it arrives on the network. <br /> Spilling can prevent deadlocks but can cause bad performance.</td>
<td>false</td>
</tr>
<tr>
<td>channel.network.mergeSpilledBuffers</td>
<td>Enables/disables merging of spilled buffers</td>
<td>true</td>
</tr>
<tr>
<td>channel.inMemory.numberOfConnectionRetries</td>
<td>The number of connection retries.</td>
<td>30</td>
</tr>
</tbody>
</table>
</section>

<section id="compiler">
### Compiler

The following parameters configure the PACT compiler and have therefore
an impact on the scheduling and execution of PACT programs on Nephele.

<table class="table table-striped">
<thead>
<tr class="header">
<th>Key</th>
<th>Description</th>
<th>Default Value</th>
</tr>
</thead>
<tbody>
<tr>
<td>pact.parallelization.degree</td>
<td>The default degree of parallelism to use for pact programs that have no degree of parallelism specified. A value of -1 indicates no limit, in which the degree of parallelism is set to the number of available instances at the time of compilation.</td>
<td>-1</td>
</tr>
<tr>
<td>pact.parallelization.max-intra-node-degree</td>
<td>The maximal number of parallel instances of the user function that are assigned to a single computing instance. A value of -1 indicates no limit. If the desired degree of parallelism is not achievable with the given number of instances and the given upper limit per instance, then the degree of parallelism may be reduced by the compiler.</td>
<td>1</td>
</tr>
<tr>
<td>pact.parallelization.maxmachines</td>
<td>An optional hard limit in the number of machines (Nephele Instances) to use. A program will never use more than the here specified number of machines. If set to '-1', the limit is set by the maximal number of instances available in the cluster. If this value is set, the actual number of machines used for certain tasks may be even lower than this value, due to scheduling constraints.</td>
<td>-1</td>
</tr>
</tbody>
</table>
</section>

<section id="web_frontend">
### Web Frontend

These parameters configure the PACT web interface. For information on
how to start, use, and configure the PACT web interface, refer to
[here](executepactprogram.html "executepactprogram").

<table class="table table-striped">
<tbody>
<tr>
<td>pact.web.port</td>
<td>The port of the frontend web server</td>
<td>8080</td>
</tr>
<tr>
<td>pact.web.rootpath</td>
<td>The path to the root directory containing the web documents</td>
<td>./resources/web-docs/</td>
</tr>
<tr>
<td>pact.web.temp</td>
<td>The temp directory for the web server. Used for example for caching file fragments during file-uploads.</td>
<td>/tmp</td>
</tr>
<tr>
<td>pact.web.uploaddir</td>
<td>The directory into which the web server will store uploaded pact programs.</td>
<td>/tmp/pact-jobs/</td>
</tr>
<tr>
<td>pact.web.plandump</td>
<td>The directory into which the web server will dump temporary JSON files describing pact plans.</td>
<td>/tmp/pact-plans/</td>
</tr>
</tbody>
</table>
</section>

<section id="hdfs">
### HDFS

These parameters configure the default HDFS used by Stratosphere. If you don't specify a HDFS configuration, you will have to specify the full path to your HDFS files like `hdfs://address:port/path/to/files`.

<table class="table table-striped">
<thead>
<tr class="header">
<th>Key</th>
<th>Description</th>
<th>Default Value</th>
</tr>
</thead>
<tbody>
<tr>
<td>fs.hdfs.hdfsdefault</td>
<td>The absolute path of Hadoop's own configuration file “hdfs-default.xml”.</td>
<td>null</td>
</tr>
<tr>
<td>fs.hdfs.hdfssite</td>
<td>The absolute path of Hadoop's own configuration file “hdfs-site.xml”.</td>
<td>null</td>
</tr>
</tbody>
</table>
</section>

<section id="profiling">
### Profiling

These parameters configure Nephele's profiling features.

<table class="table table-striped">
<thead>
<tr class="header">
<th>Key</th>
<th>Description</th>
<th>Default Value</th>
</tr>
</thead>
<tbody>
<tr>
<td>jobmanager.profiling.enable</td>
<td>Enables/disables the job manager's profiling component.</td>
<td>false</td>
</tr>
<tr>
<td>jobmanager.profiling.classname</td>
<td>The class name of the the job manager's profiling component to load if profiling is enabled.</td>
<td>null</td>
</tr>
<tr>
<td>taskmanager.profiling.classname</td>
<td>The class name of the task manager's profiling component to load if profiling is enabled.</td>
<td>null</td>
</tr>
<tr>
<td>taskmanager.profiling.rpc.numhandler</td>
<td>Number of threads for the profiler.</td>
<td>3</td>
</tr>
<tr>
<td>taskmanager.profiling.reportinterval</td>
<td>The interval in which a task manager is supposed to send profiling data to the job manager (s).</td>
<td>2</td>
</tr>
<tr>
<td>jobmanager.profiling.rpc.port</td>
<td>The job manager's profiling RPC port.</td>
<td>6124</td>
</tr>
</tbody>
</table>
</section>

<section id="visualization">
### Visualization

These parameters configure Nephele's visualization client.

<table class="table table-striped">
<thead>
<tr class="header">
<th>Key</th>
<th>Description</th>
<th>Default Value</th>
</tr>
</thead>
<tbody>
<tr>
<td>visualization.bottleneckDetection.enable</td>
<td>Boolean: enable Bottleneck Detection</td>
<td>false</td>
</tr>
</tbody>
</table>
</section>