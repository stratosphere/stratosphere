---
layout: post
title:  'Use Stratosphere with Amazon AWS Elastic MapReduce'
date:   2014-02-12 10:57:18
categories: tutorial blog
---

<div class="lead">Get started with Stratosphere within 5 minutes using Amazon Elastic MapReduce.</div>

This step-by-step tutorial will guide you through the setup of Stratosphere using Amazon Elastic MapReduce.

### Background
[Amazon Elastic MapReduce](http://aws.amazon.com/elasticmapreduce/) (Amazon EMR) is part of Amazon Web services. EMR allows to create Hadoop clusters that analyze data stored in Amazon S3 (AWS' cloud storage). Stratosphere runs atop of Hadoop using its [recently](http://hadoop.apache.org/docs/r2.2.0/hadoop-project-dist/hadoop-common/releasenotes.html) released cluster resource manager [YARN](http://hadoop.apache.org/docs/current2/hadoop-yarn/hadoop-yarn-site/YARN.html). YARN allows to use many different data analysis tools in your cluster side by side. Tools that run with YARN are, for example [Apache Giraph](https://giraph.apache.org/), [Spark](http://spark.incubator.apache.org/) or [HBase](http://hortonworks.com/blog/introducing-hoya-hbase-on-yarn/). Stratosphere also [runs on YARN]({{site.baseurl}}/docs/0.4/setup/yarn.html) and thats the approach for this tutorial.

### 1. Step: Login to AWS and prepare secure access

* Log in to the [AWS Console](https://console.aws.amazon.com/console/home)

You have to SSH keys to access the Hadoop master node. If you do not have keys for your computer, generate them:

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
			<li>You can remain the other settings unchanged (termination protection, logging, debugging)</li>
			<li>For the Hadoop distribution, it is very important to choose one with YARN support. We use <b>3.0.2 (Hadoop 2.2.0)</b></li>
			<li>Remove all applications to be installed (unless you want to use them)</li>
			<li>Choose the instance types you want to start. Stratosphere runs fine with m1.large instances. Core and Task instances both run Stratosphere, but only core instances contain HDFS data nodes.</li>
			<li>Choose the <b>EC2 key pair</b> you've created in the previous step!</li>
		</ul>
	</div>
</div>

* Thats it! You can now press the "Create cluster" button at the end of the form to boot it!

### 3. Step: Launch Stratosphere

You might need to wait a few minutes until Amazon started your cluster. (You can monitor the progress of the instances in EC2)

You see that the master is up if the field <b>Master public DNS</b> contains a value (first line), connect to it using SSH.

{% highlight bash %}
ssh hadoop@<your master public DNS> -i <path to your .pem>
# for my example, it looks like this:
ssh hadoop@ec2-54-213-61-105.us-west-2.compute.amazonaws.com -i ~/Downloads/work-laptop.pem
{% endhighlight %}

* Download Stratosphere-YARN
{% highlight bash %}
wget --no-check-certificate https://github.com/stratosphere/stratosphere/releases/download/release-0.4/stratosphere-dist-0.4-hadoop2-yarn-uberjar.jar
{% endhighlight %}

* Start Stratosphere in the cluster
{% highlight bash %}
java -jar stratosphere-dist-0.4-hadoop2-yarn-uberjar.jar -n 4 -jm 3000
{% endhighlight %}

Once the output has changed from 
{% highlight bash %}
JobManager is now running on N/A:6123
{% endhighlight %}
to 
{% highlight bash %}
JobManager is now running on ip-172-31-13-68.us-west-2.compute.internal:6123
{% endhighlight %}
Stratosphere is ready to execute jobs.

### 4. Step: Launch a Stratosphere Job.

Open an additional terminal and connect again to the master

ssh -D localhost:2001 hadoop@ec2-54-213-63-191.us-west-2.compute.amazonaws.com -i ~/Downloads/work-laptop.pem

Configure a browser (in my case firefox) ...


  ResourceManager    lynx http://localhost:9026/
  NameNode           lynx http://localhost:9101/

  Also access Stratosphere JobManager

  generate data using

java -cp stratosphere-dist-0.5-SNAPSHOT-yarn-uberjar.jar eu.stratosphere.client.CliFrontend run -m ip-172-31-13-68.us-west-2.compute.internal:6123  -j stratosphere-java-examples-0.5-SNAPSHOT.jar -c eu.stratosphere.example.java.record.util.WordCountDataGenerator -a hdfs:///input 50000000 208
(1.8 TB of data)


kill using yarn command:
yarn application -kill application_1392133159960_0002


Windows users have to follow [these instructions](http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/emr-connect-master-node-ssh.html) to SSH into the machine running the master.