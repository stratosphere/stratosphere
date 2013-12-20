---
layout: inner_docs_v04
title:  "CLI Client"
---

## CLI Client

Stratosphere provides a commandline client to:

- submit jobs for execution,
- cancel a running job,
- provide information about a job, and
- list running and waiting jobs.

### Usage

The client is used as follows:

    ./bin/stratosphere [[ACTION]] [[GENERAL_OPTIONS]] [[ACTION_ARGUMENTS]]
      general options:
         -h,--help      Show the help for the CLI Frontend.
         -v,--verbose   Print more detailed error messages.

    Action "run" compiles and submits a Stratosphere program.
      "run" action arguments:
         -a,--arguments <programArgs>   Program arguments
         -c,--class <classname>         Program class that assembles the plan
         -j,--jarfile <jarfile>         JAR file that contains the program
         -m,--jobmanager <host:port>    Jobmanager to which the program is submitted
         -w,--wait                      Return after program finished

    Action "info" displays information about a Stratosphere program.
      "info" action arguments:
         -a,--arguments <programArgs>   Program arguments
         -c,--class <classname>         Program class that assembles the plan
         -d,--description               Show description of expected program arguments
         -j,--jarfile <jarfile>         JAR file that contains the program
         -p,--plan                      Show optimized execution plan of the program (JSON)

    Action "list" lists submitted Stratosphere programs.
      "list" action arguments:
         -r,--running     Show running programs and their JobIDs
         -s,--scheduled   Show scheduled prorgrams and their JobIDs

    Action "cancel" cancels a submitted Stratosphere program.
      "cancel" action arguments:
         -i,--jobid <jobID>   JobID to cancel

### Example Usage:

-   Run WordCount example program on the JobManager configured in ./conf/stratosphere.yaml:

        ./bin/stratosphere run -j ./examples/stratosphere-java-examples-0.4-WordCount.jar \
                               -a 4 file:///home/user/hamlet.txt file:///home/user/wordcount_out

- Run WordCount example program on a specific JobManager:

        ./bin/stratosphere run -m myJMHost:6123 \
                               -j ./examples/stratosphere-java-examples-0.4-WordCount.jar \
                               -a 4 file:///home/user/hamlet.txt file:///home/user/wordcount_out

-   Run WordCount example program (program class not defined in the JAR file manifest):

        ./bin/stratosphere run -j ./examples/stratosphere-java-examples-0.4-WordCount.jar \
                               -c eu.stratosphere.example.java.record.wordcount.WordCount \
                               -a 4 file:///home/user/hamlet.txt file:///home/user/wordcount_out

-   Display the expected arguments for the WordCount example program:

        ./bin/stratosphere info -d \
                                -j ./examples/stratosphere-java-examples-0.4-WordCount.jar

-   Display the optimized execution plan for the WordCount example program as JSON:

        ./bin/stratosphere info -p \
                                -j ./examples/stratosphere-java-examples-0.4-WordCount.jar \
                                -a 4 file:///home/user/hamlet.txt file:///home/user/wordcount_out

-   List scheduled and running jobs (including their JobIDs):

        ./bin/stratosphere list -s \
                                -r

-   Cancel a job:

        ./bin/stratosphere cancel -i <jobID>
