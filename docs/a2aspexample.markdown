---
layout: documentation
---
---
layout: inner_complex
---


Pairwise Shortest Paths
=======================

Known as Floyd-Warshall Algorithm, the following graph analysis
algorithm finds the shortest path between all pairs of vertices in a
weighted graph.

A detailed description of the Floyd-Warshall algorithms can be found on
[wikipedia](http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm "http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm").

Algorithm description
---------------------

Consider a directed graph G(V,E), where V is a set of vertices and E is
a set of edges. The algorithm takes the adjacency matrix D of G and
compares all possible path combinations between every two vertices in
the graph G.

    (Pseudocode)
        for m := 1 to n
           for i := 1 to n
              for j := 1 to n
                 D[[i]][[j]] = min ( D[[i]][[j]], D[[i]][[m]]+D[[m]][[j]] );

[![](media/wiki//allpairs.png)](media/wiki/allpairs.png "wiki:allpairs.png")

Iterative approach
------------------

In order to parallelize the shortest path algorithm, the following steps
will be iteratively performed:

Assuming that I(k) is the set of the shortest paths in the k-th
iteration.

1.  Generate two key/value sets from the I(k) set:
    -   S - the set of the source vertices of all paths from I(k)
    -   T - the set of the target vertices of all paths from I(k)

2.  Perform an equi-join on the two sets of pairs:
    -   J - the set, consisting of paths, constructed from T and S,
        where T-th element (path's target) equals the S-th element
        (path's source)

3.  Union this J joined set with the intermediate result of the previous
    iteration I(k):
    -   U = J+I(k)

4.  For all pairwise distances from U set, only the shortest will remain
    in the I(k+1) set:
    -   dist(i,j) = min { dist(i,j) | dist(i,m)+dist(m,j) }.

[![](media/wiki/all2all_sp_taskdescription.png)](media/wiki/all2all_sp_taskdescription.png "wiki:all2all_sp_taskdescription.png")

PACT Program
------------

The example PACT program implements one iteration step of the all pairs
shortest path algorithm.   
 The implementation resides in the following Java class:
[eu.stratosphere.pact.example.graph.PairwiseSP.java](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/graph/PairwiseSP.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/graph/PairwiseSP.java")
in the pact-examples module.

[![](media/wiki/all2all_sp_pactprogram.png)](media/wiki/all2all_sp_pactprogram.png "wiki:all2all_sp_pactprogram.png")

1.  The PACT program supports two data input formats, namely RDF triples
    with foaf:knows predicates and our custom path format. The single
    DataSourceContract of the program is configured with the appropriate
    InputFormat during plan construction via a parameter. From both
    input formats, key-value-pairs of the same type are generated. The
    keys consist of the source and target vertices of the paths. Values
    is the detailed path information: the source vertex, the target
    vertex, the path length and a list of all intermediate vertices
    (hops) between the source and target vertices.
2.  The following two Map contracts project the input paths on their
    source and target vertices by setting the keys. The values are not
    changed and simply forwarded.
3.  The outputs of both Map contracts are fed into the following Match
    contract. The Match concatenates all two paths if the start vertex
    of one path is the end vertex of the other one. The key of the
    output key-value pair is built from the newly constructed paths'
    from- and to-vertex. The value is the detailed information of the
    new path including the updated length and hop-list.
4.  The CoGroup contract operates on the output set of the Match
    (recently combined paths) and the original (incoming) paths. The
    emitted set contains the shortest path (with minimal length) between
    each pair of from-vertex and to-vertex.
5.  Finally, a DataSink contract writes the data into the HDFS for the
    next iteration.

Program Arguments
-----------------

Four arguments must be provided to the `getPlan()` method of the example
job:

1.  `int noSubStasks`: Degree of parallelism of all tasks.
2.  `String inputPaths`: Path to the input paths.
3.  `String outputPaths`: Destination path for the result paths.
4.  `boolean RDFInputFlag`: Input format flag. If set to true, RDF input
    must be provided. Otherwise, the custom path format is required.

See
[here](executepactprogram.html "executepactprogram")
for details on how to specify paths for Nephele.

Test Data Sets
--------------

We provide a small RDF test data set which is a subset of a
Billion-Triple-Challenge data set
[RDFDataSet.tar.gz](media/wiki/rdfdataset.tar.gz "wiki:rdfdataset.tar.gz")
(77KB, 1.8MB uncompressed). If you want to run the example with (a lot)
more test data, you can download a larger subset (or the whole)
Billion-Triple-Challenge data set from
[http://km.aifb.kit.edu/projects/btc-2009/](http://km.aifb.kit.edu/projects/btc-2009/ "http://km.aifb.kit.edu/projects/btc-2009/").
