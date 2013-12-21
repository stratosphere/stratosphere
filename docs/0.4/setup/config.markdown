---
layout: inner_docs_v04
title:  "Configuration"
sublinks:
  - {anchor: "common", title: "Most Common Options"}
  - {anchor: "hdfs", title: "HDFS"}
  - {anchor: "jm_tm", title: "Job &amp; TaskManager"}
  - {anchor: "jmweb", title: "JobManager Web"}
  - {anchor: "web_frontend", title: "Web Frontend"}
  - {anchor: "compiler", title: "Compiler"}
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
<td>jobmanager.heap.mb</td>
<td>JVM heap size (in megabytes) for the JobManager.</td>
<td>256</td>
</tr>
<tr>
<td>taskmanager.heap.mb</td>
<td>JVM heap size (in megabytes) for the TaskManager. In contrast to Hadoop, Stratosphere runs operators and functions inside the TaskManager (including sorting/hashing/caching), so this value should be as large as possible.</td>
<td>512</td>
</tr>
<tr>
<td>taskmanager.tmp.dirs</td>
<td>The directory for temporary files, or a list of directories separated by the systems directory delimiter (for example ':' (colon) on Linux/Unix). If multiple directories are specified then the temporary files will be distributed across the directories in a round robin fashion. The I/O manager component will spawn one reading and one writing thread per directory. A directory may be listed multiple times to have the I/O manager use multiple threads for it (for example if it is physically stored on a very fast disc or RAID).</td>
<td>The system's tmp dir</td>
</tr>
<tr>
<td>parallelization.degree.default</td>
<td>The default degree of parallelism to use for programs that have no degree of parallelism specified. A value of -1 indicates no limit, in which the degree of parallelism is set to the number of available instances at the time of compilation.</td>
<td>-1</td>
</tr>
<tr>
<td>parallelization.intra-node.default</td>
<td>The number of parallel instances of an operation that are assigned to each TaskManager. A value of -1 indicates no limit.</td>
<td>-1</td>
</tr>
<tr>
<td>taskmanager.network.numberOfBuffers</td>
<td>The number of buffers available to the network stack. This number determines how many streaming data exchange channels a TaskManager can have at the same time and how well buffered the channels are. If a job is rejected or you get a warning that the system has not enough buffers available, increase this value.</td>
<td>2048</td>
</tr>
<tr>
<td>taskmanager.memory.size</td>
<td>The amount of memory (in megabytes) that the task manager reserves for sorting, hash tables, and caching of intermediate results. If unspecified (-1), the memory manager will take a fixed ratio of the heap memory available to the JVM after the allocation of the network buffers (0.8).</td>
<td>-1</td>
</tr>
<tr>
<td>jobmanager.profiling.enable</td>
<td>Flag to enable job manager's profiling component. This collects network/cpu utilization statistics, which are displayed as charts in the SWT visualization GUI.</td>
<td>false</td>
</tr>
</tbody>
</table>
</section>

<section id="hdfs">
### HDFS

These parameters configure the default HDFS used by Stratosphere. If you don't specify a HDFS configuration, you will have to specify the full path to your HDFS files like `hdfs://address:port/path/to/files` and filed with be written with default HDFS parameters (block size, replication factor).

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
<td>fs.hdfs.hadoopconf</td>
<td>The absolute path to the Hadoop configuration directory. The system will look for the "core-site.xml" and "hdfs-site.xml" files in that directory.</td>
<td>null</td>
</tr>
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

<section id="jm_tm">
### Job & TaskManager

The following parameters configure Stratosphere's JobManager, TaskManager,
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
<td>jobmanager.rpc.address</td>
<td>The hostname or IP address of the JobManager.</td>
<td>localhost</td>
</tr>
<tr>
<td>jobmanager.rpc.port</td>
<td>The port of the JobManager.</td>
<td>6123</td>
</tr>
<tr>
<td>jobmanager.rpc.numhandler</td>
<td>The number of RPC threads for the JobManager. Increase those for large setups in which many TaskManagers communicate with the JobManager simultaneously</td>
<td>8</td>
</tr>
<tr>
<td>jobmanager.profiling.enable</td>
<td>Flag to enable the profiling component. This collects network/cpu utilization statistics, which are displayed as charts in the SWT visualization GUI. The profiling may add a small overhead on the execution.</td>
<td>false</td>
</tr>
<tr>
<td>jobmanager.web.port</td>
<td>Port of the JobManager's web interface.</td>
<td>8081</td>
</tr>
<tr>
<td>jobmanager.heap.mb</td>
<td>JVM heap size (in megabytes) for the JobManager.</td>
<td>256</td>
</tr>
<tr>
<td>taskmanager.heap.mb</td>
<td>JVM heap size (in megabytes) for the TaskManager. In contrast to Hadoop, Stratosphere runs operators and functions inside the TaskManager (including sorting/hashing/caching), so this value should be as large as possible.</td>
<td>512</td>
</tr>
<tr>
<td>taskmanager.rpc.port</td>
<td>The task manager's IPC port.</td>
<td>6122</td>
</tr>
<tr>
<td>taskmanager.data.port</td>
<td>The task manager's port used for data exchange operations.</td>
<td>6121</td>
</tr>
<tr>
<td>taskmanager.tmp.dirs</td>
<td>The directory for temporary files, or a list of directories separated by the systems directory delimiter (for example ':' (colon) on Linux/Unix). If multiple directories are specified then the temporary files will be distributed across the directories in a round robin fashion. The I/O manager component will spawn one reading and one writing thread per directory. A directory may be listed multiple times to have the I/O manager use multiple threads for it (for example if it is physically stored on a very fast disc or RAID).</td>
<td>The system's tmp dir</td>
</tr>
<tr>
<td>taskmanager.network.numberOfBuffers</td>
<td>The number of buffers available to the network stack. This number determines how many streaming data exchange channels a TaskManager can have at the same time and how well buffered the channels are. If a job is rejected or you get a warning that the system has not enough buffers available, increase this value.</td>
<td>2048</td>
</tr>
<tr>
<td>taskmanager.network.bufferSizeInBytes</td>
<td>The size of the network buffers, in bytes.</td>
<td>32768 (= 32 KiBytes)</td>
</tr>
<tr>
<td>taskmanager.memory.size</td>
<td>The amount of memory (in megabytes) that the task manager reserves for sorting, hash tables, and caching of intermediate results. If unspecified (-1), the memory manager will take a relative amount of the heap memory available to the JVM after the allocation of the network buffers (0.8).</td>
<td>-1</td>
</tr>
<tr>
<td>taskmanager.memory.fraction</td>
<td>The fraction of memory (after allocation of the network buffers) that the task manager reserves for sorting, hash tables, and caching of intermediate results. This value is only used if 'taskmanager.memory.size' is unspecified (-1). 
</td>
<td>0.8</td>
</tr>
<tr>
<td>jobclient.polling.interval</td>
<td>The interval (in seconds) in which the client polls the JobManager for the status of its job.</td>
<td>2</td>
</tr>
<tr>
<td>taskmanager.runtime.max-fan</td>
<td>The maximal fan-in for external merge joins and fan-out for spilling hash tables. Limits the numer of file handles per operator, but may cause intermediate merging/partitioning, if set too small.</td>
<td>128</td>
</tr>
<tr>
<td>taskmanager.runtime.sort-spilling-threshold</td>
<td>A sort operation starts spilling when this fraction of its memory budget is full.</td>
<td>0.8</td>
</tr>
<tr>
<td>taskmanager.runtime.fs_timeout</td>
<td>The maximal time (in milliseconds) that the system waits for a response from the filesystem. Note that for HDFS, this time may occasionally be rather long. A value of 0 indicates infinite waiting time.</td>
<td>0</td>
</tr>
</tbody>
</table>
</section>

<section id="jmweb">
### JobManager Web Frontend

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
<td>jobmanager.web.port</td>
<td>Port of the JobManager's web interface that displays status of running jobs and execution time breakdowns of finished jobs.</td>
<td>8081</td>
</tr>
<tr>
<td>jobmanager.web.history</td>
<td>The number of latest jobs that the JobManager's web front-end in its history.</td>
<td>5</td>
</tr>
</tbody>
</table>
</section>

<section id="web_frontend">
### Webclient

These parameters configure the web interface that can be used to submit jobs and review the compiler's execution plans.

<table class="table table-striped">
<tbody>
<tr>
<td>webclient.port</td>
<td>The port of the webclient server</td>
<td>8080</td>
</tr>
<tr>
<td>webclient.tempdir</td>
<td>The temp directory for the web server. Used for example for caching file fragments during file-uploads.</td>
<td>The system's temp directory</td>
</tr>
<tr>
<td>webclient.uploaddir</td>
<td>The directory into which the web server will store uploaded programs.</td>
<td>${webclient.tempdir}/webclient-jobs/</td>
</tr>
<tr>
<td>webclient.plandump</td>
<td>The directory into which the web server will dump temporary JSON files describing the execution plans.</td>
<td>${webclient.tempdir}/webclient-plans/</td>
</tr>
</tbody>
</table>
</section>


<section id="compiler">
### Compiler

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
<td>compiler.delimited-informat.max-line-samples</td>
<td>The maximum number of line samples taken by the compiler for delimited inputs. The samples are used to estimate the number of records. This value can be overridden for a specific input with the input format's parameters.</td>
<td>10</td>
</tr>
<tr>
<td>compiler.delimited-informat.min-line-samples</td>
<td>The minimum number of line samples taken by the compiler for delimited inputs. The samples are used to estimate the number of records. This value can be overridden for a specific input with the input format's parameters.</td>
<td>2</td>
</tr>
<tr>
<td>compiler.delimited-informat.max-sample-len</td>
<td>The maximal length of a line sample that the compiler takes for delimited inputs. If the length of a single sample exceeds this value (possible because of misconfiguration of the parser), the sampling aborts. This value can be overridden for a specific input with the input format's parameters.</td>
<td>2097152 (= 2 MiBytes)</td>
</tr>
</tbody>
</table>
</section>
