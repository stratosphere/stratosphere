---
layout: inner_docs
title:  "Local Setup"
sublinks:
  - {anchor: "download", title: "Download"}
  - {anchor: "requirements", title: "Requirements"}
  - {anchor: "config", title: "Configuration"}
  - {anchor: "start", title: "Starting Stratosphere"}
---

## Local Setup

<p class="lead">This documentation is intended to provide instructions on how to run Stratosphere locally on a single machine.</p>

<section id="download">
### Download

Go to the [downloads page]({{site.baseurl}}/downloads/) and get the ready to run package. If you want to interact with Hadoop (e.g. HDFS or HBase), make sure to pick the Stratosphere package **matching your Hadoop version**. When in doubt or you plan to just work with the local file system pick the package for Hadoop 1.2.x.
</section>

<section id="requirements">
### Requirements

Stratosphere runs on all *UNIX-like environments*, e.g. **Linux**, **Mac OS X**, and **Cygwin** (for Windows). The only requirement for a local setup is **Java 1.6.x** or higher.

You can check the correct installation of Java by issuing the following command:

    java -version

The command should output something comparable to the following:

    java version "1.6.0_22"
    Java(TM) SE Runtime Environment (build 1.6.0_22-b04)
    Java HotSpot(TM) 64-Bit Server VM (build 17.1-b03, mixed mode)
</section>

<section id="config">
### Configuration

**For local mode Stratosphere is ready to go out of the box and you don't need to change the default configuration.**

The out of the box configuration will use your default Java installation. You can manually set the environment variable `JAVA_HOME` or the configuration key `env.java.home` in `conf/stratosphere-conf.yaml` if you want to manually override the Java runtime to use. Consult the [configuration page]({{site.baseurl}}/setup/config.html) for further details about configuring Stratosphere.
</section>

<section id="start">
### Starting Stratosphere

**You are now ready to start Stratosphere.** Unpack the downloaded archive and change to the newly created `stratosphere` directory. There you can start Stratosphere in local mode:

{% highlight bash %}
$ tar xzf stratosphere-*.tgz
$ cd stratosphere
$ bin/start-local.sh
Starting Nephele job manager
{% endhighlight %}

You can check that the system is running by checking the log files in the `logs` directory:

{% highlight bash %}
$ tail log/nephele-*-jobmanager-*.log
INFO ... - Initializing memory manager with 409 megabytes of memory
INFO ... - Trying to load eu.stratosphere.nephele.jobmanager.scheduler.local.LocalScheduler as scheduler
INFO ... - Setting up web info server, using web-root directory ...
INFO ... - Web info server will display information about nephele job-manager on localhost, port 8081.
INFO ... - Starting web info server for JobManager on port 8081
{% endhighlight %}

The JobManager will also start a web frontend on port 8081, which you can check with your browser at `http://localhost:8081`.
</section>
