---
layout: documentation
---
Setting up Stratosphere in Cloud Mode
=====================================

<table>
<thead>
<tr class="header">
<th align="left">Attention:</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left">This is an experimental feature which is not yet officially supported by the Stratosphere team.</td>
</tr>
</tbody>
</table>

This documentation is intended to provide instructions on how to run the
Stratosphere system in the so-called *cloud mode*. In contrast to the
[cluster
mode](clustersetup.html "clustersetup"),
the Stratosphere system does not work with a fixed number of worker
nodes in cloud node. Instead, the system tries to allocate the worker
nodes which are necessary to run a job *on demand* from an
Infrastructure as a Service cloud. The general idea behind this mode is
described in the [Nephele research
paper](http://stratosphere.eu/files/Nephele_09.pdf "http://stratosphere.eu/files/Nephele_09.pdf").

<table>
<thead>
<tr class="header">
<th align="left">Attention:</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left">You must be aware that in this cloud mode the Stratosphere system will attempt to autonomically allocate virtual machines on your behalf. Depending on the cloud system, the cloud operate may charge you for the usage of the virtual machines. Please note that we do not take responsibility for any financial damage you might incur as a result of using this feature. There is no warranty for this program. Use it a your own risk!</td>
</tr>
</tbody>
</table>

Register with an Infrastructure as a Service Cloud
--------------------------------------------------

Since in cloud mode, Nephele, the runtime engine of the Stratosphere
system, dynamically allocated the required worker nodes from an
Infrastructure as a Service cloud, you first must register with such a
cloud system. Currently, Nephele implements the [Amazon EC2
API](http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ "http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/"),
so you can either use [Amazon
EC2](http://aws.amazon.com/de/ec2/ "http://aws.amazon.com/de/ec2/") or
the
[Eucalyptus](http://open.eucalyptus.com/ "http://open.eucalyptus.com/")
cloud to run the system in this mode.

Once you have registered with one of the cloud systems above, you will
obtain a set of credentials. These credentials must be later attached to
the job to be submitted.

Prepare the Virtual Machine Image
---------------------------------

In order to enable Nephele to bring up worker nodes on demand, you must
prepare a virtual machine image and upload it to our cloud provider. The
virtual machine image must be configured to automatically start a
TaskManager. *We are currently working on prepackaging such a VM image
to simplify the setup process.*

Once you have uploaded the image to your cloud provider, you will
receive an identifier for that image, for example *emi-AE291B0F*. Please
write down this identifier because it is necessary for the configuration
later on.

Setting up the Job Manager Node
-------------------------------

The JobManager is central management node of the Nephele system. It
coordinates the scheduling of the individual job tasks and controls the
allocation of the worker nodes. You are free to choose whether the job
manager is supposed to run on a node inside or outside of the cloud
which will accommodate the worker nodes. However, make sure that the
worker nodes can contact the JobManager node to register themselves.

### Obtaining the Stratosphere System

To install the JobManager on a compute node, you must first download the
Stratosphere system. You can download the latest release from the
[Download
Page](http://stratosphere.eu/downloads "http://stratosphere.eu/downloads").
Once you have downloaded the archive, you must extract it:

    $ tar xvfz stratosphere-dist-0.2-bin.tar.gz
    $ cd stratosphere-0.2

### Configuring the System for Cloud Mode

In order to configure the system for cloud mode, enter the configuration
file *conf/nephele-user.xml*. Within the file, you must set the
following key/value pairs:

<table>
<thead>
<tr class="header">
<th align="left">Key</th>
<th align="left">Value</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>jobmanager.instancemanager.cloud.classname</em></td>
<td align="left"><em>eu.stratosphere.nephele.instance.cloud.EC2CloudManager</em></td>
</tr>
<tr class="even">
<td align="left"><em>jobmanager.scheduler.cloud.classname</em></td>
<td align="left"><em>eu.stratosphere.nephele.jobmanager.scheduler.queue.QueueScheduler</em></td>
</tr>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.endpoint</em></td>
<td align="left">The host name of EC2 Web Services, e.g., <em>eu-west-1.ec2.amazonaws.com</em>.</td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.ec2.leaseperiod</em></td>
<td align="left">The period of time in milliseconds after which idle instances will be released</td>
</tr>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.availabilityzone</em></td>
<td align="left">The preferred EC2 availability zone used to allocate instances</td>
</tr>
<tr class="even">
<td align="left"><em>instancemanager.ec2.type.i</em></td>
<td align="left">The virtual machine type for virtual machine number <em>i</em></td>
</tr>
<tr class="odd">
<td align="left"><em>instancemanager.ec2.defaulttype</em></td>
<td align="left">The index of the default virtual machine type</td>
</tr>
<tr class="even">
<td align="left"><em>job.ec2.awsaccessid</em></td>
<td align="left">The AWS access key of the user that must be attached to the job</td>
</tr>
<tr class="odd">
<td align="left"><em>job.ec2.awssecretkey</em></td>
<td align="left">The AWS secret key of the user that must be attached to the job</td>
</tr>
<tr class="even">
<td align="left"><em>job.ec2.ami</em></td>
<td align="left">The ID of the AMI to be started for the worker nodes</td>
</tr>
</tbody>
</table>

The last three configuration settings are required to describe the
different types of virtual machines available at the cloud and their
respective cost per hour. Each value for *cloudmgr.instancetype.i* must
be composed of the following information:

<table>
<thead>
<tr class="header">
<th align="left">Value</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><em>Identifier</em></td>
<td align="left">A unique name for this type of compute node, e.g. <em>small</em>, <em>large</em>, or <em>highcpu</em>.</td>
</tr>
<tr class="even">
<td align="left"><em>Number of Compute Units</em></td>
<td align="left">The number of compute units refer to the compute power of the type of node. Usually, the number of compute units should be set to the number of CPU cores the respective node type has (or a multiple thereof) unless the clock frequencies of the CPUs differ significantly.</td>
</tr>
<tr class="odd">
<td align="left"><em>Number of CPU Cores</em></td>
<td align="left">The number of CPU cores the type of worker node has.</td>
</tr>
<tr class="even">
<td align="left"><em>Amount of RAM</em></td>
<td align="left">The amount of RAM of this type of worker node, specified in MB.</td>
</tr>
<tr class="odd">
<td align="left"><em>Disk Capacity</em></td>
<td align="left">The disk capacity of this type of worker node, specified in GB.</td>
</tr>
<tr class="even">
<td align="left"><em>Price per Hour</em></td>
<td align="left">A price per hour in cents that may be charged for using this type of worker node. If there is no charge for the usage of the machines, it is safe to set to value to 0.</td>
</tr>
</tbody>
</table>

For Amazon EC2, you can use the following configuration excerpt as a
template:

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>

            .
            .
            <property>
                    <key>instancemanager.ec2.type.1</key>
                    <value>m1.small,1,1,1740,160,9</value>
            </property>

            <property>
                    <key>instancemanager.ec2.type.2</key>
                    <value>m1.large,4,2,7680,850,34</value>
            </property>

            <property>
                    <key>instancemanager.ec2.type.3</key>
                    <value>m1.xlarge,8,4,15360,1690,68</value>
            </property>

            <property>
                    <key>instancemanager.ec2.type.4</key>
                    <value>t1.micro,2,1,613,0,2</value>
            </property>

            <property>
                    <key>instancemanager.ec2.type.5</key>
                    <value>m2.xlarge,6,2,17510,420,50</value>
            </property>

            <property>
                    <key>instancemanager.ec2.type.6</key>
                    <value>m2.2xlarge,13,4,35020,850,100</value>
            </property>

            <property>
                    <key>instancemanager.ec2.type.6</key>
                    <value>m2.4xlarge,26,8,70041,1690,200</value>
            </property>

            .
            .
    </configuration>

### Launching the Stratosphere System in Cloud Mode

To launch the Stratosphere system in cloud mode, log on to the
JobManager node and type in the following command:

    $ ./bin/start-cloud.sh
