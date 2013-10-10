---
layout: documentation
---
Configuration Reference
=======================

This page contains a reference for all the configuration settings used
in the Stratosphere system. Options in the files *nephele-user.xml* and
*pact-user.xml* are treated equally; the configuration options are only
spread across two files to structure the configuration.

Most Common Options
-------------------

This section lists the most common options that are configured.

### Most Common Nephele Options

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>jobmanager.rpc.address</em></td>
<td align="left">The IP address of the JobManager.</td>
<td align="left"><em>“127.0.0.1”</em></td>
</tr>
<tr class="even">
<td align="left"><em>jobmanager.rpc.port</em></td>
<td align="left">The port number of the JobManager.</td>
<td align="left"><em>6123</em></td>
</tr>
<tr class="odd">
<td align="left"><em>jobmanager.profiling.enable</em></td>
<td align="left">The key to check if the job manager's profiling component should be enabled (required for load charts in NepheleGUI).</td>
<td align="left"><em>“false”</em></td>
</tr>
<tr class="even">
<td align="left"><em>taskmanager.tmp.dirs</em></td>
<td align="left">The directory for temporary files, or a list of directories separated by the systems directory delimiter (for example ':' (colon) on Linux/Unix). If multiple directories are specified then the temporary files will be distributed across the directories in a round robin fashion. The I/O manager component will spawn one reading and one writing thread per directory. A directory may be listed multiple times to have the I/O manager use multiple threads for it (for example if it is physically stored on a very fast disc or RAID).</td>
<td align="left">The system's tmp dir</td>
</tr>
<tr class="odd">
<td align="left"><em>taskmanager.memory.size</em></td>
<td align="left">The amount of memory available for the task manager's memory manager, in megabytes. The memory manager distributes memory among the different tasks, which need it for sorting, hash tables, or result caching. If unspecified (-1), the memory manager will take a fixed ratio of the heap memory available to the JVM after all Nephele services have started (0.8).</td>
<td align="left"><em>-1</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.network.numberOfBuffers</em></td>
<td align="left">The default number of buffers available to the system. This number determines how many channels a TaskManager can have at the same time and how well buffered the channels are. If a job is rejected or you get a warning that the system has too little buffers available, increase this value.</td>
<td align="left"><em>256</em></td>
</tr>
</tbody>
</table>

### Most Common PACT Options

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>pact.parallelization.degree</em></td>
<td align="left">The default degree of parallelism to use for pact programs that have no degree of parallelism specified. A value of -1 indicates no limit, in which the degree of parallelism is set to the number of available instances (at the time of compilation) times the intra-node parallelism of each instance.</td>
<td align="left"><em>-1</em></td>
</tr>
<tr class="even">
<td align="left"><em>pact.parallelization.max-intra-node-degree</em></td>
<td align="left">The maximal number of parallel instances of the user function that are assigned to a single computing instance. A value of -1 indicates no limit. If the desired degree of parallelism is not achievable with the given number of instances and the given upper limit per instance, then the degree of parallelism may be reduced by the compiler.</td>
<td align="left"><em>-1</em></td>
</tr>
</tbody>
</table>

* * * * *

The following sections give a conclusive overview of all configuration
parameters.

PACT
----

### Pact Compiler

The following parameters configure the PACT compiler and have therefore
an impact on the scheduling and execution of PACT programs on Nephele.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>pact.parallelization.degree</em></td>
<td align="left">The default degree of parallelism to use for pact programs that have no degree of parallelism specified. A value of -1 indicates no limit, in which the degree of parallelism is set to the number of available instances at the time of compilation.</td>
<td align="left"><em>-1</em></td>
</tr>
<tr class="even">
<td align="left"><em>pact.parallelization.max-intra-node-degree</em></td>
<td align="left">The maximal number of parallel instances of the user function that are assigned to a single computing instance. A value of -1 indicates no limit. If the desired degree of parallelism is not achievable with the given number of instances and the given upper limit per instance, then the degree of parallelism may be reduced by the compiler.</td>
<td align="left"><em>1</em></td>
</tr>
<tr class="odd">
<td align="left"><em>pact.parallelization.maxmachines</em></td>
<td align="left">An optional hard limit in the number of machines (Nephele Instances) to use. A program will never use more than the here specified number of machines. If set to '-1', the limit is set by the maximal number of instances available in the cluster. If this value is set, the actual number of machines used for certain tasks may be even lower than this value, due to scheduling constraints.</td>
<td align="left"><em>-1</em></td>
</tr>
</tbody>
</table>

### Web Frontend

These parameters configure the PACT web interface. For information on
how to start, use, and configure the PACT web interface, refer to
[here](executepactprogram.html "executepactprogram").

<table>
<tbody>
<tr class="odd">
<td align="left"><em>pact.web.port</em></td>
<td align="left">The port of the frontend web server</td>
<td align="left"><em>8080</em></td>
</tr>
<tr class="even">
<td align="left"><em>pact.web.rootpath</em></td>
<td align="left">The path to the root directory containing the web documents</td>
<td align="left"><em>”./resources/web-docs/”</em></td>
</tr>
<tr class="odd">
<td align="left"><em>pact.web.temp</em></td>
<td align="left">The temp directory for the web server. Used for example for caching file fragments during file-uploads.</td>
<td align="left"><em>”/tmp”</em></td>
</tr>
<tr class="even">
<td align="left"><em>pact.web.uploaddir</em></td>
<td align="left">The directory into which the web server will store uploaded pact programs.</td>
<td align="left"><em>”/tmp/pact-jobs/”</em></td>
</tr>
<tr class="odd">
<td align="left"><em>pact.web.plandump</em></td>
<td align="left">The directory into which the web server will dump temporary JSON files describing pact plans.</td>
<td align="left"><em>”/tmp/pact-plans/”</em></td>
</tr>
</tbody>
</table>

* * * * *

Nephele
-------

### nephele-common

These parameters are relevant for configuration of Nephele channels.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>channel.file.compressor</em></td>
<td align="left">The name of the Compressor class used by the output channel whose type is FILE.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.network.compressor</em></td>
<td align="left">The name of the Compressor class used by the output channel whose type is NETWORK.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="odd">
<td align="left"><em>channel.file.decompressor</em></td>
<td align="left">The name of the Decompressor class used by the input channel whose type is FILE.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.network.decompressor</em></td>
<td align="left">The name of the Decompressor class used by the input channel whose type is NETWORK.</td>
<td align="left"><em>null</em></td>
</tr>
</tbody>
</table>

<table>
<tbody>
<tr class="odd">
<td align="left"><em>channel.compression.lightClass</em></td>
<td align="left">Library class for compression level light.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.compression.mediumClass</em></td>
<td align="left">Library class for compression level medium.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="odd">
<td align="left"><em>channel.compression.heavyClass</em></td>
<td align="left">Library class for compression level heavy.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.compression.dynamicClass</em></td>
<td align="left">Library class for dynamic compression level.</td>
<td align="left"><em>null</em></td>
</tr>
</tbody>
</table>

### nephele-ec2cloudmanager

These parameters are relevant if you want to setup Nephele in a Cloud
environment. See [Cloud
Setup](cloudsetup.html "cloudsetup")
for information how to setup Nephele in Cloud mode.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.leaseperiod</em></td>
<td align="left">The lease period of cloud instances (in ms). The cloud manager tries to keep unused, but already launched, instances running as backup until the next full lease period is reached (many IaaS providers use a lease period of 1 hour)</td>
<td align="left">1 hour</td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.ec2.cleanupinterval</em></td>
<td align="left">The interval of cleaning up of a CloudInstance. If the CloudManager does not receive the heartbeat from the CloudInstance for more than the interval, the CloudManager will regard it as dead. The interval is measured in milliseconds. The valid interval should be a multiple of 10 seconds, otherwise it is invalid.</td>
<td align="left"><em>2 * 60 * 1000 (2 min)</em></td>
</tr>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.availabilityzone</em></td>
<td align="left">The preferred EC2 availability zone</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.ec2.defaultawsaccessid</em></td>
<td align="left">The Amazon EC2 AWS Access ID (this can also be set on a per-job basis). If a job-specific access ID is provided, it is preferred over the global value. Access ID and secret key are needed in order to allocate worker instances.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.defaultawssecretkey</em></td>
<td align="left">The corresponding EC2 secret key</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.ec2.endpoint</em></td>
<td align="left">The webservice endpoint (default: ec2.eu-west-1.amazonaws.com)</td>
<td align="left"><em>AWS EU</em></td>
</tr>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.defaultami</em></td>
<td align="left">The (AMI) image ID of the image to be started for the worker nodes. Note: in case of AWS, this ID is region-specific. This can also be set job-specific.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.ec2.type.i</em></td>
<td align="left">The instance type for instance number <em>i</em>.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.defaulttype</em></td>
<td align="left">The default instance type.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"></td>
<td align="left"></td>
<td align="left"></td>
</tr>
</tbody>
</table>

### nephele-ec2cloudmanager (job-specific parameters)

these parameters must be supplied using job-specific configurations.

<table>
<tbody>
<tr class="odd">
<td align="left"><em>job.ec2.awsaccessid</em></td>
<td align="left">The access key for AWS (mandatory if no global key is set). If job-specific id/key is present it will be preferred over the global configuration.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>job.ec2.awssecretkey</em></td>
<td align="left">The correspondign secret key for AWS (mandatory if no global key is set)</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="odd">
<td align="left"><em>job.ec2.sshkeypair</em></td>
<td align="left">Name of the AWS SSH keypair to access worker instances (this optional)</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>job.ec2.ami</em></td>
<td align="left">Job-specific image ID of the image to be used (will be preferred over global conf))</td>
<td align="left"><em>null</em></td>
</tr>
</tbody>
</table>

### nephele-clustermanager

The following parameters configure Nephele's
[ClusterManager](clustermanager "clustermanager")
and the types of instances managed by it.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>instancemanager.cluster.cleanupinterval</em></td>
<td align="left">The cleanup interval (in seconds). Instances that do not report via a heartbeat within the interval to the ClusterManager are removed.</td>
<td align="left"><em>2 * 60 (2 min)</em></td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.cluster.defaulttype</em></td>
<td align="left">The index of the default instance type.</td>
<td align="left"><em>1</em></td>
</tr>
<tr class="odd">
<td align="left"><em>config.dir</em></td>
<td align="left">The config directory dir.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.cluster.type.n</em> , with n: int &gt; 0</td>
<td align="left">n is an id for an instance type.<br /> Instance types are defined as “instanceName,numComputeUnits,numCores,memorySize,diskCapacity,pricePerHour”.<br /> “m1.small,2,1,2048,10,10” describes an instance type named “m1.small” with 2 computational units, 1 CPU core, 2048 MB main memory and 10 GB disk capacity. It is charged 10 for running an instance of this type per hour.</td>
<td align="left"><em>null</em></td>
</tr>
</tbody>
</table>

### nephele-hdfs

These parameters configure the default HDFS used by Nephele.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>fs.hdfs.hdfsdefault</em></td>
<td align="left">The absolute path of Hadoop's own configuration file “hdfs-default.xml”.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>fs.hdfs.hdfssite</em></td>
<td align="left">The absolute path of Hadoop's own configuration file “hdfs-site.xml”.</td>
<td align="left"><em>null</em></td>
</tr>
</tbody>
</table>

### nephele-profiling

These parameters configure Nephele's profiling features.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>jobmanager.profiling.enable</em></td>
<td align="left">Enables/disables the job manager's profiling component.</td>
<td align="left"><em>“false”</em></td>
</tr>
<tr class="even">
<td align="left"><em>jobmanager.profiling.classname</em></td>
<td align="left">The class name of the the job manager's profiling component to load if profiling is enabled.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="odd">
<td align="left"><em>taskmanager.profiling.classname</em></td>
<td align="left">The class name of the task manager's profiling component to load if profiling is enabled.</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>taskmanager.profiling.rpc.numhandler</em></td>
<td align="left">Number of threads for the profiler.</td>
<td align="left"><em>3</em></td>
</tr>
<tr class="odd">
<td align="left"><em>taskmanager.profiling.reportinterval</em></td>
<td align="left">The interval in which a task manager is supposed to send profiling data to the job manager (s).</td>
<td align="left"><em>2</em></td>
</tr>
<tr class="even">
<td align="left"><em>jobmanager.profiling.rpc.port</em></td>
<td align="left">The job manager's profiling RPC port.</td>
<td align="left"><em>6124</em></td>
</tr>
</tbody>
</table>

### nephele-server

The following parameters configure Nephele's JobManager, TaskManager,
and runtime channel management.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>discoveryservice.magicnumber</em></td>
<td align="left">The discovery service's magic number</td>
<td align="left"><em>0</em></td>
</tr>
<tr class="even">
<td align="left"><em>discoveryservice.port</em></td>
<td align="left">The port which the discovery service runs on.</td>
<td align="left"><em>7001</em></td>
</tr>
<tr class="odd">
<td align="left"><em>job.execution.retries</em></td>
<td align="left">The default number of times of vertex shall be reexecuted before its execution is considered as failed.</td>
<td align="left"><em>2</em></td>
</tr>
<tr class="even">
<td align="left"><em>jobclient.polling.interval</em></td>
<td align="left">The recommended client polling interval (seconds).</td>
<td align="left"><em>5</em></td>
</tr>
<tr class="odd">
<td align="left"><em>jobmanager.rpc.address</em></td>
<td align="left">The IP address of the JobManager.</td>
<td align="left"><em>“127.0.0.1”</em></td>
</tr>
<tr class="even">
<td align="left"><em>jobmanager.rpc.port</em></td>
<td align="left">The port number of the JobManager.</td>
<td align="left"><em>6123</em></td>
</tr>
<tr class="odd">
<td align="left"><em>jobmanager.rpc.numhandler</em></td>
<td align="left">The number of RPC threads for the JobManager.</td>
<td align="left"><em>3</em></td>
</tr>
<tr class="even">
<td align="left"><em>jobmanager.profiling.enable</em></td>
<td align="left">Decide whether the Profiler is used or not.</td>
<td align="left"><em>false</em></td>
</tr>
<tr class="odd">
<td align="left"><em>taskmanager.rpc.port</em></td>
<td align="left">The task manager's IPC port.</td>
<td align="left"><em>6122</em></td>
</tr>
<tr class="even">
<td align="left"><em>taskmanager.data.port</em></td>
<td align="left">The task manager's data port used for NETWORK channels.</td>
<td align="left"><em>6121</em></td>
</tr>
<tr class="odd">
<td align="left"><em>taskmanager.setup.usediscovery</em></td>
<td align="left">!Enables/Disables discovery service for the TaskManager. If enabled, the TaskManager will use discovery service to find the JobManager in the network.</td>
<td align="left"><em>true</em></td>
</tr>
<tr class="even">
<td align="left"><em>taskmanager.setup.periodictaskinterval</em></td>
<td align="left">The interval of periodic tasks (Heartbeat, check Task Execution) by the TaskManager. The interval is measured in milliseconds.</td>
<td align="left"><em>1000</em></td>
</tr>
<tr class="odd">
<td align="left"><em>taskmanager.memory.size</em></td>
<td align="left">The amount of memory available for the task manager's memory manager, in megabytes. The memory manager distributes memory among the different tasks, which need it for sorting, hash tables, or result caching. If unspecified (-1), the memory manager will take a fixed ratio of the heap memory available to the JVM after all Nephele services have started (0.8).</td>
<td align="left"><em>-1</em></td>
</tr>
<tr class="even">
<td align="left"><em>taskmanager.tmp.dirs</em></td>
<td align="left">The directory for temporary files, or a list of directories separated by the systems directory delimiter (for example ':' (colon) on Linux/Unix). If multiple directories are specified then the temporary files will be distributed across the directories in a round robin fashion. The I/O manager component will spawn one reading and one writing thread per directory. A directory may be listed multiple times to have the I/O manager use multiple threads for it (for example if it is physically stored on a very fast disc or RAID).</td>
<td align="left"><code>The system's tmp dir</code></td>
</tr>
<tr class="odd">
<td align="left"><em>channel.network.numberOfBuffers</em></td>
<td align="left">The default number of buffers available to the system. This number determines how many channels a TaskManager can have at the same time and how well buffered the channels are. If a job is rejected or you get a warning that the system has too little buffers available, increase this value.</td>
<td align="left"><em>256</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.network.bufferSizeInBytes</em></td>
<td align="left">The default number of read buffers.</td>
<td align="left"><em>64 * 1024 (64 k)</em></td>
</tr>
<tr class="odd">
<td align="left"><em>channel.network.numberOfOutgoingConnectionThreads</em></td>
<td align="left">The default number of outgoing connection threads.</td>
<td align="left"><em>1</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.network.numberOfConnectionRetries</em></td>
<td align="left">The default number of connection retries.</td>
<td align="left"><em>10</em></td>
</tr>
<tr class="odd">
<td align="left"><em>channel.network.allowSenderSideSpilling</em></td>
<td align="left">!Enables/Disables spilling of network channels. <br /> Spilling happens if a task does not consume data as fast as it arrives on the network. <br /> Spilling can prevent deadlocks but can cause bad performance.</td>
<td align="left"><em>false</em></td>
</tr>
<tr class="even">
<td align="left"><em>channel.network.mergeSpilledBuffers</em></td>
<td align="left">Enables/disables merging of spilled buffers</td>
<td align="left"><em>true</em></td>
</tr>
<tr class="odd">
<td align="left"><em>channel.inMemory.numberOfConnectionRetries</em></td>
<td align="left">The number of connection retries.</td>
<td align="left"><em>30</em></td>
</tr>
<tr class="even">
<td align="left"><em>checkpoint.local.path</em></td>
<td align="left">Path in the local filesystem used for checkpointing</td>
<td align="left"><em>temp dir detected by the JVM</em></td>
</tr>
<tr class="odd">
<td align="left"><em>checkpoint.distributed.path</em></td>
<td align="left">Path in a distributed FS used for distributed checkpointing</td>
<td align="left"><em>null</em></td>
</tr>
<tr class="even">
<td align="left"><em>checkpoint.mode</em></td>
<td align="left">Checkpointing strategy to be used (always,network,never)</td>
<td align="left"><em>never</em></td>
</tr>
</tbody>
</table>

### nephele-visualization

These parameters configure Nephele's visualization client.

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Description</th>
<th align="left">Default Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>visualization.bottleneckDetection.enable</em></td>
<td align="left">Boolean: enable Bottleneck Detection</td>
<td align="left"><em>false</em></td>
</tr>
</tbody>
</table>


