---
layout: inner_docs_v04
title:  "CLI Client"
---

## CLI Client

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
