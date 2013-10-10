---
layout: documentation
---
Execution of Meteor Queries
---------------------------

In this article, we describe the execution of a Meteor query. Meteor
provides a standalone client that can executed on a different computer
than the actual Stratosphere cluster.

As a precondition, the [Nephele
Jobmanager](clustersetup.html "wiki:clustersetup")
and the Sopremo Server must run.

### Starting Sopremo Server

First of all, the Meteor client translates a query into a SopremoPlan.
The plan is then send to the Sopremo server and executed.

The Sopremo server must run on the same computer as the Nephele
Jobmanager.

To start the Sopremo server, first adjust the server address in the
sopremo-user.xml configuration file in the conf folder.

    <property>
        <key>sopremo.rpc.address</key>
        <value>localhost</value>
    </property>

Then launch the server.

    $ ./bin/start-sopremo-server.sh

Currently, the server only executes one SopremoPlan at a time but that
will be subject to change.

### Executing the Script

The script itself may be executed on an arbitrary computer. To execute a
Meteor script, please store it in an external file. There is currently
no support for an interactive shell.

    usage: meteor-client.sh <scripts>
        --configDir <config>        Uses the given configuration
        --port <port>               Uses the specified port
        --server <server>           Uses the specified server
        --updateTime <updateTime>   Checks with the given update time in ms
                                    for the current status
        --wait                      Waits until the script terminates on the
                                    server

The Meteor client first of all requires at least one script file to
execute. Additionally, it needs the server address that can be specified
in three ways.

-   Manually specified with the –server option.
-   Written in sopremo-user.xml that resides in the folder ../conf
    relative to the meteor-client.sh
-   Written in another sopremo-user.xml. In that case, the configuration
    directory that contains that sopremo-user.xml must be specified.

Without additional parameters, the client exits immediately and prints
out whether the script was successfully enqueued.

To monitor the progress of the script, users can add the –wait option
that shows additional information about the job at the given updateTime
(default 1000 ms).

    Submitted script meteor_test.script.
    Executing script meteor_test.script.............
    Executed in 7734 ms

### Referenced Packages

All packages that are used from Meteor scripts must either be in the
classpath or in the current directory. To adjust the classpath, please
edit the meteor-script.sh or invoke it with additional -cp options.

For each package, the !Meteor/Sopremo duo checks whether there is a
current version of that package on the server and transfers it when
needed. Thus, custom Sopremo operators may be tested quite easily with
Meteor.
