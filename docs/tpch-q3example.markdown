---
layout: documentation
---
TPCH - Query 3
==============

The source code of this example can be found
[here](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java").

Task Description
----------------

The TPC-H benchmark is a decision support benchmark on relational data
(see
http://[www.tpc.org/tpch/](http://www.tpc.org/tpch/ "http://www.tpc.org/tpch/")).
The example below shows a PACT program realizing a modified version of
Query 3 from TPC-H including one join, some filtering and an
aggregation. The original query contains another join with the
*Customer* relation, which we omitted to reduce the size of the example.
The picture below shows the schema of the data to be analyzed and the
corresponding SQL query.

[![](media/wiki/tpch3_taskdescription.2.png)](/Users/asteriosk/Downloads/dokuwiki/bin/lib/exe/detail.php?id=&media=tpch3_taskdescription.2.png "tpch3_taskdescription.2.png")

PACT Program
------------

The example PACT program implements the SQL Query given above in the
Java class
[eu.stratosphere.pact.example.relational.TPCHQuery3](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/relational/TPCHQuery3.java")
in the pact-examples module.

[![](media/wiki/tpch3_pactprogram.2.png)](media/wiki/tpch3_pactprogram.2.png "tpch3_pactprogram.2.png")

1.  The program starts with two data sources, one for the *Orders*
    relation and one for the *Lineitem* relation. The input format for
    both data sources uses the first attribute as key. For orders the
    first attribute is the primary key of the relation. Therefore, the
    key of each key/value-pair is unique and a *!UniqueKey* Output
    Contract is attached to the orders data source.
2.  Each data source is followed by a Map contract:
3.  Orders: Filtering and projection is done for the orders table in the
    map step. Since the key is not changed, a *!SameKey* Output Contract
    is attached.
4.  LineItem: Columns that are not needed are projected out in the map
    step. Since this map step does not alter the key,a *!SameKey* Output
    Contract is attached.
5.  The join is realized in a *Match* contract. The join condition
    (i.e., equality of orderkey attributes) is ensured by *Match*.
    Hence, the user code only concatenates the attributes of both tuples
    and builds a new key by concatenating orderkey and shippriority. The
    new key is a super key of the input key. Therefore, a *!SuperKey*
    Output Contract is attached.
6.  The *Reduce* step realizes the grouping by orderkey and shippriority
    and calculates the sum on the extended price. Partial sums are
    calculated in the combine step.
7.  The result is written back to a data sink.

Program Arguments
-----------------

For this example job, four arguments must be provided to the `getPlan()`
method. Namely:

1.  `int noSubStasks`: Degree of parallelism for all tasks.
2.  `String orders`: Path to the *Orders* relation.
3.  `String lineitem`: Path to the *Lineitem* relation.
4.  `String outputPath`: Destination path for the result output.

See *!ExecutePactProgram* or details on how to specify paths for
Nephele.

Data Generation
---------------

The TPC-H benchmark suite provides a data generator tool (DBGEN) for
generating sample input data (see
[http://www.tpc.org/tpch/](http://www.tpc.org/tpch/ "http://www.tpc.org/tpch/")).
To use it together with PACT, take the following steps:

1.  Download and unpack DBGEN
2.  Make a copy of *makefile.suite* called *Makefile* and perform the
    following changes:

<!-- -->

    #PACT program was tested with DB2 data format
    DATABASE = DB2
    MACHINE  = LINUX
    WORKLOAD = TPCH

    #according to your compiler, mostly gcc
    CC       = gcc
     

1.  Build DBGEN using *make*
2.  Generate lineitem and orders relations using dbgen. A scale factor
    (-s) of 1 results in a generated data set with about 1 GB size.

<!-- -->

    ./dbgen -T o -s 1
     
