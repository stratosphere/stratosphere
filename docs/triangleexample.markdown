---
layout: documentation
---
Triangle Enumeration
====================

The triangle enumeration PACT example program works on undirected
graphs. It identifies all triples of nodes, which are pair-wise
connected with each other, i.e., their edges form a triangle. This is a
common preprocessing step for methods that identify highly connected
subgraphs or cliques within larger graphs. Such methods are often used
for analysis of social networks.   

A MapReduce variant of this task was published by J. Cohen in “Graph
Twiddling in a MapReduce World”, Computing in Science and Engineering,
2009.

Task Description
----------------

The goal of the triangle enumeration algorithm is to identify all
triples of nodes, which are pair-wise connected with each other.   

[![](media/wiki/triangleenum_taskdescription.png)](media/wiki/triangleenum_taskdescription.png "triangleenum_taskdescription.png")

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

PACT Program
------------

The triangle enumeration example PACT program is implemented in the
following Java class:
[eu.stratosphere.pact.example.graph.EnumTriangles.java](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/graph/EnumTriangles.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/graph/EnumTriangles.java")
in the pact-examples module.

[![](media/wiki/triangleenum_pactprogram.png)](media/wiki/triangleenum_pactprogram.png "triangleenum_pactprogram.png")

1.  The triangle enumeration example PACT program supports RDF triples
    with `<http://xmlns.com/foaf/0.1/knows>` predicates as data input
    format. RDF triples with other predicates are simply filtered out.
    The triples must be separated by the line-break character (`'\n`').
    A RDF predicate is interpreted as edge between its subject and
    object. The edge is forwarded as key with the lexicographically
    smaller node being the first part of the edge and the greater node
    the second. The value is set to `NULL` because all relevant
    information is contained in the key.
2.  The Map contract projects edges to their lexicographically smaller
    node. This is done by setting the key of the output key-value-pair
    to the smaller node and forwarding the edge as value.
3.  The Match contract builds triads by combining all edges that share
    the same smaller node. Since both inputs of the Match originate from
    the same contract (Map), the Match is essentially a self-Match. A
    triad is build by combining both input edges, given that both edges
    are distinct. In order to avoid duplicate triads, the first edge
    must be lexicographically smaller than the second edge. The output
    key-value-pair is built as follows: The key is set to the missing
    edge of the triad. The lexicographically smaller node becomes the
    first part of the edge, the greater one the second part. The value
    is the complete triad (the two edges that the triad consists of).
4.  The Match contract takes all generated triads and all original edges
    as input. Both input keys are edges *(A-B), A\<B*. The key of the
    triad input is the missing edge, the key of the original edge input
    are the edges themselves. Therefore, Match closes all triads for
    which a closing edge exists in the original edge set.
5.  Finally, a DataSink contract writes out all triangles.

Program Arguments
-----------------

Three arguments must be provided to the `getPlan()` method of the
example job:

1.  `int noSubStasks`: Degree of parallelism of all tasks.
2.  `String inputRDFTriples`: Path to the input RDF triples.
3.  `String outputTriangles`: Destination path for the output of result
    triangles.

See
[here](executepactprogram.html "executepactprogram")
for details on how to specify paths for Nephele.

Test Data Sets
--------------

We provide a small RDF test data set which is a subset of a
Billion-Triple-Challenge data set
[rdfdataset.tar.gz](media/wiki/rdfdataset.tar.gz "wiki:rdfdataset.tar.gz")
(77KB, 1.8MB uncompressed). If you want do run the example with (much)
more test data, you can download a larger subset (or the whole)
Billion-Triple-Challenge data set from
[http://vmlion25.deri.ie](http://vmlion25.deri.ie/ "http://vmlion25.deri.ie/").
