--- 
layout: inner_docs_v04
title:  "Frequently Asked Questions (FAQ)"
questions: 
  - {section: "true", anchor: "general", title: "General"}
  - {anchor: "stratosphere_hadoop_project", title: "Is Stratosphere a Hadoop Project?"}
  - {anchor: "stratosphere_hadoop_req", title: "Do I have to install Apache Hadoop to use Stratosphere?"}
  - {section: "true", anchor: "usage", title: "Usage"}
  - {anchor: "usage_progress", title: "How do I assess the progress of a Stratosphere program?"}
  - {anchor: "usage_crash", title: "How can I figure out why a program failed?"}
  - {anchor: "usage_debugging", title: "How do I debug Stratosphere programs?"}
  - {section: "true", anchor: "errors", title: "Errors"}
  - {anchor: "errors_buffers", title: "I get an error message saying that not enough buffers are available. How do I fix this?"}
  - {anchor: "errors_hdfs", title: "My job fails early with a java.io.EOFException. What could be the cause?"}
  - {anchor: "errors_keys", title: "My program does not compute the correct result. Why are my custom key types are not grouped/joined correctly?"}
  - {anchor: "errors_instantiation", title: "I get a <em>java.lang.InstantiationException</em> for my data type, what is wrong?"}
  - {anchor: "errors_visualization", title: "I get a <em>java.lang.UnsatisfiedLinkError</em> when starting the runtime visualization. How can I fix that?"}
  - {anchor: "errors_stop_stratosphere", title: "I can't stop Stratosphere with the provided stop-scripts. What can I do?"}
  - {anchor: "errors_out_of_memory", title: "I got an <em>OutOfMemoryException</em>. What can I do?"}
  - {anchor: "errors_huge_tm_log", title: "Why do the TaskManager log files become so huge?"}
  - {section: "true", anchor: "features", title: "Features"}
  - {anchor: "features_fault_tolerance", title: "What kind of fault-tolerance does Stratosphere provide?"}
  - {anchor: "features_hadoop", title: "Are Hadoop-like utilities, such as Counters and the DistributedCache supported?"}
---

<div class="row">
  <ul class="nav sidenav sidenav-md sidenav-row">
{% for question in page.questions %}
  {% if question('section')? %}
    <li class="section"><a href="#{{ question.anchor }}">{{ question.title }}</a></li>
  {% else %}
    <li class="subsection"><a href="#{{ question.anchor }}">{{ question.title }}</a></li>
  {% endif %}
{% endfor %}
  </ul>
</div>

<section id="general">
<div class="page-header"><h2>General</h2></div>

<section id="stratosphere_hadoop_project">
### Is Stratosphere a Hadoop Project?

Stratosphere is a data processing system and an alternative to Hadoop's MapReduce component. It comes with its own runtime, rather than building on top of MapReduce. As such, it can work completely independently of the Hadoop ecosystem. However, Stratosphere can also access Hadoop's distributed file system (HDFS) to read and write data, and Hadoop's next-generation resource manager (YARN) to provision cluster resources. Since most Stratosphere users are using Hadoop HDFS to store their data, we ship already the required libraries to access HDFS.
</section>

<section id="stratosphere_hadoop_req">

### Do I have to install Apache Hadoop to use Stratosphere?

No. Stratosphere can run without a Hadoop installation. However, a very common setup is to use Stratosphere to analyze data stored in the Hadoop Distributed File System (HDFS). To make these setups work out of the box, we bundle the Hadoop client libraries with Stratosphere by default.

Additionally, we provide a special YARN Enabled download of Stratosphere for users with an existing Hadoop YARN cluster. [Apache Hadoop YARN](http://hadoop.apache.org/docs/r2.2.0/hadoop-yarn/hadoop-yarn-site/YARN.html) is Hadoop's cluster resource manager that allows to use different execution engines next to each other on a cluster.

</section>


<section id="usage">
<div class="page-header"><h2>Usage</h2></div>

<section id="usage_progress">
### How do I assess the progress of a Stratosphere program?

There are a multiple of ways to track the progress of a Stratosphere program:

-   The JobManager (the master of the distributed system) starts a web interface to observe program execution. In runs on port 8081 by default (configured in `conf/stratosphere-config.yml`).
-   When you start a program from the command line, it will print the status changes of all operators as the program progresses through the operations.
-   The `swt-visualization` tool reports the states of all subtasks. If *profiling* is enabled (see [Configuration Reference](http://stratosphere.eu/docs/0.4/setup/config.html "Configuration Reference")), the load of all machines is displayed as well.
-   All status changes are also logged to the JobManager's log file.
</section>

<section id="usage_crash">
### How can I figure out why a program failed?

-   If you run the program from the command-line in blocking mode (with *wait* flag `--wait` or `-w`), task exceptions are printed to the standard error stream and shown on the console.
-   Both the command line and the web interface allow you to figure out which parallel task first failed and caused the other tasks to cancel the execution.
-   Failing tasks and the corresponding exceptions are reported in the log files of the master and the worker where the exception occurred (`log/stratosphere-<user>-jobmanager-<host>.log` and `log/stratosphere-<user>-taskmanager-<host>.log`).
</section>

<section id="usage_debugging">
### How do I debug Stratosphere programs?

-   When you start a program locally with the [LocalExecutor](http://stratosphere.eu/docs/0.4/program_execution/local_executor.html), you can place breakpoints in your functions and debug them like normal Java/Scala programs.
-   The [Accumulators](http://stratosphere.eu/docs/0.4/programming_guides/java.html#accumulators) are very helpful in tracking the behavior of the parallel execution. They allow you to gather information inside the program's operations and show them after the program execution.
</section>
</section>

<section id="errors">
<div class="page-header"><h2>Errors</h2></div>

<section id="errors_buffers">

### I get an error message saying that not enough buffers are available. How do I fix this?

If you run Stratosphere in a massively parallel setting (100+ parallel threads), you need to adapt the number of network buffers via the config parameter `taskmanager.network.numberOfBuffers`.
As a rule-of-thumb, the number of buffers should be at least `4 * numberOfNodes * numberOfTasksPerNode^2` See [Configuration Reference](http://stratosphere.eu/docs/0.4/setup/config.html "Configuration Reference") for details.

</section>


<section id="errors_hdfs">
### My job fails early with a <em>java.io.EOFException</em>. What could be the cause?

Note: In version <em>0.4</em>, the delta iterations limit the solution set to records with fixed-length data types. We will  in the next version.

The most common case for these exception is when Stratosphere is set up with the wrong HDFS version. Because different HDFS versions are often not compatible with each other, the connection between the filesystem master and the client breaks.

{% highlight bash %}
Call to <host:port> failed on local exception: java.io.EOFException
    at org.apache.hadoop.ipc.Client.wrapException(Client.java:775)
    at org.apache.hadoop.ipc.Client.call(Client.java:743)
    at org.apache.hadoop.ipc.RPC$Invoker.invoke(RPC.java:220)
    at $Proxy0.getProtocolVersion(Unknown Source)
    at org.apache.hadoop.ipc.RPC.getProxy(RPC.java:359)
    at org.apache.hadoop.hdfs.DFSClient.createRPCNamenode(DFSClient.java:106)
    at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:207)
    at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:170)
    at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:82)
    at eu.stratosphere.runtime.fs.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:276
{% endhighlight %}

Please refer to the [download page](http://stratosphere.eu/downloads/#maven) and the [build instructions](https://github.com/stratosphere/stratosphere/blob/master/README.md) for details on how to set up Stratosphere for different Hadoop and HDFS versions.
</section>

<section id="errors_keys">
### My program does not compute the correct result. Why are my custom key types are not grouped/joined correctly?

Keys must correctly implement the methods `java.lang.Object#hashCode()`, `java.lang.Object#equals(Object o)`, and `java.util.Comparable#compareTo(...)`. These methods are always backed with default implementations which are usually inadequate. Therefore, all keys must override `hashCode()` and `equals(Object o)`.
</section>

<section id="errors_instantiation">
### I get a <em>java.lang.InstantiationException</em> for my data type, what is wrong?

All data type classes must be public and have a public nullary constructor (constructor with no arguments).
Further more, the classes must not be abstract or interfaces.
If the classes are internal classes, they must be public and static.
</section>

<section id="errors_visualization">
### I get a <em>java.lang.UnsatisfiedLinkError</em> when starting the swt-visualization. How can I fix this?

The swt-visualization is, as the name suggests, a SWT application. It requires the appropriate native library for the SWT (Standard Widget
Toolkit) gui library. That library must be specific to your platform. The one that is packaged with Stratosphere by default is for 64bit Linux GTK systems.
If you have a different operating system, architecture, or graphics library, you need a different SWT version.

To fix the problem, update the maven dependency in
[stratosphere-addons/swt-visualization/pom.xml](https://github.com/stratosphere/stratosphere/blob/master/stratosphere-addons/swt-visualization/pom.xml "https://github.com/stratosphere/stratosphere/blob/master/stratosphere-addons/swt-visualization/pom.xml")
to refer to your platform specific library. The relevant dependency entry is 
{% highlight xml %}
<dependency>
    <groupId>org.eclipse.swt.gtk.linux</groupId>
    <artifactId>x86_64</artifactId>
    <version>3.3.0-v3346</version>
</dependency>
{% endhighlight %}
You can find a the list of available library versions under
[http://repo1.maven.org/maven2/org/eclipse/swt/](http://repo1.maven.org/maven2/org/eclipse/swt/ "http://repo1.maven.org/maven2/org/eclipse/swt/").
</section>

<section id="errors_stop_stratosphere">
### I can't stop Stratosphere with the provided stop-scripts. What can I do?

Stopping the processes sometimes takes a few seconds, because the shutdown may do some cleanup work.

In some error cases it happens that the JobManager or TaskManager cannot be stopped with the provided stop-scripts (`bin/stop-local.sh` or `bin/stop-cluster.sh`).   
You can kill their processes on Linux/Mac as follows:

-   Determine the process id (pid) of the JobManager / TaskManager process. You can use the `jps` command on Linux(if you have OpenJDK installed) or command `ps -ef | grep java` to find all Java processes. 
-   Kill the process with `kill -9 <pid>`, where `pid` is the process id of the affected JobManager or TaskManager process.
    
On Windows, the TaskManager shows a table of all processes and allows you to destroy a process by right its entry.
</section>

<section id="errors_out_of_memory">
### I got an <em>OutOfMemoryException</em>. What can I do?

These exceptions occur usually when the functions in the program consume a lot of memory by collection large numbers of objects, for example in lists or maps. The OutOfMemoryExceptions in Java are kind of tricky. The exception is not necessarily thrown by the component that allocated most of the memory but by the component that tried to requested the latest bit of memory that could not be provided.

There are two ways to go about this:
1.  See whether you can use less memory inside the functions. For example, use arrays of primitive types instead of object types.
2.  Reduce the memory that Stratosphere reserves for its own processing. The TaskManager reserves a certain portion of the available memory for sorting, hashing, caching, network buffering, etc. That part of the memory is unavailable to the user-defined functions. By reserving it, the system can guarantee to not run out of memory on large inputs, but to plan with the available memory and destage operations to disk, if necessary. By default, the system reserves around 70% of the memory. If you frequently run applications that need more memory in the user-defined functions, you can reduce that value using the configuration entries `taskmanager.memory.fraction` or `taskmanager.memory.size`. See the [Configuration Reference](http://stratosphere.eu/docs/0.4/setup/config.html "Configuration Reference") for details. This will leave more memory to JVM heap, but may cause data processing tasks to go to disk more often. 
</section>

<section id="errors_huge_tm_log">
### Why do the TaskManager log files become so huge?

Check the logging behavior of your jobs. Emitting logging per or tuple may be helpful to debug jobs in small setups with tiny data sets, it becomes very inefficient and disk space consuming if used for large input data.
</section>

</section>


<section id="features">
<div class="page-header"><h2>Features</h2></div>

<section id="features_fault_tolerance">
### What kind of fault-tolerance does Stratosphere provide?

Fault tolerance will go into the open source project in the next versions.
</section>

<section id="features_hadoop">
### Are Hadoop-like utilities, such as Counters and the DistributedCache supported?

[Stratosphere's Accumulators]({{site.baseurl}}/docs/0.4/programming_guides/java.html#accumulators) work very similar like Hadoop's counters, but are more powerful.

A distributed cache is not available right now. In many cases, operators like [cross]({{site.baseurl/docs/0.4/programming_guides/pmodel.html#operators}}) and the upcoming broadcast variables handle these situations more efficiently.
</section>

</section>