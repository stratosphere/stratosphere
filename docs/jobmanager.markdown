---
layout: documentation
---
JobManager
==========

In Nephele, the job manager is the central component for communicating
with clients, creating schedules for incoming jobs, and supervising the
execution of the jobs. A job manager may only exist once in the system
and its address must be known to all clients. [Task
managers](taskmanager.html "taskmanager")
can discover the job manager by means of an UDP broadcast and then
advertise themselves as new workers for tasks.

If a job graph is submitted from a client to the job manager, each task
of the job will be sent to a task manager. The job manager periodically
receives heartbeats from the task managers and propagates them to the
instance manager. To supervise the execution of the job, each task
manager sends information to the job manager about changes in the
execution states of a task (e.g.: changing to FAILED).
