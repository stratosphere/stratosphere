---
layout: inner_docs_pre04
title:  Program Execution
sublinks:
  - {anchor: "pm", title: "Command Line Client"}
  - {anchor: "compiler", title: "Web Client"}
  - {anchor: "strategies", title: "Client API"}
---
Execute PACT Programs
=====================

The Stratosphere system comes with different clients for PACT program
submission.   
 Currently, we have a command-line client, a web-client, and a Java
library to submit PACT programs directly from within an arbitrary Java
program (also called embedded client).

**Note:** Each client needs access at least to a minimal configuration.
As the bare minimum, the connection information for Nephele's JobManager
(host and port) must be set in the configuration that the client uses.
When started from the same directory, the command-line client and the
web-client share Nephele's configuration. The embedded client, however,
must be passed a configuration with those values manually. Keep in mind
that the PACT compiler runs in the client, so it will operate using the
configuration of the client.

Prepare a Program for Execution
===============================

In order to execute a PACT program on a Nephele cluster it must be
packaged into a JAR file. That file needs to contain all necessary class
files for execution. The JAR typically has a Manifest file with a
special entry   
 ”*Pact-Assembler-Class: \<your-plan-assember-class\>*” which defines
the plan assembler class.   
 **Example manifest file:**

    Manifest-Version: 1.0
    Pact-Assembler-Class: eu.stratosphere.pact.example.wordcount.WordCount

If the plan assembler class is not described within the manifest, its
fully qualified name must be passed as a parameter to the client.

Specify Data Paths
------------------

The paths for data sources and data sinks must be specified as absolute
paths with filesystem prefix. Data can be read from single files or
directories. For more detail consult the Tutorial on [How to write a
Pact
Program](writepactprogram#reading_data_sources "writepactprogram").

-   HDFS Path hdfs://host:port/path/to/data/in/hdfs
-   Local Path (for local setup) file:///path/to/data/in/local/fs

The PACT Command Line Client
============================

The PACT command-line client features actions to submit, cancel, list,
and get information about PACT programs.   

### Usage

    ./pact-client [[ACTION]] [[GENERAL_OPTIONS]] [[ACTION_ARGUMENTS]]
      general options:
         -h,--help      Show the help for the CLI Frontend.
         -v,--verbose   Print more detailed error messages.

    Action "run" compiles and submits a PACT program.
      "run" action arguments:
         -a,--arguments <programArgs>   Pact program arguments
         -c,--class <classname>         Pact program assembler class
         -j,--jarfile <jarfile>         Pact program JAR file
         -w,--wait                      Wait until program finishes

    Action "info" displays information about a PACT program.
      "info" action arguments:
         -a,--arguments <programArgs>   Pact program arguments
         -c,--class <classname>         Pact program assembler class
         -d,--description               Show argument description of pact
                                        program
         -j,--jarfile <jarfile>         Pact program JAR file
         -p,--plan                      Show execution plan of the pact
                                        program

    Action "list" lists submitted PACT programs.
      "list" action arguments:
         -r,--running     Show running jobs
         -s,--scheduled   Show scheduled jobs

    Action "cancel" cancels a submitted PACT program.
      "cancel" action arguments:
         -i,--jobid <jobID>   JobID to cancel

### Basic Actions

-   Run WordCount PACT example program:

<!-- -->

    ./bin/pact-client.sh run -j ./examples/pact/pact-examples-0.1-WordCount.jar -a 4 file:///home/user/hamlet.txt file:///home/user/wordcount_out

-   Run WordCount PACT example program (Plan-Assembler not in JAR file
    manifest):

<!-- -->

    ./bin/pact-client.sh run -j ./examples/pact/pact-examples-0.1-WordCount.jar -c eu.stratosphere.pact.example.wordcount.WordCount -a 4 file:///home/user/hamlet.txt file:///home/user/wordcount_out

-   Display WordCount PACT example program argument description:

<!-- -->

    ./bin/pact-client.sh info -d -j ./examples/pact/pact-examples-0.1-WordCount.jar

-   Display WordCount PACT example program execution plan as JSON:

<!-- -->

    ./bin/pact-client.sh info -p -j ./examples/pact/pact-examples-0.1-WordCount.jar -a 4 file:///home/user/hamlet.txt file:///home/user/wordcount_out

-   List scheduled and running jobs (including job ids):

<!-- -->

    ./bin/pact-client.sh list -s -r

-   Cancel a job:

<!-- -->

    ./bin/pact-client.sh cancel -i <jobID>

### Configure the Command Line Client

The command-line client is configured in *./conf/pact-user.xml*. The
configuration includes parameters that are used by the compiler, such as
the default parallelism, intra-node parallelism, and the limit to the
number of instances to use.   
 The JobManager to which PACT programs are submitted is configured by
specifying host and port in *./conf/nephele-user.xml*.   
 Refer to the [Configuration
Reference](configreference.html "configreference")
for details.

The PACT Web Interface
======================

### Start the Web Interface

To start the web interface execute:

    ./bin/pact-webfrontend.sh

By default, it is running on port 8080.

### Use the Web Interface

The PACT web client has two views:

1.  A submission view to upload, preview, and submit PACT programs.
2.  An explain view to analyze the optimized plan, node properties, and
    optimizer estimates.

##### Submission View

The client is started with the submission view. The following screenshot
shows the client's submission view:

[![](media/wiki/pactwebclient_submit.png)](media/wiki/pactwebclient_submit.png "wiki:pactwebclient_submit.png")

**Features of the submission view**

-   **Upload a new PACT program as JAR file**: Use the form in the right
    bottom corner.
-   **Delete a PACT program**: Click on the red cross of the program to
    be deleted in the left hand side program list.
-   **Preview a PACT program**: Check the checkbox of the program to
    preview in the left hand side program list. If the JAR file has a
    valid Manifest with assembler-class entry, the program's preview is
    shown on the right pane.
-   **Run a PACT program and show optimizer plan**: Check the program to
    execute in the left hand side program list. Give all required
    parameters in the input field “Arguments” and click on the “Run job”
    button. After hitting the “Run job” button, the explain view will be
    displayed. To run the job from there, click on the “Run” button in
    the right bottom corner.
-   If the plan assembler class implements the
    *PlanAssemblerDescription* interface, its returned string is
    displayed when the program is checked.
-   If the PACT program has no Manifest file with a valid
    assembler-class entry, you can specify the assembler class in the
    “Arguments” input file before any program arguments with

        assembler <assemblerClass> <programArgs...>

-   **Run a PACT program directly**: Uncheck the option “Show optimizer
    plan” and follow the instructions as before. After hitting the “Run
    job” button, the job will be directly submitted to the Nephele
    JobManager and scheduled for execution.

##### Explain View

The following screenshot shows the web client's explain view:

[![](media/wiki/pactwebclient_explain.png)](media/wiki/pactwebclient_explain.png "wiki:pactwebclient_explain.png")

The view displays a compiled PACT program before it is submitted to
Nephele for execution and is horizontally split two parts:

-   A plan view on the upper half.
-   A detail view on the lower half.

The plan view shows the data flow of the PACT program. In contrast to
the preview, which is available on the Submission View, the data flow on
the Explain View shows the optimization decisions of the PACT compiler:

-   Ship Strategies: The gray boxes display the chosen ship strategy for
    the data flow connections.
-   Local Strategies: When clicking on the ”+” in a node's bottom right
    corner, the node is expanded and the chosen local strategy for that
    PACT is displayed.
-   Node Estimates: When clicking on the ”+” in the bottom right corner
    of a PACT node, the size estimates for that PACT are displayed.

The detail view shows the details of a PACT node. To change the PACT
whose details are displayed, click on the corresponding node in the
upper plan view. The following information is displayed:

-   PACT Properties: Details on the selected PACT. Refer to the section
    [Writing Pact
    Programs](writepactprogram.html "writepactprogram")
    to learn how to set Output Contracts and the degree of parallelism.
-   Global Data Properties: Information on the global properties of the
    data after the PACT (including the user function) was applied, i.e.
    properties that hold over all parallel instances.
-   Local Data Properties: Information on the local properties of the
    data after the PACT (including the user function) was applied, i.e.
    properties that hold within all parallel instances.
-   Size Estimates: Size estimates of the PACT compiler for relevant
    metrics which are the basis for the applied optimization. Estimates
    for the data sources are obtained through simple sampling. Estimates
    for further nodes are based on the [Compiler
    Hints](writepactprogram#compilerhints "writepactprogram"),
    or certain default assumptions.
-   Cost Estimates: Cost estimates of the PACT compiler for the selected
    PACT and cumulative costs for the data flow up to and including the
    selected PACT.
-   Compiler Hints: Compiler hints that have been attached to the PACT.
    Refer to the [Compiler
    Hints](writepactprogram#compilerhints "writepactprogram")
    section for details.

### Configure the Web Interface

The web interface is configured in *./conf/pact-user.xml*. Values like
the server port and the directory for job uploads are defined there. The
configuration also states parameters that are used by the compiler, such
as the default parallelism, intra-node parallelism, and the limit to the
number of instances to use.   
 The JobManager to which PACT programs are submitted is configured by
specifying host and port in *./conf/nephele-user.xml*.   
 Refer to the [Configuration
Reference](configreference.html "configreference")
for details.

The PACT Client API (Embedded Client)
=====================================

For executing Pact programs from Java source code, a simplified Client
API is provided. The API makes it possible to submit PACT programs from
within any Java application. Both the command line client and the web
client internally use that API. It consists of the following two
classes:

**eu.stratosphere.pact.client.nephele.api.PactProgram**: This class
wraps all PACT program related code. The class has to be instantiated
with the path to the jar file and the arguments for the program.

**eu.stratosphere.pact.client.nephele.api.Client**: The client class
wraps all code related to sending a program to the Nephele job manager.
The relevant method is *Client\#run(PactProgram)* method which accepts a
pact program as argument and sends that to the job manager. More methods
are provided, which allow for example only compilation without
submission, or submission of a pre-compiled plan.

A minimal example for sending a readily packaged pact program to the job
manager is:

    File jar = new File("./path/to/jar");
    // load configuration
    GlobalConfiguration.loadConfiguration(location);
    Configuration config = GlobalConfiguration.getConfiguration();

    try {
      PactProgram prog = new PactProgram(jar);
      Client client = new Client(configuration);
      client.run(prog);
    } catch (ProgramInvocationException e) {
      handleError(e);
    } catch (ErrorInPlanAssemblerException e) {
      handleError(e);
    }

As mentioned before, the embedded client requires a configuration to be
passed, describing how to reach the JobManager. As a minimum, the values
*jobmanager.rpc.address* and *jobmanager.rpc.port* must be set. The
parameters that define the behavior of the PactCompiler (such as the
default parallelism, intra-node parallelism, and the limit to the number
of instances to use) are also drawn from this configuration. Please
refer to the [Configuration
Reference](configreference.html "configreference")
for details.

As an alternative, the client can be started with a Java
[InetSocketAddress](http://download.oracle.com/javase/6/docs/api/java/net/InetSocketAddress.html "http://download.oracle.com/javase/6/docs/api/java/net/InetSocketAddress.html"),
that describes the connection to the JobManager. The configuration for
the compiler is assumed empty in that case, causing the default
parameters to be used for compilation.
