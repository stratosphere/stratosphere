---
layout: post
title:  'Use Stratosphere with Amazon Elastic MapReduce'
date:   2014-02-18 19:57:18
categories: blog tutorial
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
			<li>For the Hadoop distribution, it is very important to choose one with YARN support. We use <b>3.0.3 (Hadoop 2.2.0)</b> (the minor version might change over time)</li>
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
	<li>Download and extract Stratosphere-YARN</li>
{% highlight bash %}
wget http://stratosphere-bin.s3-website-us-east-1.amazonaws.com/stratosphere-dist-0.5-SNAPSHOT-yarn.tar.gz
# extract it
tar xvzf stratosphere-dist-0.5-SNAPSHOT-yarn.tar.gz
{% endhighlight %}
	<li>Start Stratosphere in the cluster using Hadoop YARN</li>

{% highlight bash %}
cd stratosphere-yarn-0.5-SNAPSHOT/
./bin/yarn-session.sh -n 4 -jm 1024 -tm 3000
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
<b>YARN Resource Manager</b>: <code>http://&lt;masterIPAddress>:9026/</code> <br>
<b>HDFS NameNode</b>: <code>http://&lt;masterIPAddress>:9101/</code>

You find the `masterIPAddress` by entering `ifconfig` into the terminal:
{% highlight bash %}
[hadoop@ip-172-31-38-95 ~]$ ifconfig
eth0      Link encap:Ethernet  HWaddr 02:CF:8E:CB:28:B2  
          inet addr:172.31.38.95  Bcast:172.31.47.255  Mask:255.255.240.0
          inet6 addr: fe80::cf:8eff:fecb:28b2/64 Scope:Link
          RX bytes:166314967 (158.6 MiB)  TX bytes:89319246 (85.1 MiB)
{% endhighlight %}

**Optional:** If you want to use the hostnames within your Firefox (that also makes the NameNode links work), you have to enable DNS resolution over the SOCKS proxy. Open the Firefox config `about:config` and set `network.proxy.socks_remote_dns` to `true`.

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
# optional: go to the extracted directory
cd stratosphere-yarn-0.5-SNAPSHOT/
# run the wordcount example
./bin/stratosphere run -m  <your JobManagers hostname>:6123 -j examples/stratosphere-java-examples-0.5-SNAPSHOT-WordCount.jar  -a 16 hdfs:///input hdfs:///output
{% endhighlight %}

Lets go through the command in detail:

* `./bin/stratosphere` is the standard launcher for Stratosphere jobs from the command line
* `-m ip-172-31-14-146.us-west-2.compute.internal:6123` is the address of the JobManager. The exact address in for your case is visible in the terminal that contains the YARN output. (`-m` is short for *master*)
* `-j examples/stratosphere-java-examples-0.5-SNAPSHOT-WordCount.jar` the `-j` command sets the jar file containing the job. If you have you own application, place your Jar-file here.
* `-a 16 hdfs:///input hdfs:///output` the `-a` command specifies the Job-specific arguments. In this case, the wordcount expects the following input `<numSubStasks> <input> <output>`.

You can monitor the progress of your job in the JobManager webinterface. Once the job has finished (which should be the case after less than 10 seconds), you can analyze it there.
Inspect the result in HDFS using:

{% highlight bash %}
hadoop fs -tail /output
{% endhighlight %}

If you want to shut down the whole cluster in the cloud, use Amazon's webinterface and click on "Terminate cluster". If you just want to stop the YARN session, press CTRL+C in the terminal. The Stratosphere instances will be killed by YARN.


<br><br>
<small>Written by Robert Metzger ([@rmetzger_](https://twitter.com/rmetzger_)).</small>

