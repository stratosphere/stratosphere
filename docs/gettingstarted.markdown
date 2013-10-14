---
layout: documentation
title: Getting Started
---

<!-- <ol class="breadcrumb">
  <li><a href="/">Home</a></li>
  <li><a href="/docs/">Documentation</a></li>
  <li><a href="/docs/setupandconfig.html">Setup &amp; Configuration</a></li>
  <li class="active">Getting Started</li>
</ol> -->


Getting Started
===============

This guide describes the local installation of Stratosphere for
operation on a single machine in a non-distributed fashion. All data
will be accessed through the local file system; hence, no additional
distributed file system is needed.  

For distributed cluster and cloud setups, Stratosphere supports the
[Hadoop Distributed File
System](http://hadoop.apache.org/hdfs/ "http://hadoop.apache.org/hdfs/")
(HDFS). A complete guide for distributed setups can be found in the
following wiki pages.

-   [Cluster
    Setup](clustersetup.html "clustersetup")
-   [Configuration
    Reference](configreference.html "configreference")

In its current version, Stratosphere consists of the parallel dataflow
engine
[Nephele](nephele.html "nephele")
and the
[PACT](pact.html "pact")
programming model and compiler. Please consult the following wiki pages
for detailed information on the system and its features.

-   [System
    Architecture](systemarchitecture.html "systemarchitecture")
-   [Nephele Execution
    Engine](nephele.html "nephele")
-   [PACT Programming
    Model](pactpm.html "pactpm")

Installing Stratosphere
=======================

Supported Platform and Requirements
-----------------------------------

Stratosphere is implemented in Java and currently tested on Ubuntu,
Debian and Suse Linux systems. To run Stratosphere, you need a Java 1.6
compatible JRE. We highly recommend to use Sun's JRE instead of the
OpenJDK, the default Java runtime on many Linux distributions.

Download, Install, and Configure Stratosphere
---------------------------------------------

-   [Download](http://stratosphere.eu/downloads "http://stratosphere.eu/downloads")
    the latest release.

-   Unpack the source code archive   

TAR archive:

       tar xvfz stratosphere-dist-0.2-bin.tar.gz

**OR** ZIP archive:

    unzip stratosphere-dist-0.2-bin.zip

-   Change into the extracted directory:

<!-- -->

    cd stratosphere-0.2

-   The default configuration is tailored towards the local setup to
    reduce the initial configuration efforts.   

By default, Nephele's JVM requires 768MB of memory to start. To change
the amount of memory dedicated to Nephele's JVM, adjust the parameters
`JM_JHEAP` and `TM_JHEAP` in `./bin/nephele-config.sh`. `JM_JHEAP`
defines the amount of memory for the JobManager, which is the master or
coordinator node, while `TM_JHEAP` defines the amount of memory for the
workers (TaskManager). In the local setup, only a single instance
exists, which acts as both master and worker, so bear in mind that the
sum of both values defines the memory size for local setups.

    nano ./bin/nephele-config.sh

Start Nephele engine and the PACT Web Client
--------------------------------------------

-   Start the Nephele execution engine in local execution mode:

<!-- -->

    ./bin/start-local.sh

To check, whether Nephele was successfully started you can:

-   Check the JobManager log file in `./log`
-   Check whether a JobManager Java process is running using `jps`

-   Start the PACT Web-Client:

<!-- -->

    ./bin/start-pact-web.sh

To check, whether the web client was successfully started you can:

-   Check the pact-web log file in `./log`
-   Call <http://localhost:8080> with your web browser
-   Check for a Java process named WebFrontend using `jps`.

-   You find more details on PACT's clients including the web interface
    in the section about [executing PACT
    programms](executepactprogram.html "executepactprogram").

Run WordCount Example PACT Program
==================================

The distribution contains a couple of example data analysis programs.
See [Pact
Examples](pactexamples.html "pactexamples")
for a list of provided PACT examples and detailed documentation. In this
guide, we will discuss how to setup and execute the classic MapReduce
WordCount example using the PACT web client. The other clients are
described
[here](executepactprogram.html "executepactprogram").

Example Resources
-----------------

You will find all relevant resource for the WordCount example at the
locations listed below.

-   PACT Program Jar File:

<!-- -->

    ./examples/pact/pact-examples-0.2-WordCount.jar

-   Source Code:
    [/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/wordcount/WordCount.java](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/wordcount/WordCount.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/wordcount/WordCount.java")
-   Documentation: [WordCount Example
    Documentation](wordcountexample.html "wordcountexample")

Obtain and Store Test Data
--------------------------

The WordCount example PACT program requires a plain text file as input.
You can download Shakespeare's Hamlet from [Project
Gutenberg](http://www.gutenberg.org/wiki/Main_Page "http://www.gutenberg.org/wiki/Main_Page").
  
 For example

    wget -O ~/hamlet.txt http://www.gutenberg.org/cache/epub/1787/pg1787.txt

will download Hamlet and put the text into a file called `hamlet.txt` in
your home directory.

Nephele can read data from local file systems and the [Hadoop
Distributed File System
(HDFS)](http://hadoop.apache.org/hdfs/ "http://hadoop.apache.org/hdfs/").
For a local setup, reading from a local file system is the easiest way.
In distributed setups, we strongly advise the use of HDFS. If you want
to set up a HDFS for local mode you should follow Apache's [Hadoop Quick
Start](http://hadoop.apache.org/common/docs/r0.20.0/quickstart.html "http://hadoop.apache.org/common/docs/r0.20.0/quickstart.html")
guide.

Executing a PACT program
------------------------

The Stratosphere distribution comes with several clients for PACT job
submission. In this guide we will use the web client. The usage of the
command-line client and the embedded client is described in the section
on [Pact
Clients](executepactprogram.html "executepactprogram").
  
 To execute the WordCount example using the PACT web client by following
these steps:

-   Enter the web clients URL in your web browser:
    <http://localhost:8080>.
-   Upload the example jobs jar file from
    `./examples/pact/pact-examples-0.2-WordCount.jar`.
-   Enter the parameters for job execution (degree-of-parallelism,
    input-file, output-file).   

For example:

    4 file://<yourHome>/hamlet.txt file://<yourHome>/wordCountResult

-   Hit `run` to compile and run the example job.

The result will be stored in a single file if you used 1 as
parallelization degree. Otherwise, the result is stored in a directory
that contains a result file for each degree.

If you want to read from or write to an HDFS, you have to fully specify
those locations, like `hdfs://<hdfsHostName>:<hdfsPort>/<pathInHDFS>`.

You can monitor the progress of your started PACT program by checking
the JobManager's log file in `./log/`:

    tail -f ./log/nephele-<yourUsername>-jobmanager-<yourHostname>.log

Run K-Means Example PACT Program
================================

The WordCount program perfectly fits the MapReduce programming model and
is commonly used for introduction. However, the PACT programming model
is more expressive than MapReduce. To highlight some of the advantages
of PACT over MapReduce we also present an example which fits PACT better
than MapReduce. We will discuss how to setup and execute the K-Means
example that performs one iteration of the well known [K-Means
clustering
algorithm](http://en.wikipedia.org/wiki/K-means_clustering "http://en.wikipedia.org/wiki/K-means_clustering").

Example Resources
-----------------

You will find all relevant resources for the K-Means iteration example
at the location given below.

-   PACT Job Jar File:

<!-- -->

    ./examples/pact/pact-examples-0.2-KMeansIteration.jar

-   Source Code:
    [/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/datamining/KMeansIteration.java](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/datamining/KMeansIteration.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/datamining/KMeansIteration.java")
-   Documentation: [K-Means Example
    Documentation](kmeansexample.html "kmeansexample")

Generate and Upload Test Data
-----------------------------

Please consult the example documentation for information on how to
generate synthetic test data for the K-Means example job.

The subsequent descriptions assume that you place the data in your home
directory in the following way:

-   data point files: `<yourHome>/datapoints`
-   cluster center file: `<yourHome>/iter_0`

Example Execution
-----------------

Execute the K-Means iteration example using the PACT web client by
following these steps:

-   Enter the web clients URL in your web browser:
    <http://localhost:8080>.
-   Upload the example jobs jar file from
    `./examples/pact/pact-examples-0.2-KMeansIteration.jar`.
-   Enter the parameters for job execution (degree-of-parallelism,
    input-directory-data-points, input-file-cluster-centers,
    output-directory).   

For example:

    4 file://<yourHome>/dataPoints file://<yourHome>/iter_0 file://<yourHome>/iter_1

-   Compile and run the example job by clicking on `run`
