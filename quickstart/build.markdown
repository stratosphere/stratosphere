--- 
layout: inner_simple
title: Quick Start - Build
description: Learn to build Stratosphere.
keywords: stratosphere, build, setup, howto, quickstart, big data, data analytics
---

# Quickstart: Build Stratosphere

This Quickstart guide explains how to build Stratosphere from its sources. You can then install it on your computer or cluster.


<h3><a id="build"></a>1. Build</h3>

Stratosphere runs on all *UNIX-like* environments: **Linux**, **Mac OS X**, **Cygwin** on **Windows**. The only requirements are Java (6 or 7), maven (Version 3), and git.

We check out the latest version from GitHub and build it with maven.

<pre class="prettyprint" style="padding-left:1em">
git clone https://github.com/stratosphere/stratosphere.git
cd stratosphere
mvn clean package -DskipTests
</pre>

Stratosphere is now installed in `stratosphere-dist/target`.
If you’re a Debian/Ubuntu user, you’ll also find a `.deb` package here.

<h3><a id="start"></a>2. Start</h3>

Go into the build directory

<pre class="prettyprint" style="padding-left:1em">
cd stratosphere-dist/target/stratosphere-dist-{{site.current_snapshot}}-bin/stratosphere-{{site.current_snapshot}}/
</pre>

Now, we can start Stratosphere in local mode:

<pre class="prettyprint" style="padding-left:1em">
./bin/start-local.sh
</pre>

<h3><a id="run"></a>3.1 Run (using Command Line)</h3>

We will run a simple "Word Count" example. Get some test data:

<pre class="prettyprint" style="padding-left:1em">
wget -O hamlet.txt http://www.gutenberg.org/cache/epub/1787/pg1787.txt
</pre>

Start the job:

<pre class="prettyprint" style="padding-left:1em">
./bin/stratosphere run \
    --jarfile ./examples/stratosphere-java-examples-{{site.current_snapshot}}-WordCount.jar \
    --arguments 1 file://`pwd`/hamlet.txt file://`pwd`/wordcount-result.txt
</pre>

You will find a file called `wordcount-result.txt` in your current directory.


<h3><a id="run-web"></a>3.2 Run (using the Webinterface)</h3>

Start the webinterface, using the following command:

```
./bin/start-webclient.sh
```

* Access it using the following URL: [http://localhost:8080](http://localhost:8080).
* Upload the WordCount.jar using the upload form in the lower right box. The jar is located in `./examples/stratosphere-java-examples-{{site.current_snapshot}}-WordCount.jar`
* Select the WordCount jar from the list of available jars (upper left).
* Enter the argument line in the lower-left box: 1 file://<path to>/hamlet.txt file://<wherever you want the>/wordcount-result.txt
* Hit "Run Job"


<h3><a id="cluster-setup"></a>4. Cluster Setup</h3>

To setup Stratosphere on a cluster, you need to fulfil the following requirements

* The nodes need to have access to each other, using passwordless SSH.
* The same version of Stratosphere should be placed in the same directories on each node.
We recommend to use a shared folder that is accessible from all nodes.

Write all the node's hostnames into the `conf/slaves` file. One line for each host, do not forget to add a newline in the end.

Choose one node to be the "master" (we call it JobManager). Set the value of `jobmanager.rpc.address` to its hostname.

So the `conf/stratosphere.yaml` should contain something like

```yaml
jobmanager.rpc.address = node01
```

For better performance, we also recommend to set the `taskmanager.tmp.dirs` to one or more directories where Stratosphere can write temporary files. You should use a directory for each harddisk.

You can now start your cluster by invoking

```
./bin/start-cluster.sh
```

This will start the JobManager on that node and the TaskManagers on all `slaves`.

The job submission remains the same.

