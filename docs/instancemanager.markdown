---
layout: documentation
---
InstanceManager
---------------

In Nephele an instance manager maintains the set of available compute
resources. It is responsible for allocating new compute resources and
provisioning available compute resources to the
[JobManager](jobmanager.html "jobmanager").
Additionally it is keeping track of the availability of the utilized
compute resources in order to report unexpected resource outages. For
this purpose the instance manager receives heartbeats of each task
manager. If a task manager has not sent a heartbeat in the given
heartbeat interval, the host is assumed to be dead. The instance manager
then removes the respective task manager from the set of compute
resources and calls the scheduler to take appropriate actions.

### Interface

The instance manager interface is defined as part of the
`nephele-server` project in the package
*[eu.stratosphere.nephele.instance](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/instance/ "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/instance/")*.

### Default Instance Manager

Nephele provides a default instance manager for each execution mode
(Local-, Cluster-, Cloud-Mode).

##### Local instance Manager

The LocalInstanceManager is in the `nephele-server` project in the
*[eu.stratosphere.nephele.instance.local](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/instance/local/ "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/instance/local/")*
package. The local instance manager is designed to manage instance
allocation/deallocation for a single-node setup. It spans a task manager
which is executed within the same process as the job manager. Moreover,
it determines the hardware characteristics of the machine it runs on and
generates a default instance type.

##### Cluster instance Manager

The ClusterManager is declared in the `nephele-clustermanager` project
in the
*[eu.stratosphere.nephele.instance.cluster](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-clustermanager/src/main/java/eu/stratosphere/nephele/instance/cluster/ "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-clustermanager/src/main/java/eu/stratosphere/nephele/instance/cluster/")*
package. The ClusterManager is an instance manager for a static cluster.
The ClusterManager can handle heterogeneous instances (compute nodes).
Each instance type used in the cluster must be described in the
[configuration](configreference.html "configreference").
Each instance is expected to run exactly one TaskManager. When the
TaskManager registers with the JobManager it sends a hardware
description of the actual hardware characteristics of the instance
(compute node). The cluster manager will attempt to match the report
hardware characteristics with one of the configured instance types.
Moreover, the cluster manager is capable of partitioning larger
instances (compute nodes) into smaller, less powerful instances.

##### Cloud instance Manager

The CloudManager is in the `nephele-ec2cloudmanager` project in the
package
*[eu.stratosphere.nephele.instance.ec2](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-ec2cloudmanager/src/main/java/eu/stratosphere/nephele/instance/ec2/ "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-ec2cloudmanager/src/main/java/eu/stratosphere/nephele/instance/ec2/")*.
The cloud manager is managing instances (compute nodes) in the cloud.
The CloudManager can allocate and release instances. There are three
types of instances: reserved instance, cloud instance and floating
instance. The reserved instance is reserved for a specific job. The
cloud instance is an instance in the running state. The floating
instance is idle and not reserved for any job. The type of an instance
in the cloud may vary among these three states. One instance belongs to
only one user, i.e. the user can use only his own instances in the
cloud. The user pays fee for allocated instances hourly. The cloud
manager checks the floating instances every 30 seconds in order to
terminate the floating instances that are to expire.
