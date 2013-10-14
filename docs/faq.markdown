---
layout: documentation
---

Frequently Asked Questions
==========================

Usage
-----

**How do I assess the progress of a PACT program (or Nephele Job)?**

There are a couple of ways to track the progress of a PACT program or
Nephele job:

-   Use Nephele's visualization tool. The tool reports the states of all
    subtasks. If *profiling* is enabled (see [Configuration
    Reference](configreference.html "configreference")),
    the load of all threads and all involved instances is displayed as
    well. See NepheleGUI to learn how to configure, run and use it.
-   Execute a PACT program on the PACT command line client with *“wait”*
    option (-w). The client waits until the job is finished and prints
    its progress (status changes of all subtasks) to standard out. See
    the [PACT client
    documentation](executepactprogram.html "executepactprogram")
    for details.
-   All status changes are logged to the JobManager's log file. Track it
    with: `tail -f ../log/nephele-<user>-jobmanager-<host>.log`.

**How can I figure out why a subtask crashed?**

-   If you run a PACT program using the PACT command-line client with
    enabled *wait* flag, task exceptions are printed to the standard
    err. See ExecutePactProgram for details.
-   Failing tasks and the corresponding exceptions are reported in the
    JobManager's log file
    (../log/nephele-\<user\>-jobmanager-\<host\>.log).
-   If you need more detailed information, see the TaskManager's log
    file on which node the failing subtask ran. You can determine the
    instance that executed the failing subtask from the JobManager's log
    file.

**How can I start the system from within Eclipse, when I checked out the
source code?**

The [Stratosphere Development with
Eclipse](eclipseimport.html "eclipseimport")
guide explains how to import the source code into Eclipse and run /
debug Nephele in local mode directly from Eclipse.

Errors
------

**I tried to submit a PACT job via the web frontend and got an error
message that did not help. How do I find out what went wrong?**

Please have a look in the web-frontends log file
(./log/nephele-\<user\>-pact-web-\<host\>.log). All exceptions are
logged there.

**My PACT program does not compute the correct result. There seems to be
an issue with the handling of keys. What is wrong?**

The PACT execution framework relies on correct implementation of methods
*java.lang.Object\#hashCode()* and *java.lang.Object\#equals(Object o)*
methods. These methods are always backed with default implementations,
that cause the PACT framework to compute wrong results. Therefore, all
keys must override *hashCode()* and *equals(Object o)*. Unfortunately,
IDEs and Java compilers will not force you to override these methods,
due to the default implementation of *java.lang.Object*.

**\*I get a *java.lang.InstantiationException* for my data type, what is
wrong?**

All data type classes must have a public nullary constructor
(constructor with no arguments). If it misses, that exception will be
thrown. Further more, the classes must not be abstract. If the classes
are internal classes, they must be public and static.

**I get a `java.lang.UnsatisfiedLinkError` when starting the runtime
visualization. How can I fix that?**

Nephele's runtime visualization is a SWT application running. It
requires the appropriate native library for SWT (Standard Widget
Toolkit), specific to your platform. The one that is packaged with
Stratosphere is for 64bit Linux GTK systems. If you have a different
system, you need a different SWT library.

To fix the problem, update the maven dependency in
*[/nephele/nephele-visualization/pom.xml](https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-visualization/pom.xml "https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-visualization/pom.xml")*
to refer to your platform specific library.

You can find a the list of available library versions under
[http://repo1.maven.org/maven2/org/eclipse/swt/](http://repo1.maven.org/maven2/org/eclipse/swt/ "http://repo1.maven.org/maven2/org/eclipse/swt/").

**My job fails early with a `java.io.EOFException`. What could be the
cause?**

Those exceptions typically occur when the running HDFS version is
incompatible with the version that is used by Nephele to connect to the
HDFS. The fix is simply to recompile Stratosphere and use the desired
Hadoop Version. Verfiy that you exception has a stack trace like the
following:

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
      at eu.stratosphere.nephele.fs.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:117)
      at eu.stratosphere.nephele.fs.FileSystem.get(FileSystem.java:224)
      at eu.stratosphere.nephele.fs.Path.getFileSystem(Path.java:296)
      ...

To solve that problem, follow these steps:

1.  Check which Version you have.
2.  Checkout the Stratosphere source code. The guide is in [Building the
    System](buildthesystem.html "buildthesystem")
3.  Open the file pom.xml in the stratosphere root directory. It should
    contain an entry

        <dependencyManagement>
                <!--
                     this section defines the module versions that are used if nothing
                     else is specified.
                -->
                <dependencies>
                       <dependency>
                             <groupId>org.apache.hadoop</groupId>
                             <artifactId>hadoop-core</artifactId>
                              <version>0.20.2</version>
                              <type>jar</type>
                              <scope>compile</scope>
                       </dependency>
               </dependencies>
        </dependencyManagement>

4) Change the version from 0.20.2 to the version that you are using

5) Build the system as described in [Building the
System](buildthesystem.html "buildthesystem")

**I can't stop Nephele with the provided stop-scripts. What can I do?**

In some error cases it happens that the JobManager or TaskManager cannot
be stopped with the provided stop-scripts (`bin/stop-local.sh`,
`bin/stop-cluster.sh`, `bin/stop-cloud.sh`).   
 You can kill their processes as follows:

-   Determine the process id (pid) of the JobManager / TaskManager
    process. You can use the `jps` command for that.
-   Kill the process with `kill -9 <pid>`, where `pid` is the process id
    of the affected JobManager or TaskManager process.

**Why do I get a `MemoryAllocationException`?**

The memory of a TaskManager is managed and organized by an internal
component called MemoryManager. During start-up, the MemoryManager
allocates a certain amount of main memory and provides it to PACT
subtasks that requested it (e.g., for sorting, hash-tables, user code).
The PACT compiler is responsible for distributing the available memory
between tasks. A `MemoryAllocationException` is thrown if a task
requests more memory than available on its instance. This might happen
if more subtasks are scheduled to an instance than the PACT compiler was
assuming.

**I got an `OutOfMemoryException`. What can I do?**

OutOfMemoryExceptions in Java are kind of tricky. The exception is not
necessarily thrown by the component that allocated most of the memory
but by the component that tried to allocate more memory than was
available even though that requested memory itself was little.   

First, you should check whether your program consumed too much memory
and try to minimize the memory footprint of your code. Because
Stratosphere accumulates data internally in a byte representation in
pre-reserved memory, it is mostly the user code that consumes too much
memory.  

If your user code simply has a large memory requirement, you can
decrease the size of the memory managed by the MemoryManager using the
parameter `taskmanager.memory.size`. This will leave more memory to JVM
heap. See the [Configuration
Reference](configreference.html "configreference")
for details.

**Why do the TaskManager log files become so huge?**

Check the logging behavior of your jobs. You should not emit log
statements on pair or tuple level. Although, this might be helpful to
debug jobs in small setups with tiny data sets, it becomes very
inefficient and disk space consuming if used for large input data.

Tuning
------

**My PACT or Nephele programs make very slow progress. What can I do?**

If you run Nephele in a massively parallel setting (100+ parallel
threads), you should adapt the number of write buffers. As a
rule-of-thumb, the number of write buffers should be   
 *(2 \* numberOfNodes \* numberOfTasksPerNode%%\^2 )* See [Configuration
Reference](configreference.html "configreference")
for details.

Features
--------

**What kind of fault-tolerance does Stratosphere provide?**

At the current state, Stratosphere's fault tolerance is experimental and
not enabled by default. In future releases we will stabilize the feature
and enable it by default.

**Are Hadoop-like utilities, such as Counters and the DistributedCache
supported?**

At the moment, both are not supported. If the distributed cache's task
was to replicate user data to nodes, check whether the [Cross
Contract](pactpm#cross "pactpm")
can solve the problem.
