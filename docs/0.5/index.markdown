--- 
layout: inner_with_sidebar
title: "Documentation (0.5-SNAPSHOT)"
links: 
  - { anchor: "jdbc", title: "JDBC Input/Output Format" }
  - { anchor: "collection_data_source", title: "CollectionDataSource" }
  - { anchor: "broadcast_variables", title: "Broadcast Variables" }
---

<p class="lead">
Documentation on *new features* and *changes* contained in the current *0.5-SNAPSHOT* development branch. It should be read *in addition* to the documentation for the latest 0.4 release.

<section id="jdbc">
### JDBC Input/Output Format

The JDBC input and output formats allow to read from and write to any JDBC-accessible database.
</section>

<section id="collection_data_source">
### CollectionDataSource

The CollectionDataSource allows you to use local Java and Scala Collections as input to your Stratosphere programs.
</section>

<section id="broadcast_variables">
### Broadcast Variables

Broadcast Variables allow to broadcast computation results to all nodes executing an operator. The following example shows how to set a broadcast variable and how to access it within an operator.

{% highlight java %}
// in getPlan() method

FileDataSource someMainInput = new FileDataSource(...);

FileDataSource someBcInput = new FileDataSource(...);

MapOperator myMapper = MapOperator.builder(MyMapper.class)
    .setBroadcastVariable("my_bc_var", someBcInput) // set the variable
    .input(someMainInput)
    .build();

// [...]

public class MyMapper extends MapFunction {

    private Collection<Record> myBcRecords;

    @Override
    public void open(Configuration parameters) throws Exception {
        // receive the variables' content
        this.myBcRecords = this.getRuntimeContext().getBroadcastVariable("my_bc_var"); 
    }

    @Override
    public void map(Record record, Collector<Record> out) {       
       for (Record r : myBcRecords) {
       	   // do something with the records
       }

    }
}
{% endhighlight %}
*Note*: As the content of broadcast variables is kept in-memory on each node, it should not become too large. For simpler things like scalar values you should use `setParameter(...)`.

An example of how to use Broadcast Variables in practice can be found in the <a href="https://github.com/stratosphere/stratosphere/blob/master/stratosphere-examples/stratosphere-java-examples/src/main/java/eu/stratosphere/example/java/record/kmeans/KMeans.java">K-Means example</a>.
</section>
