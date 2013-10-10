---
layout: documentation
---
Source Code Structure
=====================

The source code of the Stratosphere project is structured in Maven
modules. The project consists of four main modules:

-   **build-tools:** Contains tools for building Eucalyptus images and
    other related tools.
-   **nephele:** Contains the source code of the Nephele system.
    Described in detail below.
-   **pact:** Contains the source code of the PACT components. Described
    in detail below.
-   **sopremo:** Contains the source code of the Sopremo higher level
    language components.
-   **stratosphere-dist:** Meta-Module used for building and packaging,
    does also contain Stratosphere shell-scripts.

In the following we describe the Maven modules of Nephele and PACT in
detail. Both consist again of multiple Maven sub-modules.

Nephele
-------

Nephele is divided into several Maven modules:

-   **nephele-common** contains all classes to develop and submit a
    Nephele program. That includes
    -   Templates for Nephele task which encapsulate the PACTs or custom
        user code
    -   All classes to construct a JobGraph, i.e. the job description of
        Nephele
    -   All classes to submit a Nephele job to the runtime system

-   **nephele-server** contains basic components of the execution
    engine. This includes
    -   The [Job
        Manager](jobmanager.html "jobmanager")
        which is responsible for the coordinating the execution of a
        job's individual tasks
    -   The [Task
        Manager](taskmanager.html "taskmanager")
        which runs on the worker nodes and actually executes the tasks
    -   Various services for memory and I/O management

-   **nephele-hdfs** is the Nephele binding to the HDFS file system.

-   **nephele-clustermanager** is the default [instance
    manager](instancemanager.html "instancemanager")
    Nephele uses in cluster mode. It is responsible for
    -   Providing suitable compute resources according to hardware
        demand of a task
    -   Monitoring the availability of the worker nodes

-   **nephele-ec2cloudmanager** is the default [instance
    manager](instancemanager.html "instancemanager")
    Nephele uses in cloud mode. It is responsible for
    -   Allocating and deallocating compute resources from a cloud with
        an EC2-compatible interface
    -   Monitoring the availability of the worker nodes

-   **nephele-queuescheduler** contains a simple FIFO job scheduler.
    It's the default job scheduler for Nephele in cluster and cloud
    mode.

-   **nephele-compression-**\* provides compression capabilities for
    Nephele's network and file channels. This includes
    -   Wrapper for different compression libraries (bzip2, lzma, zlib)
    -   A decision model for adaptive compression, i.e. the level of
        compression is adapted dynamically to the current I/O and CPU
        load situation

-   **nephele-examples** contains example Nephele programs. These
    examples illustrate
    -   How to write individual Nephele tasks
    -   How to compose the individual tasks to a Nephele JobGraph
    -   How to submit created jobs to the Nephele runtime system
    -   See the [Writing Nephele
        Jobs](writingnehelejobs.html "writingnehelejobs")
        Page to learn how to write your own example

-   **nephele-management** contains management API that goes beyond the
    capabilities of nephele-common. This includes an
    -   API to retrieve the parallelized structure of a JobGraph that
        exists at runtime
    -   API to retrieve management events collected during a job
        execution
    -   API to query the network topology which connects a set of worker
        nodes
    -   API for advanced debugging

-   **nephele-profiling** is an optional extension for profiling the
    execution of a Nephele job. This includes
    -   Profiling of threads using the appropriate management beans
    -   Profiling of worker nodes using various OS-level interfaces

-   Conversion of the individual profiling data to summarized profiling
    data

-   **nephele-visualization** is a graphical user interface. It allows
    you to
    -   Inspect the parallelized structure of a JobGraph that exists at
        runtime
    -   View profiling information as reported by the nephele-profiling
        component
    -   View the network topology as determined by Nephele

PACT
----

Similar to Nephele, the PACT project also consists of multiple Maven
modules:

-   **pact-common** contains all classes required to write a PACT
    program. This includes:
    -   Stubs for all PACTs
    -   DataFormats to read from DataSources and write to DataSinks
    -   Contracts for all PACTs, DataSources and DataSinks
    -   PACT plan and plan construction code
    -   Data types (basic data types and abstract classes to write own
        data types)
    -   See [PACT program
        documentation](writepactprogram.html "writepactprogram")
        to learn how to write PACT programs.

-   **pact-runtime** contains the source code that is executed by the
    Nephele engine. This includes:
    -   Task classes for all PACTs and internal tasks (such as the
        TempTask)
    -   Code for local strategies such as SortMerger, HashTable, and
        resettable iterators
    -   De/Serializers

-   **pact-compiler** contains the sources of the PACT optimizer,
    Nephele job graph generator, and frontend. This includes:
    -   PACT compiler that transforms and optimizes PACT programs into
        an intermediate representation
    -   Nephele job graph generator that constructs a job graph from the
        intermediate representation

-   **pact-clients** contains clients and interfaces to execute PACT
    jobs, including:
    -   Web frontend to upload PACT programs, check the optimized PACT
        program with a visualization frontend, and start the job on
        Nephele.
    -   Command line interface to run PACT programs from the command
        line
    -   A client library to run PACT programs directly from sourcecode
    -   See [PACT client
        documentation](executepactprogram.html "executepactprogram")

-   **pact-examples** contains example PACT programs.
    -   Example PACT jobs. See [PACT example
        documentation](pactexamples.html "pactexamples")
    -   Job launchers
    -   Test data generators

-   **pact-tests** contains End-to-end tests for the Nephele/PACT
    system. This includes:
    -   A flexible, configurable test environment (incl. locally started
        HDFS and Nephele)
    -   Test cases for individual PACTs
        (`eu.stratosphere.pact.test.contracts`).
    -   Test cases for more complex jobs based on the examples in the
        pact-examples module (`eu.stratosphere.pact.test.pactPrograms`).


