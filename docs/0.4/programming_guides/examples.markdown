---
layout: inner_docs_v04
title:  "Example Programs"
sublinks:
  - {anchor: "wordcount", title: "Word Count"}
  - {anchor: "page_rank", title: "Page Rank"}
  - {anchor: "connected_components", title: "Connected Components"}
  - {anchor: "relational", title: "Relational Query"}
  - {anchor: "test_data", title: "Test Data"}
---

## Example Programs

<p class="lead">
The following example programs showcase different applications of Stratosphere from simple word counting to graph algorithms. The code samples are intended to illustrate the principles of writing Stratosphere programs. The full source code of all examples can be found in the **[stratosphere-java-examples](https://github.com/stratosphere/stratosphere/tree/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples)** and **[stratosphere-scala-examples](https://github.com/stratosphere/stratosphere/tree/release-{{site.current_stable}}/stratosphere-examples/stratosphere-scala-examples)** modules.

<section id="wordcount">
<div class="page-header"><h2>Word Count</h2></div>

Counting words in a collection of text documents is a simple two step algorithm: First, the texts are tokenized to individual words (optionally stemming/normalizing the words). Second, the collection of words is grouped and counted.

<ul class="nav nav-tabs">
    <li class="active"><a href="#wordcount_scala" data-toggle="tab">Scala</a></li>
    <li><a href="#wordcount_java" data-toggle="tab">Java</a></li>
</ul>
<div class="tab-content">
    <div class="tab-pane active" id="wordcount_scala">
{% highlight scala %}
val input = TextFile(textInput)

val words = input.flatMap { _.split(" ") map { (_, 1) } }

val counts = words.groupBy { case (word, _) => word }
  .reduce { (w1, w2) => (w1._1, w1._2 + w2._2) }

val output = counts.write(wordsOutput, CsvOutputFormat()))
{% endhighlight %}

The <a href="https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-scala-examples/src/main/scala/eu/stratosphere/examples/scala/wordcount/WordCount.scala">WordCount example (Scala)</a> implements the above described algorithm with input parameters <em>degree of parallelism</em>, <em>input path</em>, and <em>output path</em>. As test data, any text file will do.
    </div>
    <div class="tab-pane" id="wordcount_java">
{% highlight java %}
// --- tokenizer function ---
public class TokenizeLine extends MapFunction {
    public void map(Record record, Collector<Record> collector) {
        // get the first field (as type StringValue) from the record
        String line = record.getField(0, StringValue.class).getValue();

        // split and emit (word, 1) pairs
        for (String word : line.split(" ")) {
            collector.collect(new Record(new StringValue(word), new IntValue(1)));
        }
    }
}

// --- counting function ---
@Combinable
public static class CountWords extends ReduceFunction {

    public void reduce(Iterator<Record> records, Collector<Record> out) throws Exception {
        Record element = null;
        int sum = 0;
        while (records.hasNext()) {
            element = records.next();
            int cnt = element.getField(1, IntValue.class).getValue();
            sum += cnt;
        }

        element.setField(1, new IntValue(sum));
        out.collect(element);
    }
}

// --- program assembly ---

FileDataSource source = new FileDataSource(new TextInputFormat(), inputPath, "Input Lines");

MapOperator mapper = MapOperator.builder(TokenizeLine.class)
                    .input(source).build();

ReduceOperator reducer = ReduceOperator.builder(CountWords.class, 
                    StringValue.class, 0)    // group on field 0 which is a string
                    .input(mapper).build();

FileDataSink out = new FileDataSink(new CsvOutputFormat(), outputPath, reducer);
{% endhighlight %}

The <a href="https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/wordcount/WordCount.java">WordCount example (Java)</a> implements the above described algorithm with input parameters <em>degree of parallelism</em>, <em>input path</em>, and <em>output path</em>. As test data, any text file will do.
    </div>
</div>

</section>


<section id="page_rank">
<div class="page-header"><h2>Page Rank</h2></div>

PageRank is an iterative algorithm, which means that it applies the same computation repeatedly. Each time, all pages distribute their current rank over their neighbors, and compute a new rank as a taxed sum of those ranks they received from the neighbors.

<ul class="nav nav-tabs">
    <li class="active"><a href="#pagerank_scala" data-toggle="tab">Scala</a></li>
</ul>
<div class="tab-content">
    <div class="tab-pane active" id="pagerank_scala">
{% highlight scala %}
// cases classes so we have named fields
case class PageWithRank(pageId: Long, rank: Double)
case class Edge(from: Long, to: Long, transitionProbability: Double)

// constants for the page rank formula
val dampening = 0.85
val randomJump = (1.0 - dampening) / NUM_VERTICES
val initialRank = 1.0 / NUM_VERTICES
  
// read inputs
val pages = DataSource(verticesPath, CsvInputFormat[Long]())
val edges = DataSource(edgesPath, CsvInputFormat[Edge]())

// assign initial rank
val pagesWithRank = pages map { p => PageWithRank(p, initialRank) }

// the iterative compüutation
def computeRank(ranks: DataSet[PageWithRank]) = {

    // send rank to neighbors
    val ranksForNeighbors = ranks join edges
        where { _.pageId } isEqualTo { _.from }
        map { (p, e) => (e.to, p.rank * e.transitionProbability) }
    
    // gather ranks per vertex and apply page rank formula
    ranksForNeighbors .groupBy { case (node, rank) => node }
                      .reduce { (a, b) => (a._1, a._2 + b._2) }
                      .map {case (node, rank) => PageWithRank(node, rank * dampening + randomJump) }
}

// invoke iteratively
val finalRanks = pagesWithRank.iterate(numIterations, computeRank)
val output = finalRanks.write(outputPath, CsvOutputFormat())
{% endhighlight %}
    </div>
</div>

**Note that because programs are optimized by the Stratosphere compiler, no manual caching of invariant data sets is necessary; this happens automatically**.
</section>


<section id="connected_components">
<div class="page-header"><h2>Connected Components</h2></div>

This algorithm computes connected components in a graph by assigning all vertices in the same component the same label. In each step, the vertices propagate their current label to their neighbors. A vertex accepts the label from a neighbor, if it is smaller than its own label. The was originally suggested by [Pegasus](http://www.cs.cmu.edu/~pegasus/).

This algorithm uses a **delta iteration**: Vertices that have not changed their component do not participate in the next step. This yields much better performance, because the later iterations typically deal only with a few outlier vertices.

<ul class="nav nav-tabs">
    <li class="active"><a href="#connected_components_scala" data-toggle="tab">Scala</a></li>
    <li><a href="#connected_components_java" data-toggle="tab">Java</a></li>
</ul>
<div class="tab-content">
    <div class="tab-pane active" id="connected_components_scala">
{% highlight scala %}
case class VertexWithComponent(vertex: Long, componentId: Long)
case class Edge(from: Long, to: Long)
  
val vertices = DataSource(verticesPath, CsvInputFormat[Long]())
val directedEdges = DataSource(edgesPath, CsvInputFormat[Edge]())

val initialComponents = vertices map { v => VertexWithComponent(v, v) }
val undirectedEdges = directedEdges flatMap { e => Seq(e, Edge(e.to, e.from)) }

def propagateComponent(s: DataSet[VertexWithComponent], ws: DataSet[VertexWithComponent]) = {
  val allNeighbors = ws join undirectedEdges
        where { _.vertex } isEqualTo { _.from }
        map { (v, e) => VertexWithComponent(e.to, v.componentId ) }
    
    val minNeighbors = allNeighbors groupBy { _.vertex } reduceGroup { cs => cs minBy { _.componentId } }

    // updated solution elements == new workset
    val s1 = s join minNeighbors
        where { _.vertex } isEqualTo { _.vertex }
        flatMap { (curr, candidate) =>
            if (candidate.componentId < curr.componentId) Some(candidate) else None
        }

  (s1, s1)
}

val components = initialComponents.iterateWithDelta(initialComponents, { _.vertex }, propagateComponent,
                    maxIterations)
val output = components.write(componentsOutput, CsvOutputFormat())
{% endhighlight %}

The <a href="https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-scala-examples/src/main/scala/eu/stratosphere/examples/scala/graph/ConnectedComponents.scala">ConnectedComponents example (Scala)</a> implements the above example.
    </div>
    <div class="tab-pane" id="connected_components_java">
{% highlight java %}
// --- plan assembly ---
FileDataSource initialVertices = new FileDataSource(
    new CsvInputFormat(' ', LongValue.class), verticesInput, "Vertices");

// assign the initial id
MapOperator verticesWithId = MapOperator.builder(AssignInitialId.class)
    .input(initialVertices).name("Assign Vertex Ids").build();

// the loop takes the vertices as the solution set and changed vertices as the workset.
// the vertices are identified (and replaced) by their vertex id (field 0)
// initially, all vertices are changed. 
DeltaIteration iteration = new DeltaIteration(0, "Connected Components Iteration");
iteration.setInitialSolutionSet(verticesWithId);
iteration.setInitialWorkset(verticesWithId);
iteration.setMaximumNumberOfIterations(MAX_NUM_ITERATIONS); // a guard

// data source for the edges
FileDataSource edges = new FileDataSource(
    new CsvInputFormat(' ', LongValue.class, LongValue.class), edgeInput, "Edges");

// join workset (changed vertices) with the edges to propagate changes to neighbors
JoinOperator joinWithNeighbors =
    JoinOperator.builder(NeighborWithComponentIDJoin.class, LongValue.class, 0, 0)
        .input1(iteration.getWorkset())
        .input2(edges)
        .name("Join Candidate Id With Neighbor").build();

// find for each neighbor the smallest of all candidates
ReduceOperator minCandidateId =
    ReduceOperator.builder(new MinimumComponentIDReduce(), LongValue.class, 0)
        .input(joinWithNeighbors)
        .name("Find Minimum Candidate Id").build();

// join candidates with the solution set and update if the candidate component-id is smaller
JoinOperator updateComponentId =
    JoinOperator.builder(UpdateComponentIdJoin.class, LongValue.class, 0, 0)
        .input1(minCandidateId)
        .input2(iteration.getSolutionSet())
        .name("Update Component Id").build();

// the result from the join (which checked whether a vertex really changed) is the delta
// and the driving data for the next round
iteration.setNextWorkset(updateComponentId);
iteration.setSolutionSetDelta(updateComponentId);

// sink is the iteration result
FileDataSink result = new FileDataSink(new CsvOutputFormat(), output, iteration, "Result");
CsvOutputFormat.configureRecordFormat(result)
        .fieldDelimiter(' ')
        .field(LongValue.class, 0)
        .field(LongValue.class, 1);

// --- the individual functions ---

public class AssignInitialId extends MapFunction {

    public void map(Record record, Collector<Record> out) throws Exception {
        record.setField(1, record.getField(0, LongValue.class));    // give vertex id as initial id
        out.collect(record);
    }
}

public class NeighborWithComponentIDJoin extends JoinFunction {

    public void join(Record vertexWithComponent, Record edge,
                                                Collector<Record> out) {
        this.result.setField(0, edge.getField(1, LongValue.class));
        this.result.setField(1, vertexWithComponent.getField(1, LongValue.class));
        out.collect(this.result);
    }
}

@Combinable
@ConstantFields(0)
public class MinimumComponentIDReduce extends ReduceFunction {

    public void reduce(Iterator<Record> records, Collector<Record> out) {
        Record rec = null;
        long minimumComponentID = Long.MAX_VALUE;

        while (records.hasNext()) {
            rec = records.next();
            long candidateComponentID = rec.getField(1, LongValue.class).getValue();
            if (candidateComponentID < minimumComponentID)
                minimumComponentID = candidateComponentID;

        }

        rec.setField(1, new LongValue(minimumComponentID));
        out.collect(rec);
    }
}

@ConstantFieldsFirst(0)
public class UpdateComponentIdJoin extends JoinFunction {

    public void join(Record newVertexWithComponent, Record currentVertexWithComponent, Collector<Record> out){
        long candidateComponentID = newVertexWithComponent.getField(1, LongValue.class).getValue();
        long currentComponentID = currentVertexWithComponent.getField(1, LongValue.class).getValue();

        if (candidateComponentID < currentComponentID) {
            out.collect(newVertexWithComponent);
        }
    }
}
{% endhighlight %}

The <a href="https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/connectedcomponents/WorksetConnectedComponents.java">ConnectedComponents example (Java)</a> implements the above example.
    </div>
</div>
</section>


<section id="relational">
<div class="page-header"><h2>Relational Query</h2></div>

The examples below assume two tables, one with `orders` and the other with `lineitems` per order. The programs execute functionality resembling the following SQL statement, as inspired by the TPC-H decision support benchmark:

{% highlight sql %}
SELECT l_orderkey, o_shippriority, sum(l_extendedprice) as revenue
    FROM orders, lineitem
WHERE l_orderkey = o_orderkey
    AND o_orderstatus = "F" 
    AND YEAR(o_orderdate) > 1993
    AND o_orderpriority LIKE "5%"
GROUP BY l_orderkey, o_shippriority;
{% endhighlight %}

<ul class="nav nav-tabs">
    <li class="active"><a href="#relational_scala" data-toggle="tab">Scala</a></li>
    <li><a href="#relational_java" data-toggle="tab">Java</a></li>
</ul>
<div class="tab-content">
    <div class="tab-pane active" id="relational_scala">
{% highlight scala %}
// --- define some custom classes to address fields by name ---
case class Order(orderId: Int, status: Char, date: String, orderPriority: String, shipPriority: Int)
case class LineItem(orderId: Int, extendedPrice: Double)
case class PrioritizedOrder(orderId: Int, shipPriority: Int, revenue: Double)

val orders = DataSource(ordersInputPath, DelimitedInputFormat(parseOrder))
val lineItems = DataSource(lineItemsInput, DelimitedInputFormat(parseLineItem))

val filteredOrders = orders filter { o => o.status == "F" && o.date.substring(0, 4).toInt > 1993 && o.orderPriority.startsWith("5") }

val prioritizedItems = filteredOrders join lineItems
    where { _.orderId } isEqualTo { _.orderId } // join on the orderIds
    map { (o, li) => PrioritizedOrder(o.orderId, o.shipPriority, li.extendedPrice) }

val prioritizedOrders = prioritizedItems
    groupBy { pi => (pi.orderId, pi.shipPriority) } 
    reduce { (po1, po2) => po1.copy(revenue = po1.revenue + po2.revenue) }

val output = prioritizedOrders.write(ordersOutput, CsvOutputFormat(formatOutput))
{% endhighlight %}

The source code of this example can be found <a href="https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-scala-examples/src/main/scala/eu/stratosphere/examples/scala/relational/TPCHQuery3.scala">here</a>.
    </div>
    <div class="tab-pane" id="relational_java">
{% highlight java %}
// --- program assembly ---

// configure CSV parser for orders file
FileDataSource orders = new FileDataSource(new CsvInputFormat(), ordersPath, "Orders");
    CsvInputFormat.configureRecordFormat(orders).fieldDelimiter('|')
            .field(LongValue.class, 0)          // order id as Longh from position 0
            .field(IntValue.class, 7)           // ship prio as Int from position 7
            .field(StringValue.class, 2)        // order status as String from position 2
            .field(StringValue.class, 4)        // order date as String from position 4
            .field(StringValue.class, 5);       // order prio as String from position 5

FileDataSource lineitems = new FileDataSource(new CsvInputFormat(), lineitemsPath, "LineItems");
     CsvInputFormat.configureRecordFormat(lineitems).fieldDelimiter('|')
            .field(LongValue.class, 0)          // order id as Long from position 0
            .field(DoubleValue.class, 5);       // extended price as Double from position 5

MapOperator filterO = MapOperator.builder(new FilterO()).input(orders).name("FilterO").build();

// join both on field 0 which is a LongValue
JoinOperator joinLiO = JoinOperator.builder(new JoinLiO(), LongValue.class, 0, 0)
            .input1(filterO).input2(lineitems)
            .name("JoinLiO").build();

 // reduce on both field 0 (LongValue) and field 1 (StringValue) 
ReduceOperator aggLiO = ReduceOperator.builder(new AggLiO())
            .keyField(LongValue.class, 0)    
            .keyField(StringValue.class, 1)   
            .input(joinLiO).name("AggLio").build();

FileDataSink result = new FileDataSink(new CsvOutputFormat(), output, aggLiO, "Output");
    CsvOutputFormat.configureRecordFormat(result).fieldDelimiter('|')
            .field(LongValue.class, 0)
            .field(IntValue.class, 1)
            .field(DoubleValue.class, 2);
            
// --- Filter Function ---
public static class FilterO extends MapFunction {

    public void map(Record record, Collector<Record> out) {
        String orderStatus = record.getField(2, StringValue.class).getValue();
        String orderPrio   = record.getField(4, StringValue.class).getValue();
        String orderDate   = record.getField(3, StringValue.class).getValue();
        int year = Integer.parseInt(orderDate.substring(0, 4));

        if (orderStatus.equals("F") && orderPrio.startsWith("5") && year > 1993)
            out.collect(record);
    }
}

// --- Join Function ---
@ConstantFieldsFirst({0,1})
public static class JoinLiO extends JoinFunction {

    public void join(Record order, Record lineitem, Collector<Record> out) {
        order.setField(2, lineitem.getField(1, DoubleValue.class));
        out.collect(order);
    }
}

// --- Aggregation Function ---
public class AggLiO extends ReduceFunction {

    public void reduce(Iterator<Record> values, Collector<Record> out) {
        Record rec = null;
        double partExtendedPriceSum = 0;

        while (values.hasNext()) {
            rec = values.next();
            partExtendedPriceSum += rec.getField(2, DoubleValue.class).getValue();
        }
        rec.setField(2, new DoubleValue(partExtendedPriceSum));
        out.collect(rec);
    }
}
{% endhighlight %}

The source code of this example can be found <a href="https://github.com/stratosphere/stratosphere/blob/release-{{site.current_stable}}/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/relational/TPCHQuery3.java">here</a>.
    </div>
</div>

## Test Data

<section id="test_data">
The relational query example works with the TPC-H benchmark suite's data generator tool (DBGEN). You can use it to generate arbitrarily large sample input data sets (see [http://www.tpc.org/tpch/](http://www.tpc.org/tpch/)).
To use it together with Stratosphere, take the following steps:

1.  Download and unpack DBGEN
2.  Make a copy of *makefile.suite* called *Makefile* and perform the following changes:

{% highlight bash %}
# The Stratosphere program was tested with DB2 data format
DATABASE = DB2
MACHINE  = LINUX
WORKLOAD = TPCH

# according to your compiler, mostly gcc
CC       = gcc
{% endhighlight %}

1.  Build DBGEN using *make*
2.  Generate lineitem and orders relations using dbgen. A scale factor
    (-s) of 1 results in a generated data set with about 1 GB size.

{% highlight bash %}
./dbgen -T o -s 1
{% endhighlight %}
</section>