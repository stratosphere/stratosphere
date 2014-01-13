---
layout: inner_docs_v04
title:  "Example Programs"
sublinks:
  - {anchor: "wordcount", title: "Word Count"}
  - {anchor: "tpchq3", title: "TPC-H Query 3"}
  - {anchor: "weblog_analysis", title: "Weblog Analysis"}
  - {anchor: "kmeans", title: "K-Means Clustering"}
  - {anchor: "triangle_enum", title: "Triangle Enumeration"}
---

## Example Programs

<p class="lead">The following example programs showcase different applications of Stratosphere from graph processing to data mining problems and relational queries.</p>

<div class="panel panel-default"><div class="panel-body">Please note that this page is based on our old documentation. There might be some differences between the text and the code. We are planning to rewrite the examples section.</div></div>

The examples consist of a task description and an in-depth discussion of the solving program. Sample data for the jobs is either provided by data set generators or links to external data sets or data set generators. The source code of all example jobs can be found in the `stratosphere-java-examples` module
([https://github.com/stratosphere/stratosphere/tree/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples](https://github.com/stratosphere/stratosphere/tree/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples)).

**Please note:** The goal of the programs is to assist you in understanding the programming model. Hence, the code often written in a more understandable way rather than in the most efficient way. 

**Overview**:

- Classic MapReduce
    - [WordCount](#wordcount)
- Relational Queries
    - [TPC-H Query 3](#tpchq3)
    - [Weblog Analysis](#weblog_analysis)
- Data Mining
    - [K-Means Iteration](#kmeans)
- Graph Analysis Algorithms
    - [Triangle Enumeration](#triangle_enum)

Please note that the *Graph Analysis Algorithms* do not use the new **Spargel** BSP API. Please see the [Spargel Documentation]({{site.baseurl}}/docs/0.4/programming_guides/spargel.html).

<section id="wordcount">
<div class="page-header"><h2>Word Count</h2></div>

Counting words is the classic example to introduce parallelized data processing with MapReduce. It demonstrates how the frequencies of words in a document collection are computed.

The source code of this example can be found
[here](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/wordcount/WordCount.java).

### Algorithm Description

The WordCount algorithm works as follows:

1.  The text input data is read line by line
2.  Each line is cleansed (removal of non-word characters, conversion to
    lower case, etc.) and tokenized by words.
3.  Each word is paired with a partial count of “1”.
4.  All pairs are grouped by the word and the total group count is
    computed by summing up partial counts.

A detailed description of the implementation of the WordCount algorithm for Hadoop MapReduce can be found at [Hadoop's Map/Reduce Tutorial](http://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html#Example%3A+WordCount+v1.0).

### Stratosphere Program

The [wordcount example](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/wordcount/WordCount.java) implements the above described algorithm.

1.  The program starts with an input data source. The
    `FileDataSource` produces a list strings. Each string represents a line of
    the input file.
2.  The Map operator is used to split each line into single words. The user
    code tokenizes the line by whitespaces (`' '`, `'\t`', ...) into
    single words. Each word is emitted in an independent record which represents a key-value pair,
    where the key (field 0) is the word itself and the value (the second field) is an Integer "1".
3.  The Reduce operator implements the reduce and combine methods. The
    ReduceFunction is annotated with `@Combinable` to use the combine
    method. The combine method computes partial sums over the "1"-values
    emitted by the Map operator, the reduce method sums these partial
    sums and obtains the final count for each word.
4.  The final DataSink operator writes the results back.

### Program Arguments

Three arguments must be provided to the `getPlan()` method of the
example job:

1.  `int noSubStasks`: Degree of parallelism of all tasks.
2.  `String inputPath`: Path to the input data.
3.  `String outputPath`: Destination path for the result.

### Test Data

Any plain text file can be used as input for the the WordCount example.
If you do not have a text file at hand, you can download some literature
from the [Gutenberg Project](http://www.gutenberg.org). For
example

    wget -O ~/hamlet.txt http://www.gutenberg.org/cache/epub/1787/pg1787.txt

will download Hamlet and put the text into a file called `hamlet.txt` in your current directory.
</section>

<section id="tpchq3">
<div class="page-header"><h2>TPCH - Query 3</h2></div>

The source code of this example can be found [here](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/relational/TPCHQuery3.java).

### Task Description

The TPC-H benchmark is a decision support benchmark on relational data
(see http://[www.tpc.org/tpch/](http://www.tpc.org/tpch/)).
The example below shows a Stratosphere program realizing a modified version of
Query 3 from TPC-H including one join, some filtering and an
aggregation. The original query contains another join with the
*Customer* relation, which we omitted to reduce the size of the example.
The picture below shows the schema of the data to be analyzed and the
corresponding SQL query.

<p class="text-center"><img src="{{site.baseurl}}/docs/0.4/media/wiki/tpch3_taskdescription.2.png"></p>

### Stratosphere Program

The example program implements the SQL Query given above in the Java class [eu.stratosphere.example.java.record.relational.TPCHQuery3](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/relational/TPCHQuery3.java) in the `stratosphere-java-examples` module.

1.  The program starts with two data sources, one for the *Orders*
    relation and one for the *Lineitem* relation. 
2.  Each data source is followed by a Map operator:
3.  Orders: Filtering and projection is done for the orders table in the
    map step.
4.  LineItem: Columns that are not needed are projected out in the map
    step. (removing or projecting out columns is not required. It is a performance optimization.)
5.  The join is realized in a JoinOperator. The join condition
    (i.e., equality of orderkey attributes) is ensured by *Join*.
    Hence, the user code only concatenates the attributes of both records. The
    new key is a super key of the input key.
6.  The *Reduce* step realizes the grouping by orderkey and shippriority
    and calculates the sum on the extended price. Partial sums are
    calculated in the combine step.
7.  The result is written back to a data sink.

### Program Arguments

For this example job, four arguments must be provided to the `getPlan()`
method. Namely:

1.  `int noSubStasks`: Degree of parallelism for all tasks.
2.  `String orders`: Path to the *Orders* relation.
3.  `String lineitem`: Path to the *Lineitem* relation.
4.  `String outputPath`: Destination path for the result output.


### Data Generation

The TPC-H benchmark suite provides a data generator tool (DBGEN) for
generating sample input data (see [http://www.tpc.org/tpch/](http://www.tpc.org/tpch/)).
To use it together with Stratosphere, take the following steps:

1.  Download and unpack DBGEN
2.  Make a copy of *makefile.suite* called *Makefile* and perform the
    following changes:

```bash
# The Stratosphere program was tested with DB2 data format
DATABASE = DB2
MACHINE  = LINUX
WORKLOAD = TPCH

# according to your compiler, mostly gcc
CC       = gcc
```

1.  Build DBGEN using *make*
2.  Generate lineitem and orders relations using dbgen. A scale factor
    (-s) of 1 results in a generated data set with about 1 GB size.

```
./dbgen -T o -s 1
```

</section>

<section id="weblog_analysis">

<div class="page-header"><h2>Weblog Analysis</h2></div>

### Task Description

The Weblog Analysis example shows how analyzing relational data can be
done with the help of the Stratosphere Programming Model.

In this case a scenario is considered where the log files for different
webpages are analyzed. These log files contain information about the
page ranks and the page visits of the different webpages. The picture
below gives the schema of the the data to be analyzed and the
corresponding SQL query. The data is split into three relations:

-   Docs (URL, content)
-   Ranks (rank, URL, average duration)
-   Visits (IP address, URL, date, advertising revenue, …)

The analysis task computes the ranking information of all documents that
contain a certain set of keywords, have at least a certain rank, and
have not been visited in a certain year.

<p class="text-center"><img src="{{site.baseurl}}/docs/0.4/media/wiki/weblog_taskdescription.png"></p>

### Stratosphere Program

The weblog analysis example program is implemented in the following Java class:
[WebLogAnalysis.java](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/relational/WebLogAnalysis.java?source=cc)
in the `stratosphere-java-examples` module.

1.  Each relation is read by a separate DataSource operator. Each line
    is converted into a key-value-pair.
2.  The local predicates on each relation are applied by a Map operator.
3.  The Match operator performs the equality join between the docs and
    ranks relations.
4.  The anti join between the (docs JOIN ranks) and visits relations is
    performed by the CoGroup operator. Tuples of the ranks relation are
    forwarded if no tuple of the visits relation has the same key (url).
5.  The result is written by the final DataSink operator.

### Program Arguments

Five arguments must be provided to the `getPlan()` method of the example job:

1.  `int noSubStasks`: Degree of parallelism for all tasks.
2.  `String docs`: Path to the docs relation.
3.  `String ranks`: Path to the ranks relation.
4.  `String visits`: Path to the visits relation.
5.  `String outputPath`: Destination path for the result.

### Data Generator

There are two generators which can provide data for the web-log analysis
Stratosphere example.

1.  A stand-alone generator for smaller data sets.
2.  A distributed generator for larger data sets.

Both generators produce identically structured test data.

### Stand-Alone Generator

We provide a data set generator to generate the docs, ranks, and visits
relations. The generator is implemented as Java class [eu.stratosphere.example.java.record.relational.generator.WebLogGenerator.java](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/relational/generator/WebLogGenerator.java?source=cc)
in the `stratosphere-java-examples` module.

The parameters of the main method of the generator are:

-   noDocuments: Number of generated doc and rank records.
-   noVisits: Number of generated visit records.
-   outPath: Path the where generated files are written.
-   noFiles: Number of files into which all relations are split.

The data generated by our stand-alone generator follows only the schema
of the distributed generator. Attribute values, distributions, and
correlations are not the same.   

Please consult the in-line JavaDocs for further information on the
generator.

### Distributed Generator

For generating larger data sets in a distributed environment, you can use a generator provided by [Brown University](http://database.cs.brown.edu/projects/mapreduce-vs-dbms/ "http://database.cs.brown.edu/projects/mapreduce-vs-dbms/") (see Section *Analysis Benchmarks Data Sets*).
</section>

<section id="kmeans">
<div class="page-header"><h2>K-Means Iteration</h2></div>

The source code of this example can be found
[here](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/kmeans/KMeansSingleStep.java).

### Task description

The k-means cluster algorithm is a well-known algorithm to group data
points in to clusters of similar points. In contrast to hierarchical
cluster algorithms, the number of cluster centers has to be provided to
the algorithm before it is started. K-means is an iterative algorithm,
which means that certain steps are repeatably performed until a
termination criterion is fulfilled.

The algorithm operates in five steps (see figure below):

1.  Starting point: A set of data points which shall be clustered is
    provided. A data point is a vector of features. A set of initial
    cluster centers is required as well. Cluster centers are feature
    vectors as well. The initial centers are randomly generated, fixed,
    or randomly picked from the set of data points. Finally, a distance
    metric that computes the distance between a data point and a cluster
    center is required.
2.  Compute Distances: An iteration starts with computing the distances
    of all data points to all cluster centers.
3.  Find Nearest Cluster: Each data point is assigned to the cluster
    center to which it is closest according to the computed distance.
4.  Recompute Center Positions: Each cluster center is moved to the
    center of all data points which were assigned to it.
5.  Check Termination Criterion: There are multiple different
    termination criteria such as a fixed number of iterations, a limit
    on the average distance the cluster centers have moved, or a
    combination of both. If the termination criteria was met, the
    algorithm terminates. Otherwise, it goes back to step 2 and uses the
    new cluster centers as input.

<p class="text-center"><img width="95%" src="{{site.baseurl}}/docs/0.4/media/wiki/k-means_taskdescription.png"></p>

A detailed discussion of the K-Means clustering algorithm can be found
here: [http://en.wikipedia.org/wiki/K-means\_clustering](http://en.wikipedia.org/wiki/K-means_clustering)

### Stratosphere Program

The example program implements one iteration step (steps 2,3, and
4) of the k-means algorithm. The implementation resides in the following
Java class: [eu.stratosphere.example.java.record.kmeans.KMeansSingleStep.java](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/kmeans/KMeansSingleStep.java)
in the `stratosphere-java-examples` module. All required classes (data types, data
formats, functions, etc.) are contained in this class as static inline
classes.

The program that implements the k-means iteration is shown in the
figure below. The implementation follows the same steps as presented
above. We discuss the program top-down in "flow direction" of the data.

-   It starts at two data sources (DataPoints and ClusterCenters) at
    the top. Both sources produce key-value pairs from their input files
    (stored in HDFS) where the ID is the key and the coordinates of the
    data point or cluster center is the value.   

Since it is assumed, that IDs are unique in both files, both data
sources are annotated with `UniqueKey` OutputOperators.

-   A Cross operator is used to compute the distances of all data points to
    all cluster centers. The user code (highlighted as leaf green box)
    implements the distance computation of a single data point to a
    single cluster center. The Cross operator takes care of enumerating all
    combinations of data points and cluster centers and calls the user
    code with each combination. The user code emits key-value pairs
    where the ID of the data point is the key (hence the `SameKeyLeft`
    OutputOperator is attached). The value consists of a record that
    contains the coordinates of the data point, the ID of the cluster
    center, and the computed distance.

-   We identify the cluster center that is closest to each data point
    with a Reduce operator. The output key-value pairs of the Cross operator
    have the ID of the data points as key. Hence, all records (with
    distance and cluster center ID) that belong to a data point are
    grouped together. These records are handed to the user code where
    the record with the minimal distance is identified. This is a
    minimum aggregation and offers the potential use of a Combiner which
    performs partial aggregations. The key-value pairs that are
    generated by the user code have the ID of the closest cluster center
    as key and the coordinates of the data point as value.

-   The computation of the new cluster center positions is done with
    another Reduce operator. Since its input has cluster center IDs as keys,
    all pairs that belong to the same center are grouped and handed to
    the user code. Here the average of all data point coordinates is
    computed. Again this average computation can be improved by
    providing a partially aggregating Combiner that computes a count and
    a coordinate sum. The user code emits for each cluster center a
    single record that contains its ID and its new position.

-   Finally, the DataSink (new ClusterCenters) writes the results back
    into the HDFS.

As already mentioned, the program only covers steps 2, 3, and 4.
The evaluation of the termination criterion and the iterative call of
the program can be done by an external control program.

### Program Arguments

Four arguments must be provided to the `getPlan()` method of the example
job:

1.  `int noSubStasks`: Degree of parallelism of all tasks.
2.  `String dataPoints`: Path to the input data points.
3.  `String clusterCenters`: Path to the input cluster centers.
4.  `String outputPath`: Destination path for the result.

### Data Generator

We provide a data set generator to generate data points and cluster
centers input files. The generator is implemented as Java class:
[KMeansSampleDataGenerator](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/kmeans/KMeansSampleDataGenerator.java).   

The parameters of the main method of the generator are:

-   numberOfDataPoints: The amount of data points to be generated.
-   numberOfDataPointFiles: The amount of files in which the data points
    are written.
-   numberOfClusterCenters: The amount of cluster centers to be
    generated.
-   outputDir: The directory where the output files are generated.

Please consult the in-line JavaDocs for further information on the generator.
</section>

<section id="triangle_enum">
<div class="page-header"><h2>Triangle Enumeration</h2></div>

The triangle enumeration example program works on undirected
graphs. It identifies all triples of nodes, which are pair-wise
connected with each other, i.e., their edges form a triangle. This is a
common preprocessing step for methods that identify highly connected
subgraphs or cliques within larger graphs. Such methods are often used
for analysis of social networks.   

A MapReduce variant of this task was published by J. Cohen in “Graph
Twiddling in a MapReduce World”, Computing in Science and Engineering,
2009.

### Task Description

The goal of the triangle enumeration algorithm is to identify all
triples of nodes, which are pair-wise connected with each other.   

<p class="text-center"><img src="{{site.baseurl}}/docs/0.4/media/wiki/triangleenum_taskdescription.png"></p>

The figure above shows how the algorithm works to achieve that:

1.  The algorithms receives as input the edge set of an undirected
    graph.
2.  All edges are projected to the lexicographically smaller node of
    both incident nodes.
3.  Edges that are projected to the same (smaller) node are combined to
    a triad (an open triangle). Note: The algorithm does only find all
    triads which are possible candidates for triangles but not all
    triads of the graph. See for example the triad *(1-2, 2-3)*. It is
    not found by the algorithm because *2* is the smallest node in
    (2-3), but not in (1-2).
4.  For each triad, the algorithm looks for an edge to close the triad
    and form a triangle. In the example the only triangle *(5-6, 5-7,
    6-7)* is found.

### Stratosphere Program

The triangle enumeration example program is implemented in the
following Java class: [EnumTrianglesRdfFoaf.java](https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/triangles/EnumTrianglesRdfFoaf.java?source=cc)
in the `stratosphere-java-examples` module.

1.  The triangle enumeration example program supports RDF triples
    with `<http://xmlns.com/foaf/0.1/knows>` predicates as data input
    format. RDF triples with other predicates are simply filtered out.
    The triples must be separated by the line-break character (`'\n`').
    A RDF predicate is interpreted as edge between its subject and
    object. The edge is forwarded as key with the lexicographically
    smaller node being the first part of the edge and the greater node
    the second. The value is set to `NULL` because all relevant
    information is contained in the key.
2.  The Map operator projects edges to their lexicographically smaller
    node. This is done by setting the key of the output key-value-pair
    to the smaller node and forwarding the edge as value.
3.  The Match operator builds triads by combining all edges that share
    the same smaller node. Since both inputs of the Match originate from
    the same operator (Map), the Match is essentially a self-Match. A
    triad is build by combining both input edges, given that both edges
    are distinct. In order to avoid duplicate triads, the first edge
    must be lexicographically smaller than the second edge. The output
    key-value-pair is built as follows: The key is set to the missing
    edge of the triad. The lexicographically smaller node becomes the
    first part of the edge, the greater one the second part. The value
    is the complete triad (the two edges that the triad consists of).
4.  The Match operator takes all generated triads and all original edges
    as input. Both input keys are edges *(A-B), A\<B*. The key of the
    triad input is the missing edge, the key of the original edge input
    are the edges themselves. Therefore, Match closes all triads for
    which a closing edge exists in the original edge set.
5.  Finally, a DataSink operator writes out all triangles.

### Program Arguments

Three arguments must be provided to the `getPlan()` method of the
example job:

1.  `int noSubStasks`: Degree of parallelism of all tasks.
2.  `String inputRDFTriples`: Path to the input RDF triples.
3.  `String outputTriangles`: Destination path for the output of result
    triangles.

### Test Data Sets

We provide a small RDF test data set which is a subset of a Billion-Triple-Challenge data set [RDFDataSet.tar.gz]({{site.baseurl}}/docs/0.4/media/wiki/rdfdataset.tar.gz) (77KB, 1.8MB uncompressed). If you want to run the example with (a lot) more test data, you can download a larger subset (or the whole) Billion-Triple-Challenge data set from [http://km.aifb.kit.edu/projects/btc-2009/](http://km.aifb.kit.edu/projects/btc-2009/).
</section>