---
layout: documentation
---
Writing a PACT Program
======================

This section assumes you are familiar with the [PACT Programming
Model](pactpm.html "pactpm").
It explains how you implement a designed pact program. We start with the
implementation of a PACT program and explain later how programs are
packaged for execution.

Implement a PACT Program
------------------------

The implementation of PACT programs is done in Java. This section
describes the required components, the interfaces and abstract classes
to implement and extend, and how to assemble them to a valid PACT
program.   
 A set of example PACT programs with links to their source code can be
found in the
[Examples](pactexamples.html "pactexamples")
section.

### Code Dependencies

PACT programs depend on the *pact-common.jar* and transitively on its
dependencies. The easiest way to resolve all dependencies is to include
the public Stratosphere Maven repository into your *pom.xml* file and
declare the *pact-common* artifact as a dependency.   
 The following code snippet shows the required sections in the *pom.xml*
of your Maven project:

    <project>
      ...
      <repositories>
        <repository>
          <id>stratosphere.eu</id>
          <url>http://www.stratosphere.eu/maven2/</url>
        </repository>
        ...
      </repositories>
      ...
      <dependencies>
        <dependency>
          <groupId>eu.stratosphere</groupId>
          <artifactId>pact-common</artifactId>
          <version>0.2</version>
          <type>jar</type>
          <scope>compile</scope>
        </dependency>
        ...
      </dependencies>
    ...
    </project>

### Basic Components and Structure of a PACT Program

A PACT program basically consist of four components:

-   **Data types**: Primitive types (Integer, String, Double, etc.) are
    provided in
    *[eu.stratosphere.pact.common.type.base](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/base "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/base")*.
      
     More complex types must implement the interfaces
    *[eu.stratosphere.pact.common.api.common.type.Key](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/Key.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/Key.java")*
    or
    *[eu.stratosphere.pact.common.api.common.type.Value](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/Value.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/Value.java")*.
      
       
     **Important:** All data types must implement a default constructor
    (no arguments expected)!   

    *[compareTo()](http://download.oracle.com/javase/6/docs/api/java/lang/Comparable.html#compareTo%28T%29 "http://download.oracle.com/javase/6/docs/api/java/lang/Comparable.html#compareTo%28T%29")*,
    *[equals()](http://download.oracle.com/javase/6/docs/api/java/lang/Object.html#equals%28java.lang.Object%29 "http://download.oracle.com/javase/6/docs/api/java/lang/Object.html#equals%28java.lang.Object%29")*,
    and
    *[hashCode()](http://download.oracle.com/javase/6/docs/api/java/lang/Object.html#hashCode%28%29 "http://download.oracle.com/javase/6/docs/api/java/lang/Object.html#hashCode%28%29")*.
    Jobs that use key types with incorrect implementations of these
    functions might behave unexpectedly and return indeterministic
    results!

-   **Data formats**: Data formats generate the key-value pairs
    processed by the Pact Program and consume the result records,
    typically by reading them from files and writing them back to files.
    They represent the user-defined functionality of DataSources and
    DataSinks. For reading and writing pairs, two corresponding classes
    have to be implemented that extend
    *[eu.stratosphere.pact.common.generic.io.InputFormat](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/generic/io/InputFormat.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/generic/io/InputFormat.java")*
    and
    *[eu.stratosphere.pact.common.generic.io.OutputFormat](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/generic/io/OutputFormat.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/generic/io/OutputFormat.java")*.
      
       
     For the common case of reading text records from files and writing
    text records back, some convenience formats are provided:
    *[eu.stratosphere.pact.common.io.TextInputFormat](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/TextInputFormat.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/TextInputFormat.java")*
    and
    *[eu.stratosphere.pact.common.io.TextOutputFormat](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/TextOutputFormat.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/TextOutputFormat.java")*.
    The text input and output format work with a customizable delimiter
    character (default is '\\n').   
       
     For more details on the Input- and Output Formats, such as non-file
    data sources, binary formats, or custom partitions, please refer to
    the detail section on [Input And Output
    Formats](dataformats.html "dataformats").

\* **Stub implementations**: Your job logic goes into PACT stub
implementations. Stubs are templates for first-order user functions that
are executed in parallel. Stub implementations extend the stubs found in
*[eu.stratosphere.pact.common.stub](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/stub "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/stub")*.
You need to extend the stub that corresponds to the [Input
Contract](pactpm#input_contracts "pactpm")
for that specific user function. For example, if your function should
use a `Match` contract, than you need to extend the `MatchStub`.

\* **Plan construction**: A class that implements the interface
*[eu.stratosphere.pact.common.plan.PlanAssembler](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/plan/PlanAssembler.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/plan/PlanAssembler.java")*
provides PACT job plans. The method *getPlan(String …)* constructs the
plan of a [PACT
Job](pactpm.html "pactpm").
The plan consists of connected [Input
Contracts](pactpm#input_contracts "pactpm")
(found in
*[eu.stratosphere.pact.common.contract](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/contract "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/contract")*).
Each [Input
Contract](pactpm#input_contracts "pactpm")
has an own contract class. A contract is initialized with the
corresponding PACT stub implementation. Contracts are assembled to a
contract graph with their *setInput()*
(*setFirstInput()**/setSecondInput()*) methods. A plan
(*[eu.stratosphere.pact.common.plan.Plan](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/plan/Plan.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/plan/Plan.java")*)
is generated and initialized with all DataSinks of the assembled
contract graph. Finally, the plan is returned by the *getPlan(String …)*
method. By default, all PACTs are executed single-threaded (only one
instance is started). To run a PACT in parallel, you must specify a
degree of parallelization at the PACT contract using the method
*setDegreeOfParallelsm(int dop);*.   

### Reading Data Sources

Nephele reads its data from file systems, in the distributed setup
typically the [Hadoop Distributed Filesystem
(HDFS)](http://hadoop.apache.org/hdfs "http://hadoop.apache.org/hdfs"),
in the local setup typically the local file system. Data is fed into a
PACT program with DataSourceContracts. A DataSourceContract is
instantiated during plan construction with a path (HDFS path or local
path) to the data it will provide. The path may point to a single file
or to a directory.   
 If the data source is a directory, all (non-recursively) contained
files are read and fed into the PACT program.

### Writing Result Data

Nephele writes result data to a file system. A PACT program emits result
data with a DataSinkContract. During plan construction, a
DataSinkContract is instantiated with a HDFS path (or local path) to
which the result data is written. Result data can be written either as a
single file or as a collection of files. If the data shall be written to
a single file, the provided path must point to file.   
 **Attention**: In case the file already exists, it will be overwritten!
Result data will be written to a single file, if the DataSink task runs
with a degree-of-parallelism of one. In that case, only a single
parallel instance of the DataSink is created!   
 If the output of the result should be done in parallel, the output path
must be an existing directory or a directory is created. This is not
possible if the target path is an existing file. Each parallel instance
of the DataSink task will write a file into the output directory. The
name of the file is the number of the parallel instance of the DataSink
subtask.

### Reduce Combiners

The PACT Programming Model does also support optional combiners for
Reduce stubs.   
 Combiners are implemented by overriding the `combine()` method of the
corresponding Reduce stub
(*[eu.stratosphere.pact.common.stub.ReduceStub](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/stub/ReduceStub.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/stub/ReduceStub.java")*).
To notify the optimizer of the *combine()* implementation the Reduce
stub must be annotated with the *Combinable* annotation
(*[eu.stratosphere.pact.common.contract.ReduceContract.Combinable](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/contract/ReduceContract.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/contract/ReduceContract.java")*).
  
 **Attention:** The combiner will not be used, if the annotation is
missing. Annotating a Reduce stub although *combine()* was not
implemented may cause incorrect results, unless the combine function is
identical to the reduce function!

### User Code Annotations

[Stub
Annotations](pactpm#user_code_annotations "pactpm")
are realized as Java annotations and defined in
*[eu.stratosphere.pact.common.stubs.StubAnnotation](https://github.com/stratosphere-eu/stratosphere/tree/master/pact-common/src/main/java/eu/stratosphere/pact/common/stubs/StubAnnotation.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact-common/src/main/java/eu/stratosphere/pact/common/stubs/StubAnnotation.java")*.
  

There are two ways to attach the annotations.

1.  Attach the annotation as a class annotation directly at the stub
    class.
2.  Set the annotation at the contract object using the method
    *setStubAnnotation(…)*.

DataSources can also be annotated. Because annotations of DataSources
will mostly depend on the input data, they cannot be attached to
DataSource stub classes, i.e. *InputFormat*s. Instead, they can only be
attached to DataSource contracts which include the definition of the
input location using the *setStubAnnotation(…)* method.

### Configuration of Stubs

To improve the re-usability of stub implementations, stubs can be
parametrized, e.g. to have configurable filter values instead of
hard-coding them into the stub implementation.   

Stubs can be configured with custom parameters by overriding the method
`configure(Configuration parameters)` in your stub implementation. The
PACT framework calls *configure()* with a *Configuration* object that
holds all parameters which were passed to the stub implementation during
plan construction. To set stub parameters, use the methods
*setStubParameter(String key, String value)*, *setStubParameter(String
key, int value)*, and *setStubParameter(String key, boolean value)* of
the contract object that contains the stub class to parametrize.   
 See the `FilterO` Map stub of the [TPCHQuery3 Example
Program](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java")
to learn how to configure Stubs.

### Compiler Hints

During optimization, the compiler estimates costs for all generated
execution plans. The costs of a plan are derived from size estimations
of intermediate results. Due to the nature of black-box stub code, the
optimizer can only make rough conservative estimations. Later stages in
the plan often have no estimates at all. In this case the compiler
chooses robust default strategies that behave well for large result
sizes. The accuracy of these estimates can be significantly improved by
supplying compiler hints to the optimizer.   

Compiler hints can be given to the optimizer by setting them at the
contracts during plan construction. This is done by first fetching the
contract's
[eu.stratosphere.pact.common.contract.CompilerHints](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/contract/CompilerHints.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/contract/CompilerHints.java")
object:

    Contract.getCompilerHints()

Compiler hints can then be set on the CompilerHints object. The
following compiler hints are currently supported:

-   **Distinct Count**: Specifies the number of distinct values for a
    set of fields. Set by:

<!-- -->

    CompilerHints.setDistinctCount(FieldSet fields, long card)

Cardinality must be \> 0.

-   **Average Number of Records per Distinct Fields**: Specifies the
    average number of records that share the same values for the
    specified set of fields. Set by:

<!-- -->

    CompilerHints.setAvgNumRecordsPerDistinctFields(FieldSet fields, float avgNumRecords)

Average number of records must be \> 0.

-   **Unique Fields**: Specifies that a set of fields has unique values.
    Set by:

<!-- -->

    CompilerHints.setUniqueField(FieldSet uniqueFieldSet)

-   **Average Bytes per Record**: Specifies the average width of emitted
    records in bytes. The width is the sum of the sizes of all
    serialized fields. Set by:

<!-- -->

    CompilerHints.setAvgBytesPerRecord(float avgBytes)

Average bytes per record must be \>= 0.

-   **Average Number of Records Emitted Per Stub Call**: Specifies the
    average number of emitted records per stub call. If not set, “1.0f”
    is used as default value. Set by:

<!-- -->

    CompilerHints.setAvgRecordsEmittedPerStubCall(float avgRecordsEmittedPerStubCall)

Average records emitted per stub call must be \>= 0.

### Best Practices

This page only explains the technical details of how to write a Pact
program.   
 Have a look at [Best Practices of Pact
Programming](advancedpactprogramming.html "advancedpactprogramming")
for concrete implementation guidelines.

Package a PACT Program
----------------------

To run a PACT program on a Nephele system all required Java classes must
be available to all participating Nephele
[TaskManagers](taskmanager.html "taskmanager").
Therefore, all Java classes that are part of the PACT program (stub
implementations, data types, data formats, external classes) must be
packaged into a jar file. To make the jar file an executable PACT
program, it must also contain a class that implements the
*PlanAssembler* interface. In addition, you must register in the jar
file's manifest using the attribute *Pact-Assembler-Class*. See
[Executing Pact
Programs](executepactprogram.html "executepactprogram")
for details.
