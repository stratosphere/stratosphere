---
layout: documentation
---
Nephele Architecture
====================

[![Architecture](media/wiki/nephelearch.png "Architecture")](media/wiki/nephelearch.png "wiki:nephelearch.png")

Before submitting a Nephele compute job, a user must start an instance
inside the cloud which runs the so called [Job
Manager](jobmanager.html "jobmanager").
The Job Manager receives the client’s jobs, is responsible for
scheduling them and coordinates their execution. It can allocate or
deallocate virtual machines according to the current job execution
phase. The actual execution of tasks is carried out by a set of
instances. Each instance runs a local component of the Nephele framework
([Task
Manager](taskmanager.html "taskmanager")).
A Task Manager receives one or more tasks from the Job Manager at a
time, executes them and informs the Job Manager about their completion
or possible errors. Unless a job is submitted to the Job Manager, we
expect the set of instances (and hence the set of Task Managers) to be
empty. Upon job reception, the Job Manager decides depending on the
particular tasks inside a job, how many and what kind of instances the
job should be executed on. It also decided when respective instances
must be allocated or deallocated in order to ensure a continuous but
cost-efficient processing.

For details on the architectural components, refer to the following
sections:

-   [JobManager](jobmanager.html "jobmanager")
-   [TaskManager](taskmanager.html "taskmanager")
-   [InstanceManager](instancemanager.html "instancemanager")
-   [Scheduler](scheduler "scheduler")
-   [MemoryManager](memorymanager.html "memorymanager")
-   [IOManager](iomanager.html "iomanager")
-   [NepheleGUI](nephelegui "nephelegui")
-   [Client](nephele.html "nephele")

