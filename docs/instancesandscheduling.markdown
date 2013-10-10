---
layout: documentation
---
Resource Scheduling
-------------------

The scheduling and assignment of system resources to tasks is done
through so called **Instance Types**. An instance type is something like
a hardware profile. The concept originates from Amazon's EC2, where
instance types define the type of the instances you want to allocate,
with respect to their resources. An instance type is described in the
configuration as a comma-separated string composed of the following
values:

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

An example for an instance type definition would be
*“standard,2,1,1024,20,0”*.

Whenever resources are announced as available, or resources are reserved
(such as for a parallel instance of a PACT, or a Nephele Task), the
request for the resources is attached the name of an instance type, to
describe what collection of resources is referred to. For example, each
TaskManager has an Instance Type, that describes its available
resources, such as available memory, disk and CPU power. The type is
determined automatically at the task-manager's startup (by inspecting
the systems hardware configuration, which currently only works on
Linux), matching the system resources of the hosting computer against
the available instance types. The matching is done conservatively: the
Task Manager selects the largest Instance Type that describes a less or
equal amount of resources than it actually determines to have available.
An instance type is currently defined larger than another instance type,
if it has more memory, and in case of equal amount of memory, more CPU
capacity. See [Cluster
Setup](clustersetup#clusterinstanceconfig "clustersetup")
for details.

In the current state, a fix set of instance types is used, which needs
to be pre-defined in the configuration. It is hence important to include
Instance Type descriptions that the Task Managers can match. If you have
for example a heterogeneous cluster with three types of machines running
Task Managers, you should have three different Instance Types described,
that can be matched by them. Otherwise, all Task Managers will match the
same small instance type and the larger hosts will announce less
resources as available than they actually have.

* * * * *

The picture below shows an example cluster with six Task Managers on
three different types of machines. A job is executed on the cluster with
a degree-of-parallelism of 12. The smallest instance type is selected as
the type that defines the resources per parallel task. In the given
example, the Task Manager in the large machine is assigned four
instances (leaving half of its resources free), the small machines are
completely reserved, the first medium sized machine is fully allocated,
and the remaining medium sized machine is assigned one instance. The
example shows how heterogeneous nodes are treated in Stratosphere.

[![](media/wiki/resource_allocation.png)](media/wiki/resource_allocation.png "wiki:resource_allocation.png")

The following excerpt shows the instance type configuration for the
above example:

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>

            .
            .
            <property>
                    <key>instancemanager.cluster.type.1</key>
                    <value>small,2,1,4096,100,0</value>
            </property>

            <property>
                    <key>instancemanager.cluster.type.2</key>
                    <value>med,4,2,16384,300,0</value>
            </property>

            <property>
                    <key>instancemanager.cluster.type.3</key>
                    <value>large,8,4,32768,1000,0</value>
            </property>
            .
            .
    </configuration>
