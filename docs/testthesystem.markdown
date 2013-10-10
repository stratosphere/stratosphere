---
layout: documentation
---
[[PageOutline]](pageoutline "pageoutline")

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
