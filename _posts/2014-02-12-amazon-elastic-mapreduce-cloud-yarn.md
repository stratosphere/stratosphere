---
layout: post
title:  'Use Stratosphere with Amazon Elastic MapReduce'
date:   2014-02-12 10:57:18
categories: tutorial blog
---

<div class="lead">Get started with Stratosphere within 10 minutes using Amazon Elastic MapReduce.</div>

This step-by-step tutorial will guide you through the setup of Stratosphere using Amazon Elastic MapReduce.

### Background
[Amazon Elastic MapReduce](http://aws.amazon.com/elasticmapreduce/) (Amazon EMR) is part of Amazon Web services. EMR allows to create Hadoop clusters that analyze data stored in Amazon S3 (AWS' cloud storage). Stratosphere runs atop of Hadoop using the [recently](http://hadoop.apache.org/docs/r2.2.0/hadoop-project-dist/hadoop-common/releasenotes.html) released cluster resource manager [YARN](http://hadoop.apache.org/docs/current2/hadoop-yarn/hadoop-yarn-site/YARN.html). YARN allows to use many different data analysis tools in your cluster side by side. Tools that run with YARN are, for example [Apache Giraph](https://giraph.apache.org/), [Spark](http://spark.incubator.apache.org/) or [HBase](http://hortonworks.com/blog/introducing-hoya-hbase-on-yarn/). Stratosphere also [runs on YARN]({{site.baseurl}}/docs/0.4/setup/yarn.html) and that's the approach for this tutorial.

### 1. Step: Login to AWS and prepare secure access

* Log in to the [AWS Console](https://console.aws.amazon.com/console/home)

You need to have SSH keys to access the Hadoop master node. If you do not have keys for your computer, generate them:

<div class="row" style="padding-top:15px">
	<div class="col-md-6">
<a data-lightbox="inputs" href="{{site.baseurl}}/img/blog/emr-security.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/blog/emr-security.png" /></a>
	</div>
	<div class="col-md-6">
		<ul>
			<li>Select <a href="https://console.aws.amazon.com/ec2/v2/home">EC2</a> and click on "Key Pairs" in the "NETWORK & SECURITY" section.</li>
			<li>Click on "Create Key Pair" and give it a name</li>
			<li>After pressing "Yes" it will download a .pem file.</li>
			<li>Change the permissions of the .pem file</li>
{% highlight bash %}
chmod og-rwx ~/work-laptop.pem 
{% endhighlight %}
		</ul>
	</div>
</div>

### 2. Step: Create your Hadoop Cluster in the cloud

* Select [Elastic MapReduce](https://console.aws.amazon.com/elasticmapreduce/vnext/) from the AWS console
* Click the blue "Create cluster" button.

<div class="row" style="padding-top:15px">
	<div class="col-md-6">
<a data-lightbox="inputs" href="{{site.baseurl}}/img/blog/emr-hadoopversion.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/blog/emr-hadoopversion.png" /></a>
	</div>
	<div class="col-md-6">
		<ul>
			<li>Choose a Cluster name</li>
			<li>You can let the other settings remain unchanged (termination protection, logging, debugging)</li>
			<li>For the Hadoop distribution, it is very important to choose one with YARN support. We use <b>3.0.2 (Hadoop 2.2.0)</b></li>
			<li>Remove all applications to be installed (unless you want to use them)</li>
			<li>Choose the instance types you want to start. Stratosphere runs fine with m1.large instances. Core and Task instances both run Stratosphere, but only core instances contain HDFS data nodes.</li>
			<li>Choose the <b>EC2 key pair</b> you've created in the previous step!</li>
		</ul>
	</div>
</div>

* Thats it! You can now press the "Create cluster" button at the end of the form to boot it!

### 3. Step: Launch Stratosphere

You might need to wait a few minutes until Amazon started your cluster. (You can monitor the progress of the instances in EC2). Use the refresh button in the top right corner.

You see that the master is up if the field <b>Master public DNS</b> contains a value (first line), connect to it using SSH.

{% highlight bash %}
ssh hadoop@<your master public DNS> -i <path to your .pem>
# for my example, it looks like this:
ssh hadoop@ec2-54-213-61-105.us-west-2.compute.amazonaws.com -i ~/Downloads/work-laptop.pem
{% endhighlight %}


Windows users have to follow <a href="ttp://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/emr-connect-master-node-ssh.html">these instructions</a> to SSH into the machine running the master. </br>

<ul>
	<li>Download Stratosphere-YARN</li>
{% highlight bash %}
wget http://stratosphere-bin.s3-website-us-east-1.amazonaws.com/stratosphere-dist/target/stratosphere-dist-0.5-hadoop2-SNAPSHOT-yarn-uberjar.jar
{% endhighlight %}
	<li>Start Stratosphere in the cluster using Hadoop YARN</li>

{% highlight bash %}
java -jar stratosphere-dist-0.4-hadoop2-yarn-uberjar.jar -n 4 -jm 3000 -tm 3000
{% endhighlight %}

The arguments have the following meaning
	<ul>
			<li><code>-n</code> number of TaskManagers (=workers). This number must not exeed the number of task instances</li>
			<li><code>-jm</code> memory (heapspace) for the JobManager</li>
			<li><code>-tm</code> memory for the TaskManagers</li>
	</ul>
</ul>

Once the output has changed from 
{% highlight bash %}
JobManager is now running on N/A:6123
{% endhighlight %}
to 
{% highlight bash %}
JobManager is now running on ip-172-31-13-68.us-west-2.compute.internal:6123
{% endhighlight %}
Stratosphere is ready to execute jobs.


You'll need the JobManager <code>host:post</code> information later if you want to submit jobs to the Stratosphere cluster.



<h3> 4. Step: Launch a Stratosphere Job</h3>

This step shows how to submit and monitor a Stratosphere Job in the Amazon Cloud.

<ul>
<li> Open an additional terminal and connect again to the master of your cluster. </li>

We recommend to create a SOCKS-proxy with your SSH that allows you to easily connect into the cluster. (If you've already a VPN setup with EC2, you can probably use that as well.)

{% highlight bash %}
ssh -D localhost:2001 hadoop@<your master dns name> -i <your pem file>
{% endhighlight %}

Notice the <code>-D localhost:2001</code> argument: It opens a SOCKS proxy on your computer allowing any application to use it to communicate through the master node.

<li>Configure a browser to use the SOCKS proxy. I therefore used a browser that I do not use regularly (Firefox).</li>

<div class="row" style="padding-top:15px">
	<div class="col-md-6">
<a data-lightbox="inputs" href="{{site.baseurl}}/img/blog/emr-firefoxsettings.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/blog/emr-firefoxsettings.png" /></a>
	</div>
	<div class="col-md-6">
		<ul>
			<li>To configure the SOCKS proxy with Firefox, click on "Edit", "Preferences", choose the "Advanced" tab and press the "Settings ..." button.</li>
			<li>Enter the details of the SOCKS proxy <b>localhost:2001</b>. Choose SOCKS v4.</li>
			<li>Close the settings, your browser is now talking to the master node of your cluster</li>
		</ul>
	</div>
</div>

</ul>

Since you're connected to the master now, you can open several web interfaces: <br>
<b>YARN Resource Manager</b>: <code>http://&lt;yourMasterDNSName>:9026/</code> <br>
<b>HDFS NameNode</b>: <code>http://&lt;yourMasterDNSName>:9101/</code>

The YARN ResourceManager also allows you to connect to <b>Stratosphere's JobManager web interface</b>. Click the <b>ApplicationMaster</b> link in the "Tracking UI" column.

To run the Wordcount example, you have to upload some sample data.
{% highlight bash %}
# download a text
wget http://www.gnu.org/licenses/gpl.txt
# upload it to HDFS:
hadoop fs -copyFromLocal gpl.txt /input
{% endhighlight %}

To run a Job, enter the following command into the master's command line:
{% highlight bash %}
java -cp stratosphere-dist-0.5-SNAPSHOT-yarn-uberjar.jar \
	eu.stratosphere.client.CliFrontend run \
	-m <your JobManagers hostname>:6123 \
	-j stratosphere-dist-0.5-SNAPSHOT-yarn-uberjar.jar -c eu.stratosphere.example.java.record.wordcount.Wordcount \
	-a 16 hdfs:///input hdfs:///output
{% endhighlight %}

Lets go through the command in detail:

* `-cp stratosphere-dist-0.5-SNAPSHOT-yarn-uberjar.jar` sets puts the YARN-jar into the classpath of Java
* `eu.stratosphere.client.CliFrontend run` is the main class (the same that is actually called when you use `/bin/stratosphere`) with the `run` command
* `-m ip-172-31-14-146.us-west-2.compute.internal:6123` is the address of the JobManager. The exact address in for your case is visible in the terminal that contains the YARN output. (`-m` is short for *master*)
* `-j stratosphere-dist-0.5-SNAPSHOT-yarn-uberjar.jar` the `-j` command sets the jar file containing the job. Since the example is contained in the main jar, you can just re-use it here to submit it to the cluster. If you have you own application, place your Jar-file here.
* `-c eu.stratosphere.example.java.record.wordcount.Wordcount` is the name of the main class you want to run and that contains your job.
* `-a 16 hdfs:///input hdfs:///output` the `-a` command specifies the Job-specific arguments. In this case, the wordcount expects the following input `<numSubStasks> <input> <output>`.

Inspect the result in HDFS using:

{% highlight bash %}
hadoop fs -tail /output
{% endhighlight %}

If you want to shut down the whole cluster in the cloud, us Amazon's webinterface and click on "Terminate cluster".


<br><br>
<small>Written by Robert Metzger ([@rmetzger_](https://twitter.com/rmetzger_)).</small>

