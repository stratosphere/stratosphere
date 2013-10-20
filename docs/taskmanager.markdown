---
layout: documentation
---
TaskManager
-----------

A task manager receives tasks from the job manager and executes them.
After having executed them (or in case of an execution error) it reports
the execution result back to the job manager. Task managers are able to
automatically discover the job manager and receive its configuration
when the job manager is running on the same local network. The task
manager periodically sends heartbeats to the job manager to report that
it is still running.

### Interface

The task manager implements the
*[TaskOperationProtocol](https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/protocols/TaskOperationProtocol.java "https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/protocols/TaskOperationProtocol.java")*
interface which is defined as part of the `nephele-server` project in
the package
*[eu.stratosphere.nephele.protocols](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/protocols "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/protocols")*.
It provides methods for the job manager to submit and cancel tasks, as
well as to query the task manager for cached libraries and submit these
if necessary.
