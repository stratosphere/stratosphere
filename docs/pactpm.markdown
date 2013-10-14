---
layout: documentation
---
The PACT Programming Model
==========================

PACT and MapReduce
------------------

The PACT programming model is a generalization of the [MapReduce](http://research.google.com/archive/mapreduce.html)
programming model.
In the following, we briefly introduce the MapReduce programming model
and highlight the main differences to the PACT programming model.   

### MapReduce Programming Model

The goal of the MapReduce programming model is to ease the development
of parallel data processing tasks by hiding the complexity of writing
parallel and fault-tolerant code. To specify a MapReduce job, a
developer needs to implement only two first-order functions. When a
MapReduce job is executed by a MapReduce framework, these user functions
are handed to the second-order functions `map()` and `reduce()`. The
framework takes care of executing the job in a distributed and
fault-tolerant way.

The user functions can be of arbitrary complexity. Both operate on
independent subsets of the input data which are build by the
second-order functions (SOFs) `map()` and `reduce()` (Data Parallelism).
MapReduce's data model is based on pairs of keys and values. Both, key
and value can be of any complex type and are interpreted only by the
user code and not by the execution framework.   

### PACT Programming Model

The PACT programming model is based on the concept of Parallelization
Contracts (PACTs). Similar to MapReduce, arbitrary user code is handed
and executed by PACTs. However, PACT generalizes a couple of MapReduce's
concepts:

1.  Second-order Functions: PACT provides more second-order functions.
    Currently, five SOF called Input Contracts are supported. This set
    might be extended in the future.
2.  Program structure: PACT allows the composition of arbitrary acyclic
    data flow graphs. In contract, MapReduce programs have a static
    structure (Map → Reduce).
3.  Data Model: PACT's data model are records of arbitrary many fields
    of arbitrary types. MapReduce's KeyValue-Pairs can be considered as
    records with two fields.

In the following, the concept of Parallelization Contracts is discussed
and how they are composed to PACT programs.

What is a PACT
--------------

Parallelization Contracts (PACTs) are data processing operators in a
data flow. Therefore, a PACT has one or more data inputs and one or more
outputs. A PACT consists of two components:

-   Input Contract
-   User function
-   User code annotations

The figure below shows how those components work together. Input
Contracts split the input data into independently processable subset.
The user code is called for each of these independent subsets. All calls
can be executed in parallel, because the subsets are independent.

Optionally, the user code can be annotated with additional information.
These annotations disclose some information on the behavior of the
black-box user function. The [PACT
Compiler](pactcompiler.html "pactcompiler")
can utilize the information to obtain more efficient execution plans.
However, while a missing annotation will not change the result of the
execution, an incorrect Output Contract produces wrong results.

![](media/wiki/pact.png)

The currently supported Input Contracts and annotation are presented and
discussed in the following.

### Input Contracts

Input Contracts split the input data of a PACT into independently
processable subsets that are handed to the user function of the PACT.
Input Contracts vary in the number of data inputs and the way how
independent subsets are generated.

More formally, Input Contracts are second-order functions with a
first-order function (the user code), one or more input sets, and none
or more key fields per input as parameters. The first-order function is
called (one or) multiple times with subsets of the input set(s). Since
the first-order functions have no side effects, each call is independent
from each other and all calls can be done in parallel.

The second-order functions `map()` and `reduce()` of the MapReduce
programming model are Input Contracts in the context of the PACT
programming model.

##### MAP

The Map Input Contract works in the same way as in MapReduce. It has a
single input and assigns each input record to its own subset. Hence, all
records are processed independently from each other (see figure below).
  

![](media/wiki/map.png)

##### REDUCE

The Reduce Input Contract has the same semantics as the reduce function
in MapReduce. It has a single input and groups together all records that
have identical key fields. Each of these groups is handed as a whole to
the user code and processed by it (see figure below). The PACT
Programming Model does also support optional Combiners, e.g. for partial
aggregations.   

![](media/wiki/reduce.png)

##### CROSS

The Cross Input Contract works on two inputs. It builds the Cartesian
product of the records of both inputs. Each element of the Cartesian
product (pair of records) is handed to the user code.   

![](media/wiki/cross.png)

##### MATCH

The Match Input Contract works on two inputs. From both inputs it
matches those records that are identical on their key fields come from
different inputs. Hence, it resembles an equality join where the keys of
both inputs are the attributes to join on. Each matched pair of records
is handed to the user code.   

![](media/wiki/match.png)

##### COGROUP

The CoGroup Input Contract works on two inputs as well. It can be seen
as a Reduce on two inputs. On each input, the records are grouped by key
(such as Reduce does) and handed to the user code. In contrast to Match,
the user code is also called for a key if only one input has a pair with
it (see blue key in example below).

![](media/wiki/cogroup.png)

Pact Record Data Model
----------------------

In contrast to MapReduce, PACT uses a more generic data model of records
([Pact
Record](pactrecord.html "pactrecord"))
to pass data between functions. The Pact Record can be thought of as a
tuple with a free schema. The interpretation of the fields of a record
is up to the user function. A Key/Value pair (as in MapReduce) is a
special case of that record with only two fields (the key and the
value).

For input contracts that operate on keys (like *Reduce*, *Match*, or
*CoGroup*, one specifies which combination of the record's fields make
up the key. An arbitrary combination of fields may used. See the [TPCH
Query
Exampe](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java")
on how programs defining *Reduce* and *Match* contracts on one or more
fields and can be written to minimally move data between fields.

The record may be sparsely filled, i.e. it may have fields that have
*null* values. It is legal to produce a record where for example only
fields 2 and 5 are set. Fields 1, 3, 4 are interpreted to be *null*.
Fields that are used by a contract as key fields may however not be
null, or an exception is raised.

User code annotations
---------------------

User code annotation are optional in the PACT programming model. They
allow the developer to make certain behaviors of her/his user code
explicit to the optimizer. The PACT optimizer can utilize that
information to obtain more efficient execution plans. However, it will
not impact the correctness of the result if a valid annotation was not
attached to the user code. On the other hand, invalidly specified
annotations might cause the computation of wrong results. In the
following, we list the current set of available Output Contracts.

##### Constant Fields

The **Constant Fields** annotation marks fields that are not modified by
the user code function. Note that for every input record a constant
field may not change its content and position in any output record! In
case of binary second-order functions such as Cross, Match, and CoGroup,
the user can specify one annotation per input.

##### Constant Fields Except

The **Constant Fields Except** annotation is inverse to the **Constant
Fields** annotation. It annotates all fields which might be modified by
the annotated user-function, hence the optimizer considers **any not
annotated field as constant**. This annotation should be used very
carefully! Again, for binary second-order functions (Cross, Match,
CoGroup), one annotation per input can be defined. Note that either the
Constant Fields or the Constant Fields Except annotation may be used for
an input.

PACT Programs
-------------

PACT programs are constructed as data flow graphs that consist of data
sources, PACTs, and data sinks. One or more data sources read files that
contain the input data and generate records from those files. Those
records are processed by one or more PACTs, each consisting of an Input
Contract, user code, and optional code annotations. Finally, the results
are written back to output files by one or more data sinks. In contrast
to the MapReduce programming model, a PACT program can be arbitrary
complex and has no fixed structure.   

The figure below shows a PACT program with two data sources, four PACTs,
and one data sink. Each data source reads data from a specified location
in the file system. Both sources forward the data to respective PACTs
with Map Input Contracts. The user code is not shown in the figure. The
output of both Map PACTs streams into a PACT with a Match Input
Contract. The last PACT has a Reduce Input Contract and forwards its
result to the data sink.

![](media/wiki/pactprogram.png)

Advantages of PACT over MapReduce
---------------------------------

1.  The PACT programming model encourages a more modular programming
    style. Although the number of user functions is usually higher, they
    are more fine-grain and focus on specific problems. Hence,
    interweaving of functionality which is common for MapReduce jobs can
    be avoided.
2.  Data analysis tasks can be expressed as straight-forward data flows,
    especially when multiple inputs are required.
3.  PACT has a record-based data model, which reduces the need to
    specify custom data types as not all data items need to be packed
    into a single value type.
4.  PACT frequently eradicates the need for auxiliary structures, such
    as the distributed cache, which “break” the parallel programming
    model.
5.  Data organization operations such as building a Cartesian product or
    combining records with equal keys are done by the runtime system. In
    MapReduce such often needed functionality must be provided by the
    developer of the user code.
6.  PACTs specify data parallelization in a declarative way which leaves
    several degrees of freedom to the system. These degrees of freedom
    are an important prerequisite for automatic optimization. The [PACT
    compiler](pactcompiler.html "pactcompiler")
    enumerate different execution strategies and chooses the strategy
    with the least estimated amount of data to ship. In contrast, Hadoop
    executes MapReduce jobs always with the same strategy.

For a more detailed comparison of the MapReduce and PACT programming
models you can read our paper *“MapReduce and PACT - Comparing Data
Parallel Programming Models”* (see our [publications
page](http://www.stratosphere.eu/publications "http://www.stratosphere.eu/publications")).
