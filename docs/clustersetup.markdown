---
layout: documentation
---
Setting up Stratosphere on a Cluster
====================================

This documentation is intended to provide instructions on how to run the
Stratosphere system in a fully distributed fashion on a static (but
possibly heterogeneous) cluster. Setting up Stratosphere on a cluster
involves basically two steps. First, installing and configuring the
Nephele execution engine and second installing and configuring the
[Hadoop Distributed
Filesystem](http://hadoop.apache.org/hdfs/ "http://hadoop.apache.org/hdfs/")
(HDFS).

Preparing the Cluster
---------------------

### Software Requirements

Nephele, the execution engine of the Stratosphere system, expects the
cluster to consists of one master node and one or more worker nodes.
Before you start to setup the actual Stratosphere software, make sure
you have the following software installed **on each node**:

-   Java 1.6.x, preferably from
    [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html "http://www.oracle.com/technetwork/java/javase/downloads/index.html")
-   **ssh** (sshd must be running to use the Nephele scripts that manage
    remote components)

If your cluster does not fulfill these software requirements you will
need to install/upgrade it.

For example, on Ubuntu Linux, type in the following commands to install
Java and ssh:

    $ sudo apt-get install ssh 
    $ sudo apt-get install sun-java6-jre

You can check the correct installation of Java by issuing the following
command:

    $ java -version

The command should output something comparable to the following on every
node of your cluster (depending on your Java version, there may be small
differences):

    java version "1.6.0_22"
    Java(TM) SE Runtime Environment (build 1.6.0_22-b04)
    Java HotSpot(TM) 64-Bit Server VM (build 17.1-b03, mixed mode)

To make sure the ssh daemon is running properly, you can use the command

    $ ps aux | grep sshd

Something comparable to the following line should appear in the output
of the command on every host of your cluster:

    root       894  0.0  0.0  49260   320 ?        Ss   Jan09   0:13 /usr/sbin/sshd

### Configuring Remote Access with ssh

In order to start/stop the remote processes, the master node requires
access via ssh to the worker nodes. It is most convenient to use ssh's
public key authentication for this. To setup public key authentication,
log on to the master as the user who will later execute all the Nephele
components. **The same user (i.e. a user with the same user name) must
also exist on all worker nodes**. For the remainder of this instruction
we will refer to this user as *nephele*. Using the super user *root* is
highly discouraged for security reasons.

Once you logged in to the master node as the desired user, you must
generate a new public/private key pair. The following command will
create a new public/private key pair into the *.ssh* directory inside
the home directory of the user *nephele*. See the ssh-keygen man page
for more details. Note that the private key is not protected by a
passphrase. If you have security concerns about this, you might want to
consider tools like
[Keychain](http://www.funtoo.org/en/security/keychain/intro/ "http://www.funtoo.org/en/security/keychain/intro/").

    $ ssh-keygen -b 2048 -P '' -f ~/.ssh/id_rsa

Next, copy/append the content of the file *.ssh/id\_rsa.pub* to your
authorized\_keys file. The content of the authorized\_keys file defines
which public keys are considered trustworthy during the public key
authentication process. On most systems the appropriate command is

    $ cat .ssh/id_rsa.pub >> .ssh/authorized_keys

On some Linux systems, the authorized keys file may also be expected by
the ssh daemon under *.ssh/authorized\_keys2*. In either case, you
should make sure the file only contains those public keys which you
consider trustworthy for each node of cluster.

Finally, the authorized keys file must be copied to every worker node of
your cluster. You can do this by repeatedly typing in

    $ scp .ssh/authorized_keys <worker>:~/.ssh/

and replacing *\<worker\>* with the host name of the respective worker
node. After having finished the copy process, you should be able to log
on to each worker node from your master node via ssh without a password.

**Note:** The [script to execute a command on each
node](helpfulscripts#commandoneachnode "helpfulscripts")
may help you to finish the next steps easier.

### Setting JAVA\_HOME on each Node

Nephele requires the *JAVA\_HOME* environment variable to be set on the
master and all worker nodes. To check if the variable is already set on
your system, type in the following command

    $ echo $JAVA_HOME

The output of the command should be non-empty and pointing to the
directory of your Java installation, for example

    /usr/lib/jvm/java-6-sun/

Setting the *JAVA\_HOME* can be achieved in various ways. One way is to
add the following line to your shell profile. If you use the *bash*
shell (probably the most common shell), the shell profile is located in
*\~/.bashrc*:

    export JAVA_HOME=/usr/lib/jvm/java-6-sun/

Alternatively, if your ssh daemon supports user environments, you can
also add *JAVA\_HOME* to *.\~/.ssh/environment*. As super user *root*
you can enable ssh user environments with the following commands:

    $ echo "PermitUserEnvironment yes" >> /etc/ssh/sshd_config
    $ /etc/init.d/ssh restart

Setting up the Hadoop Distributed Filesystem (HDFS)
---------------------------------------------------

The Stratosphere system currently uses the Hadoop Distributed Filesystem
(HDFS) to read and write data in a distributed fashion. The HDFS is an
open implementation of the Google filesystem (see
[http://labs.google.com/papers/gfs.html](http://labs.google.com/papers/gfs.html "http://labs.google.com/papers/gfs.html"))
and part of Apache's Hadoop project (see
[http://hadoop.apache.org](http://hadoop.apache.org "http://hadoop.apache.org")).
Besides HDFS, the second major component of the Hadoop project is a
MapReduce execution engine. Nephele/PACTs is an alternative to Hadoop's
MapReduce engine but uses the same data storage, namely the HDFS.
'''Currently, only versions 0.20.\* of the HDFS are supported by the
Stratosphere system.

### Downloading, Installing, and Configuring HDFS

Similar to the Stratosphere system HDFS runs in a distributed fashion.
From an architectural point of view HDFS consists of one so called
**NameNode** which manages the distributed file system's meta data. The
actual data is stored by one or more so called **DataNodes**. For the
remainder of this instruction we assume the HDFS's NameNode component
runs on the master node while all the worker nodes run an HDFS DataNode.

To start, log on to your master node and download Hadoop (which includes
the HDFS) from [Hadoop Common
Releases](http://hadoop.apache.org/common/releases.html "http://hadoop.apache.org/common/releases.html").
Next, extract the Hadoop archive.

    $ wget http://ftp.halifax.rwth-aachen.de/apache//hadoop/core/hadoop-0.20.203.0/hadoop-0.20.203.0rc1.tar.gz
    $ tar xvfz hadoop-0.20.203.0rc1.tar.gz

After having extracted the Hadoop archive, change into the Hadoop
directory and edit the Hadoop environment configuration file:

    $ cd hadoop-0.20.203.0
    $ nano conf/hadoop-env.sh

Uncomment and modify the following line in the file according to the
location of your Java installation.

    export JAVA_HOME=/usr/lib/jvm/java-6-sun/

Save the changes and next open the HDFS configuration file
*conf/hdfs-site.xml*. HDFS offers multiple configuration parameters
which affect the behavior of the distributed file system in various
ways. The following excerpt shows a minimal configuration which is
required to make HDFS work. More information on how to configure HDFS
can be found in the [HDFS User
Guide](http://hadoop.apache.org/common/docs/r0.20.203.0/hdfs_user_guide.html "http://hadoop.apache.org/common/docs/r0.20.203.0/hdfs_user_guide.html")
guide.

    <?xml version="1.0"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

    <!-- Put site-specific property overrides in this file. -->

    <configuration>
      <property>
        <name>fs.default.name</name>
        <value>hdfs://<master>:50040/</value>
      </property>
      <property>
        <name>dfs.data.dir</name>
        <value><datapath></value>
      </property>
    </configuration>

Replace *\<master\>* with the IP/host name of your master node which
runs the NameNode. *\<datapath\>* must be replaced with path to the
directory in which the actual HDFS data shall be stored on each worker
node. Make sure that the *nephele* user has sufficient permissions to
read and write in that directory.

After having saved the HDFS configuration file, open the file
*conf/slaves* and enter the IP/host name of those worker nodes which
shall act as DataNodes. Each entry must be separated by a line break.

    <worker 1>
    <worker 2>
    .
    .
    .
    <worker n>

Initialize the HDFS by typing in the following command. Note that the
command will **delete all data** which has been previously stored in the
HDFS. However, since we have just installed a fresh HDFS, it should be
safe to answer the confirmation with *yes*.

    $ bin/hadoop namenode -format

Finally, we need to copy the Hadoop directory to all worker nodes which
are intended to act as DataNodes. It is important that the Hadoop
directory is **accessible on each machine at the same location**. We
recommend to use either a shared network directory (e.g. an NFS share)
or symlinks to ensure that. In the following we we assume the Hadoop
directory is stored directly in the home directory of the *nephele*
user. In this case enter

    $ cd ..
    $ scp -r hadoop-0.20.203.0 <worker>:~/

Repeat the last command for every worker node and replace *\<worker\>*
with the respective IP/host name.

### Starting HDFS

To start the HDFS log on to the master and type in the following
commands

    $ cd hadoop-0.20.203.0
    $ bin/start-dfs.sh

If your HDFS setup is correct, you should be able to open the HDFS
status website at *http://\<master\>:50070*. In a matter of a seconds,
all DataNodes should appear as live nodes. For troubleshooting we would
like to point you to the [Hadoop Quick
Start](http://hadoop.apache.org/common/docs/r0.20.0/quickstart.html "http://hadoop.apache.org/common/docs/r0.20.0/quickstart.html")
guide.

Setting up the Stratosphere System
----------------------------------

To obtain a version of the Stratosphere system you can either download a
release from the [Download
Page](http://stratosphere.eu/downloads "http://stratosphere.eu/downloads")
or compile the system from source code. This document focuses on the
first approach. If you want to compile the system on your own, please
check the instructions on [building the Stratosphere
system](buildthesystem.html "buildthesystem").

### Downloading and Installing the Stratosphere System

Download the latest release from the [Download
Page](http://stratosphere.eu/downloads "http://stratosphere.eu/downloads"),
copy it to the home directory of the *nephele* user on the master node
and extract it:

    $ tar xvfz stratosphere-dist-0.2-bin.tar.gz
    $ cd stratosphere-0.2

### Configuring the Cluster

After having extracted the system files, you need to configure the
cluster. Nephele offers support for heterogeneous worker nodes - in
terms of hardware resources. Nephele can exploit information about
different types of worker nodes during the scheduling process.

The configuration of different worker node-types involves three steps:

First, you have to describe the different types of worker nodes which
are available in your cluster. To do so, edit the file
*conf/nephele-user.xml*. For each type of worker node you must add a
configuration entry which describes its hardware characteristics. Make
sure the key for each configuration entry begins with
*instancemanager.cluster.type.* followed by a serial number.

The value of each configuration must be a comma-separated string
composed of the following values:

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

The following excerpt shows a sample configuration for two types of
worker nodes:

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>

            .
            .
            <property>
                    <key>instancemanager.cluster.type.1</key>
                    <value>standard,2,1,1024,20,0</value>
            </property>

            <property>
                    <key>instancemanager.cluster.type.2</key>
                    <value>medium,4,2,2048,40,0</value>
            </property>

            <property>
                    <key>instancemanager.cluster.defaulttype</key>
                    <value>1</value>
            </property>
            .
            .
    </configuration>

At runtime, Nephele tries to match each involved worker node to an
instance type according to the available hardware resources. The
configuration entry *instancemanager.cluster.defaulttype* points to the
type of worker node which shall be used for nodes which cannot be
assigned to a type automatically.

Second, after having defined the available types of compute nodes, you
must define the maximum amount of main memory the JVM is allowed to
allocate for the Stratosphere system on each worker node.

If the amount of memory is equal on each worker node you can edit the
file *bin/nephele-config.sh* and adjust the default value:

    DEFAULT_NEPHELE_TM_HEAP=512

The value is given in MB. If some worker nodes have more main memory
which you want to allocate to the Stratosphere system you can overwrite
the default value by setting an environment variable *NEPHELE\_TM\_HEAP*
on the respective node.

Third and finally, you must provide a list of all nodes in your cluster
which shall be used as worker nodes. Therefore, similar to the HDFS
configuration, edit the file *conf/slaves* and enter the IP/host name of
each worker node. Each worker node will later run a Nephele TaskManager.
Each entry must be separated by a new line, as in the following example:

    192.168.0.100
    192.168.0.101
    .
    .
    .
    192.168.0.150

Nephele will attempt to automatically match each worker node to one of
the previously configured worker node types. However, you can also
manually set the worker node type by adding the respective type
identifier behind the IP/host name.

    192.168.0.100
    192.168.0.101    medium
    .
    .
    .
    192.168.0.150

After having completed the list of worker nodes, copy the entire
Stratosphere directory to every worker node. Similar to HDFS, **we
assume the files are accessible on each machine at the same location**.
Besides copying the directory to every worker node, you can also copy
the Stratosphere directory to an NFS directory (for example HDFS) to
easily make the Stratosphere files accessible from worker nodes.

### Configuring the Network Buffers

Network buffers are a critical resource for the communication layers.
They are used to buffer records before transmission over a network, and
to buffer incoming data before dissecting it into records and handing
them to the application. A sufficient number of network buffers are
critical to achieve a good throughput. Network buffers are allocated as
*direct buffers* from Java, i.e. their memory exists out of the Java
Heap. Direct buffers are used to speed up the network transmission and
to support native compression libraries.

By default, Nephele is started with 256 read- and 256 write buffers.
That number is sufficient for a small cluster of machines, for example 4
QuadCore machines, where the total degree of parallelism will be 36. To
perform well on larger clusters, the number of read and write buffers
must be increased. In general, configure the task manager to have so
many buffers that each logical network connection on you expect to be
open at the same time has a dedicated buffer. A logical network
connection exists for each point-to-point exchange of data over the
network, which typically happens at repartitioning- or broadcasting
steps. In those, each parallel task inside the TaskManager has to be
able to talk to all other parallel tasks. Hence, the required number of
buffers on a task manager is *total-degree-of-parallelism* (number of
targets) \* *intra-node-parallelism* (number of sources in one task
manager) \* *n*. Here, *n* is a constant that defines how many
repartitioning-/broadcasting steps you expect to be active at the same
time.

Since the *intra-node-parallelism* is typically the number of cores, and
more than 4 repartitioning or broadcasting channels are rarely active in
parallel, it frequently boils down to *\#cores\^2\^* \* *\#machines* \*
4. To support for example a cluster of 20 8-core machines, you should
use roughly 5000 read and write buffers for optimal throughput.

Each network buffer is by default 64 KiBytes large. Recall that network
buffers are allocated as direct buffers. In the above example, the
system would allocate roughly 300 MiBytes for read and another 300
MiBytes for write buffers. The amount of direct memory that can be
allocated is relative to the size of the Java Heap. If the TaskManager
crashes at start-up with *OutOfMemoryError: Direct buffer memory*, the
TaskManager had to little direct memory available to allocate the
specified amount of buffers. You either need to increase the Heap size
of the JVM, or decrease the number of buffers or the buffer size in that
case.

The number and size of network buffers can be configured with the
following parameters:

-   *channel.network.numberOfBuffers*, and
-   *channel.network.bufferSizeInBytes*.

Please see the [Configuration
Reference](configreference.html "configreference")
for details.

### Launching the Stratosphere System

    $ ./bin/start-cluster.sh

This shell script starts a JobManager on the local node and connects via
SSH to all worker nodes listed in the *slaves* file to start the
TaskManager on each node. Now your Stratosphere System is up and
running. The JobManager running on the local node will now accept jobs
at the configured RPC port.
