---
layout: inner_docs_v04
title:  "Web Interface"
---

## Web Interface


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
