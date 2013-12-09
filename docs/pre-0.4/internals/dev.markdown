---
layout: inner_docs_pre04
title:  Development
sublinks:
  - {anchor: "build", title: "Building from Sources"}
  - {anchor: "src", title: "Source Code Structure"}
  - {anchor: "eclipse", title: "Eclipse Development"}
  - {anchor: "testing", title: "Testing"}
  - {anchor: "guidelines", title: "Coding Guidelines"}
  - {anchor: "licensing", title: "Licensing"}
  - {anchor: "changes", title: "Change Policy & Log"}
  - {anchor: "contribute", title: "Contribute"}
---

<section id="build">
Build the System
================

Get the Source Code
-------------------

The latest source code is available in our Git repository.
Unfortunately, our SSL certificate does not have a valid signature chain
at the moment. You have to disable GIT's check for that. To check out
the code execute:

    git clone git://github.com/stratosphere/stratosphere.git

Compile the Source Code and Build the System
--------------------------------------------

-   Change into the source code root folder:

<!-- -->

    cd stratosphere

-   Compile the code and build the system using [Maven
    3](http://maven.apache.org/download.html "http://maven.apache.org/download.html").
    The build process will take about 7 minutes. *Unfortunatelly,
    unit-tests cannot be disabled with `-DskipTests` due to a dependency
    to test code.*

<!-- -->

    mvn clean package

The build resides in

    ./stratosphere-dist/target/stratosphere-dist-0.2-stratosphere-bin/stratosphere-0.2
    ./stratosphere-dist/target/stratosphere-dist-0.2-stratosphere-bin/stratosphere-0.2.zip
    ./stratosphere-dist/target/stratosphere-dist-0.2-stratosphere-bin/stratosphere-0.2.tar.gz

</section>

<section id="src">
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
</section>

<section id="eclipse">
Stratosphere Development with Eclipse
=====================================

Eclipse Plugins
---------------

Developing Stratosphere with the [Eclipse
IDE](http://eclipse.org/ "http://eclipse.org/") becomes more comfortable
if a set of plugins is installed. The following plugins are helpful:

-   Maven Eclipse Integration m2eclipse
    [http://eclipse.org/m2e/](http://eclipse.org/m2e/ "http://eclipse.org/m2e/")
    - Maven takes care of the project setup, dependency resolution, and
    code building.
-   Eclipse EGit Plugin
    [http://www.eclipse.org/egit/](http://www.eclipse.org/egit/ "http://www.eclipse.org/egit/")
    - Integrates the Git version control system into Eclipse
-   Eclipse JGit Plugin
    [http://www.eclipse.org/jgit/](http://www.eclipse.org/jgit/ "http://www.eclipse.org/jgit/")
    - A pure Java-based implementation of the Git version control
    system. Required by EGit.

Eclipse Code Style Configuration
--------------------------------

Please configure your Eclipse to ensure your code is compliant to the
projects code style.   
 The Eclipse code style settings of the Stratosphere project are
collected in the file
[stratosphere\_codestyle.pref](media/wiki/stratosphere_codestyle.zip "wiki:stratosphere_codestyle.zip")
that can be imported with File→Import→General/Preferences.

Import Source Code into Eclipse
-------------------------------

The Eclipse source code import is done in two stages:

1.  Clone the public Stratosphere repository.
2.  Import the code into Eclipse

### Clone Public Stratosphere Git Repository

Clone the latest revision from our public github repository:

    git clone https://github.com/stratosphere/stratosphere.git

The source code will be placed in a directory called `stratosphere`. See
[here](sourcecodestructure.html "sourcecodestructure")
for information on the structure of the source code.

### Import Source Code into Eclipse

Import the Stratosphere source code using Maven's Import tool:

-   Select “Import” from the “File”-menu.
-   Expand “Maven” node, select “Existing Maven Projects”, and click
    “next” button
-   Select the root directory by clicking on the “Browse” button and
    navigate to the top folder of the cloned Stratosphere Git
    repository.
-   Ensure that all projects are selected and click the “Finish” button.

Next, you need to connect imported sources with the EGit plugin:

-   Select all (sub-)projects.
-   Right-click on projects and select “Team” → “Share Project…” (NOT
    “Share Project*s*…”)
-   Select Git as repository type and click on “Next” button.
-   Ensure all projects are selected and click on “Finish” button.

Now, your finished with the source code import.

Eclipse and JGit
----------------

You can use JGIT to pull from and push to your repository.

Alternatively, you can issue pull and push commands using Git's default
command line client. Sometimes, that irritates Eclipse so that you must
refresh and manually clean your Eclipse projects. This is done as
follow:

1.  **Refresh:** Select all Stratosphere projects in your Package
    Explorer and hit the “F5” button.
2.  **Clean:** Select all Stratosphere projects in your Package Explorer
    and select “Project” → “Clean…” from the menu. Select “Clean all
    projects” and click the “Ok” button.

Run and Debug Stratosphere via Eclipse
--------------------------------------

Running and debugging the Stratosphere system in local mode is quite
easy. Follow these steps to run / debug Stratosphere directly from
Eclipse:

-   Use the *Run* menu and select *Run Configurations* to open the run
    configurations dialog. Create a new launch configuration. For
    debugging, create a corresponding *Debug Configuration*.
-   On the *Main* tab, select `nephele-server` as the project.
-   As the *Main-Class*, search and select *JobManager* from the package
    `eu.stratoshere.nephele.jobmanager`.
-   After having selected the JobManager as the main class, change the
    *Project* to `pact-tests`. That step is important, because the
    selected project determines how the class path will be constructed
    for the launch configuration, and we need to make sure that the pact
    classes are in the class path as well. Directly choosing
    `pact-tests` as the project in the earlier step prevents the main
    class search from finding the JobManager class (seems to be an
    Eclipse Bug, may possibly be fixed in current/later Eclipse
    versions).
-   Switch to the *Arguments* tab.
-   Under *Program Arguments*, enter
    `-configDir ”<path/to/config/dir/>” -executionMode “local”`. The
    configuration directory must include a valid configuration. The
    default configuration is good for local mode operation, so you can
    use the configuration in the
    'stratosphere-dist/src/main/stratosphere-bin/conf', which resides in
    the *stratosphere-dist* project.
-   Under *VM Arguments*, enter `-Djava.net.preferIPv4Stack=true`. You
    may also configure your heap size here, by adding for example
    `-Xmx768m -Xms768m` to dedicate 768 Megabytes of memory to the
    system.
-   Hit *Run* / *Debug* to launch the JVM running a job-manager in local
    mode.

When the JobManager is up and running, you can issue any Nephele job
against the system, either as original Nephele job or resulting from
compilation of a PACT program. The run / debug configuration needs only
to be configured once.
</section>

<section id="testing">
Effective Testing on StratoSphere
=================================

Overview - How it works
-----------------------

Modern software engineering methodologies are based on iterative and
incremental development. In **test-driven development** (TDD) the
developer first writes an automated test case (unit-test) that defines
the desired contract or specification of a function, then encodes the
actual function code to pass that test. Inspired by the context of TDD,
we designed and implemented the means for effective unit-testing with
respect to:

-   Isolation/Encapsulation: Mockups and HDFS / Nephele “local modes”
    (“almost” isolating code)
-   External configuration: Tests are parameterized by external files
    (development or release builds)
-   Continuous build integration: Tests run “out of the box”
    (self-contained standalone mini-clusters)
-   JUnit: We provide extensions to the popular JUnit framework

A user-defined **Test-Case** extends the provided TestBase class which
in turn is based on JUnit. The TestBase class acts as a “facade” and
mainly reads the configuration, initializes a ClusterProdiver instance
and runs the test case.

The central component **ClusterProvider** provides encapsulated access
to two crucial components of an execution environment: the filesystem
(local or HDFS) and the job execution engine (Nephele). Each component
can be configured to run in different execution modes. The following
execution modes are supported:

-   Filesystem
    -   local: writes files to native file system (bypassing HDFS)
    -   external HDFS: reads and writes files from/to a running
        (distributed) HDFS

-   Nephele
    -   local: starts a jobmanager and a tasktrackers locally within a
        single JVM and runs the test on them.
    -   distributed: connects to a running (distributed) Nephele setup
        (jobmanager + taskmanager(s)) and runs the test on it.

Configuration - Cluster/Test
----------------------------

To ease the **Configuration**, we separated the settings into

-   cluster profiles which specify execution environments (Filesystem +
    Nephele), and
-   test configurations which initialize a test class with a set of
    pre-defined cluster profiles.

Configurations are stored as \*.properties files and thus can be easily
overwritten (e.g. by maven depending on the current build profile)
without modifying the underlying source-code. Note that execution modes
cannot be mixed arbitrarily, e.g., running the filesystem in local mode
and Nephele in a distributed setup will fail.

The property files of the test configuration are located at:

-   Cluster profile → classpath “ClusterConfigs/\_config\_name\_”,
    directory can contain multiple configurations
-   Test configuration → classpath “TestConfigs/\_package\_name\_”, a
    test can be associated with multiple cluster configurations

Exemplary, the following configuration specifies the setting for the set
of unit-tests defined in package 'eu.stratosphere.pact.test.contracts'.

**cluster config - “ClusterConfigs/localConfig1TM.prop”**

    ClusterProvider#clusterId=local1TM
    LocalClusterProvider#numTaskTrackers=1
    ClusterProvider#clusterProviderType=LocalClusterProvider
    FilesystemProvider#filesystemType=local_fs

**test config - “TestConfigs/eu.stratosphere.pact.test.contracts.prop”**

    CoGroupTest=local1TM,local4TM
    CrossTest=local1TM
    MapTest=local1TM,local4TM
    MatchTest=local1TM
    ReduceTest=local1TM

Skeleton - TestBase
-------------------

The abstract base class “TestBase” consolidates the previously described
functionality and provides means to externalize test-case specific
configurations.

    public abstract class TestBase extends TestCase
    {
        protected abstract void preSubmit();
     
        protected abstract JobGraph getJobGraph();
     
        protected abstract void postSubmit();
    }

Essentially, the “TestBase” class performs the following four steps:

1.  Initialize the configured ClusterProvider (Filesystem + Nephele)
2.  Invoke preSubmit()
3.  Obtain the user-defined JobGraph and submit the job
4.  Invoke postSubmit()

Hence, the user-defined test case has to override the stub methods
**“preSubmit”**, **“getJobGraph”**, and **“postSubmit”**.

If the same test should be executed with different parameters,
additional steps are required:

1.  Annotate the test-class with '@RunWith(Parameterized.class)'
2.  Define a constructor that passes the configuration to the TestBase
3.  Define a 'getConfigurations()' method annotated by @Parameters

Background - Details
--------------------

In detail, the test mechanism performs the following steps:

1.  JUnit invokes a user-defined static method (e.g.
    'getConfigurations()') identified by the @Parameters annotation
2.  The function 'getConfigurations()' passes a collection of
    user-defined test options to 'TestBase.toParameterList(…)'
3.  The function 'TestBase.toParameterList(…)' performs:
    -   Read the test-configuration
    -   Read the cluster-configurations
    -   Form the cartesian product of the two sets (i) user-defined
        options and (ii) cluster-configurations
    -   Return the resulting product (all possible combinations)

4.  JUnit instantiates the test-class for each config combination
5.  The constructor of the test-class passed the current configuration
    to TestBase
6.  The TestBase class initializes the configured ClusterProvider
    (FileSystem / Nephele)
7.  The TestBase performs the following user functions defined in the
    test-class
    -   Invoke 'preSubmit()'
    -   Get the user-defined job-graph and submit a job
    -   Invoke 'postSubmit()'

Quickstart - Writing a Test Case
--------------------------------

When writing your first test case you will pursue one of two paths based
on your requirements. If it is sufficient to run the test-case once,
follow the steps described in “single configuration”. If you need to run
the test-case multiple times with varying parameters, follow the
multiple configurations description.

Single configuration:

1.  Create a test class which extends “TestBase”
2.  Override “preSubmit”, “getJobGraph” and “postSubmit”
3.  Define a cluster configuration and place it into the class-path
    folder “ClusterConfigs”
4.  Define a test configuration and place it into the class-path folder
    “TestConfigs”

Multiple configurations:

1.  Create a test class which extends “TestBase”
2.  Annotate the test class with ”@RunWith(Parameterized.class)”
3.  Define “getConfigurations()”
4.  Override “preSubmit”, “getJobGraph” and “postSubmit”
5.  Define a cluster configuration and place it into the class-path
    folder “ClusterConfigs”
6.  Define a test configuration and place it into the class-path folder
    “TestConfigs”

Sample - WordCount
------------------

The following WordCount example reads a string and counts how often
words occur. The map task takes a line as input, breaks it into words
and emits each word as a key-value pair (word, 1). Consecutively, a
reduce task aggregates all pairs with the same key (word), sums up the
values for each word and finally yields the “word counts”.

Note, that the WordCountTest class is annotated with
@RunWith(Parameterized.class) which tells JUnit to instantiate the
test-case for each user-provided configuration. Therefore, the user has
to define a method which returns a set of configurations - in our case
this method is called “getConfigurations()” and uniquely identified by
the @Parameters annotation. The method returns 5 configurations where
each of them overrides the “WordCountTest\#!NoSubtasks” parameter with a
specific value.

Further, the test configuration “WordCountTest=local1TM” assigns the
“local1TM” cluster profile to our WordCountTest class. Thus, JUnit will
instantiate the WordCountTest five times. Remember that the Cartesian
product of associated cluster profiles and the configurations returned
by “getConfigurations()” is formed; the test configuration
“WordCountTest=local1TM,local2TM,local3TM” would result in 15
WordCountTest instances.

**Java source code:**

    package eu.stratosphere.pact.test;
     
    ...
     
    @RunWith(Parameterized.class)
    public class WordCountTest extends TestBase
    {
           String TEXT = "one\n" + "two two\n" + "three three three\n";
           String COUNTS = "one 1\n" + "two 2\n" + "three 3\n";
     
           String textPath, resultPath;
     
           public WordCountTest(Configuration config)
           {
                   super(config);
           }
     
           @Override
           protected void preSubmit() throws Exception
           {
                   textPath = getFilesystemProvider().getTempDirPath() + "/text";
                   resultPath = getFilesystemProvider().getTempDirPath() + "/result";
     
                   getFilesystemProvider().createDir(textPath);
     
                   String[] splits = splitInputString(TEXT, '\n', 4);
                   for (int i = 0; i < splits.length; i++)
                   {
                           getFilesystemProvider().createFile(textPath + "/part_" + i + ".txt", split);
                           LOG.debug("Text Part " + (i+1) + ":\n>" + split + "<");
                   }
           }
     
           @Override
           protected JobGraph getJobGraph() throws Exception
           {
                   Plan plan = new WordCount().getPlan(
                                   config.getString("WordCountTest#NoSubtasks","1"), 
                                   getFilesystemProvider().getURIPrefix() + textPath,
                                   getFilesystemProvider().getURIPrefix() + resultPath);
     
                   OptimizedPlan op = new PactCompiler().compile(plan);
                   return new JobGraphGenerator().compileJobGraph(op);
           }
     
           @Override
           protected void postSubmit() throws Exception
           {
                   compareResultsByLinesInMemory(COUNTS, resultPath);
           }
     
           @Parameters
           public static Collection<Object[]> getConfigurations()
           {
                   LinkedList<Configuration> configs = new LinkedList<Configuration>();
                   for(int subtasks = 1; subtasks < 5; subtasks++)
                   {
                     Configuration config = new Configuration();
                     config.setInteger("WordCountTest#NoSubtasks", subtasks);
                     configs.add(config);
                   }
                   return toParameterList(configs);
           }
    }

**Cluster configuration:**

“ClusterConfigs/localConfig1TM.prop”

    ClusterProvider#clusterId=local1TM
    LocalClusterProvider#numTaskTrackers=1
    ClusterProvider#clusterProviderType=LocalClusterProvider
    FilesystemProvider#filesystemType=local_fs

**Test configuration:**

“TestConfigs/eu.stratosphere.pact.test.prop”

    WordCountTest=local1TM

FAQ - If something goes wrong ...
---------------------------------

**Why do I get a “Insufficient java heap space …“**

Increase the JVM heap-size - e.g. set option ”-Xmx512m”.

**Why do I get a “IPv4 stack required …“**

Disable IPv6 stack - e.g. set option ”-Djava.net.preferIPv4Stack=true”.


Unit Tests
----------

Unit tests have to meet the following criteria:

-   **high code coverage:** tests should execute different scenarios for
    the class under test; especially loops and branches in the code
    under test should be tested exhaustivly
-   **exception cases:** tests should also simulate faulty behaviour
    (e.g. wrong or missing parameters, exceptions thrown during
    execution …)
-   **one test per class:** normally there should exist one test class
    for one production class; the test class has several test cases
-   **Mocking:** dependencies to other classes or components must be
    mocked (see PowerMock/Mockito), with these mocks the tester can
    drive the test case
-   **test of functionality:** the test cases should be created on a
    functional perspective (expected behaviour of a method);
    getter-/setter-methods have not to be tested

Unit tests should be executed continuously during the coding, e.g.
before a commit. All unit tests should be run. Therefore it is necessary
to develop unit tests as limited as possible (concerning the code under
test) to ensure, that the unit test phase can be executed fast
(milliseconds!).

Bug Fixing
----------

Each reported (and accepted) bug has to be transformed into a unit test,
that tests the faulty behaviour. In the comment of the test class should
be a reference, to which bug this test is related to.

</section>

<section id="guidelines">
Coding Guidelines
=================

The guidelines cover code conventions, documentation requirements, and
utilities for easing the development of Stratosphere in Java.

We strongly recommend using
[Eclipse](http://www.eclipse.org "http://www.eclipse.org") and primarily
specify the guidelines through eclipse specific configurations and
settings. If Eclipse is not the IDE of your choice, you might have to
infer the coding guidelines by yourself. Please share the inferred
configuration of your IDE with us.

Code Conventions
----------------

We fully use the [standard code
conventions](http://java.sun.com/docs/codeconv/CodeConventions.pdf "http://java.sun.com/docs/codeconv/CodeConventions.pdf").
Following these conventions is straight-forward with Eclipse and our
predefined settings for formatter, warnings, and templates.

Be aware of the name conventions:

    class ClassNameInCamelCase implements InterfaceAlsoInCamelCase {
        private final static int CONSTANT_IN_ANSI_STYLE = 1;
     
        private int fieldStartsWithLowerCase;
     
        public void methodStartsWithLowerCase(Object parametersAsWell) {
            int soDoLocalVariables;
        }
    }

Package Conventions
-------------------

All package names of the stratosphere project start with
`eu.stratosphere.` and follow the [java naming
convention](http://www.oracle.com/technetwork/java/codeconventions-135099.html#367 "http://www.oracle.com/technetwork/java/codeconventions-135099.html#367")
for packages.

Logging Conventions
-------------------

The following section defines when which log level should be used.

-   **Debug:** This is the most verbose logging level (maximum volume
    setting). Here everything may be logged, but note that you may
    respect privacy issues. Don't log sensitive information (this of
    course applies to all log levels). This log level is not intended to
    be switched on in a productive system.

-   **Trace:** This is a special debug level. With this level turned on
    a programmer may trace a specific error in a test or productive
    system. This level is not as verbose as debug, but gives enough
    information for a programmer, to identify the source of a problem.
    For example entering/exiting log messages for methods can be logged
    here.

-   **Info:** The information level is typically used to output
    information that is useful to the running and management of your
    system. This level should be the normal log level of a productive
    system. Info messages should be 1-time or very occasional messages.
    For example the start and stop of a specific component can be logged
    here (e.g. Jobmanager, Taskmanager etc.)

-   **Warn:** Warning is often used for handled 'exceptions' or other
    important log events. Warnings are exceptions from which the system
    can recover and where the part of the system, which produced the
    exception, can nevertheless give a reasonable result.
-   **Example:** Suppose you are iterating over a list of integer
    objects, which a functions wants to sum up. One entry in this list
    is a null pointer. While summing up, the function sooner or later
    reaches the null pointer and produces a null pointer exception. This
    could be logged as a warning and “interpreted” as summing up a 0.

-   **Error:** Errors are exceptions, from which the system can recover,
    but which prevents the system from fulfilling a service/task.

-   **Fatal:** Fatal is reserved for special exceptions/conditions where
    it is imperative that you can quickly pick out these events.
    Exceptions are fatal, if the system can not recover fully from this
    exception (e.g. a component is down because of this exception). This
    level should be used rarely.

Code Style Conventions
----------------------

We have defined a code style for the Stratosphere project to which all
the source code files should comply to.   
 See the [Eclipse
Import](eclipseimport.html "eclipseimport")
page for style configuration files and Eclipse configuration
instructions.

</section>

<section id="licensing">
Licensing
=========

Stratosphere is released under [Apache License, Version
2.0](http://www.apache.org/licenses/LICENSE-2.0.html "http://www.apache.org/licenses/LICENSE-2.0.html").
</section>

<section id="changes">

Update and Change Policy
========================

**We do not modify the binary builds of released versions.**   

Bug fixes and last changes are published in the public Git repository.
  
 Please follow the
[instructions](buildthesystem.html "buildthesystem")
to build the latest version of Stratosphere from the Git repository.   

All major changes and bug fixes made to released versions are logged:

-   [Updates and Changes on Release
    0.1](updateschangeson0.1.html "updateschangeson0.1")


Release Changelogs
==================

-   [Release 0.1: Changelog and
    Updates](updateschangeson0.1.html "updateschangeson0.1")

Changelog and Updates of Release 0.1
====================================

This page logs all important updates and bug fixes made to release 0.1
of the Stratosphere system.   

Updates and Bug Fixes on Release 0.1
------------------------------------

Updates and Bug Fixes are only available in the source distribution
available in our public Git repository ([build
instructions](buildthesystem.html "buildthesystem")).
  
 Of course, they will be included in the binary distributions of future
releases.

2011/05/16:: Improved shell scripts for MacOS compatibility.
Stratosphere can be started on MacOS now. 2011/05/12:: Fixed non-ASCII
serialization bug in PactString. PactString serializes and deserializes
Java (UTF-16) strings now.

-   [Release 0.1.1: Changelog and
    Updates](changesrelease0.1.1.html "changesrelease0.1.1")

Changelog and Updates of Release 0.1.1
======================================

This page shows the important changes of the Stratosphere system
introduced by release 0.1.1. It also logs all important updates and bug
fixes made to release 0.1.1.   

Changelog Release 0.1.1
-----------------------

### New Features

-   Introduced hash-based local strategy for Match input contracts. Now
    the optimizer can choose between a hash- and a sort-based local
    strategy.
-   Support for user-defined generation of input split
-   Input splits are lazily assigned to data source tasks for improved
    load balancing.
-   Improved support for low-latency programs; some programs with few
    data got a significant speed-up.
-   Support for Amazon S3 as data sources/sinks
-   Support for Snappy compression
-   MacOS support

### Included Bug Fixes

-   Serialization of PactString data types
-   Generation of small input splits
-   Several bugfixes in Nephele EC2 cloud manager
-   For a complete list of bug fixes, see the [bug
    tracker](http://dev.stratosphere.eu/query?status=closed&order=priority&col=id&col=summary&col=status&col=type&col=priority&col=milestone&col=component&milestone=Release+0.1.1 "http://dev.stratosphere.eu/query?status=closed&order=priority&col=id&col=summary&col=status&col=type&col=priority&col=milestone&col=component&milestone=Release+0.1.1").

Updates and Bug Fixes on Release 0.1.1
--------------------------------------

Updates and Bug Fixes are only available in the source distribution
available in our public Git repository ([build
instructions](buildthesystem.html "buildthesystem")).
  
 Of course, they will be included in the binary distributions of future
releases.


-   [Release 0.1.2: Changelog and
    Updates](changesrelease0.1.2.html "changesrelease0.1.2")

Changelog and Updates of Release 0.1.2
======================================

This page shows the important changes of the Stratosphere system
introduced by release 0.1.2. It also logs all important updates and bug
fixes made to release 0.1.2.   

Changelog Release 0.1.2
-----------------------

### New Features

### Included Bug Fixes

-   For a complete list of bug fixes, see the [bug
    tracker](http://dev.stratosphere.eu/query?status=closed&groupdesc=1&order=changetime&col=id&col=summary&col=milestone&col=status&col=type&col=priority&col=component&milestone=Release+0.1.2 "http://dev.stratosphere.eu/query?status=closed&groupdesc=1&order=changetime&col=id&col=summary&col=milestone&col=status&col=type&col=priority&col=component&milestone=Release+0.1.2").

Updates and Bug Fixes on Release 0.1.2
--------------------------------------

Updates and Bug Fixes are only available in the source distribution
available in our public Git repository ([build
instructions](buildthesystem.html "buildthesystem")).
  
 Of course, they will be included in the binary distributions of future
releases.

-   [Release 0.2: Changelog and
    Updates](changesrelease0.2.html "changesrelease0.2")

Nephele
-------

-   Major performance optimizations to runtime engine:
    -   Unification of channel model
    -   Support for multi-hop data routing
    -   Dynamic redistribution of memory resources
    -   Lazy task deployment to avoid allocation of resources by waiting
        tasks
    -   Several scalability improvements

-   Fault tolerance:
    -   Support for local logs for intermediate results
    -   Replay from completed/incomplete logs to recover from task
        failures
    -   Logs can be configured on a task level

-   Support for plugins:
    -   External libraries can now hook into the schedule to
        monitor/influence the job execution
    -   Custom wrappers for user code to collect statistical information
        on the task execution

-   Support for application-layer multicast network transmissions:
    -   Identical data fragements (for example in case of a broadcast
        transmission) are now distributed through a topology-aware
        application-layer multicast tree

Pact
----

-   Data-Model changed to PactRecord, ageneric tuple model (see
    [http://www.stratosphere.eu/wiki/doku.php/wiki:PactRecord](http://www.stratosphere.eu/wiki/doku.php/wiki:PactRecord "http://www.stratosphere.eu/wiki/doku.php/wiki:PactRecord"))
    -   Schema free tuples
    -   Lazy serialization / deserialization of fields
    -   Sparse representation of null fields

-   Support for composite keys for Reduce / Match / CoGroup
-   Efficient Union of data streams
-   Generic User-Code annotations replace Output Contracts
    -   Constant fields and cardinality bounds

-   Added range partitioning and global sorting for results
-   Added support for sorted input groups for Reduce and CoGroup (a.k.a.
    Secondary Sort)
-   Removed limitation in record length for sort/hash strategies
-   Various runtime performance improvements
    -   Runtime object model changed to Mutable Objects
    -   Memory management changed to memory page pools
    -   Unified handling of data in serialized form via memory views
    -   Reworked all basic runtime strategies (sorting / hashing /
        damming)
    -   Some performance improvements for parsers / serializers

-   Extended library of input/output formats
    -   Generic parameterizable tuple parsers / serializers
    -   InputFormat that start and read from external processes

-   Introduction of NormalizedKeys for efficient sorting of custom data
    types
-   Support for multiple disks/directories for parallel I/O operations
    on temporary files (temp storage, sort / hash overflows)
-   Chaining support for Mappers/Combiners (elimination of intermediate
    serializing)
-   Changed Runtime to work on generic data types (currently only with
    manual plan composition).

Sopremo
-------

-   Initial Version of Sopremo added: Extensible operator model
    organized in packages
-   Data model based on Json
-   Includes base package of operators for structured and
    semi-structured data
-   Server/Client interface to remotely execute plans on a cluster
    -   Automatically ships Sopremo packages when package on server is
        outdated

Meteor
------

-   Initial Version of Meteor added: Extensible query language to create
    Sopremo plans
-   Command line client, to execute scripte remotely

</section>

<section id="contribute">
git workflow
</section>