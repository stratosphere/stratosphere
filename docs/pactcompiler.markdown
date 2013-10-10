---
layout: documentation
---
The Pact Compiler
=================

The Pact Compiler is the component that takes a [Pact
Program](writepactprogram.html "writepactprogram")
and translates it into a Nephele DAG. The Pact Programming Model
specifies the parallelism in a declarative way: What are dependencies
for parallelization, rather than how to do it exactly. Due to that fact,
the process of the translation has certain degrees of freedom in the
selection of the algorithms and strategies that are to be used to
prepare the inputs for the user functions. Among the different
alternatives, it selects the cheapest one with respect to the cost
model.

The compiler is found in the project
*[pact-compiler](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler")*
and consist of two main components:

1.  The
    [Optimizer](pactcompiler#optimizer "pactcompiler"),
    which evaluates the different alternatives and selects the cheapest
    one. The optimizer works on an internal representation of the
    program DAG and returns its selected best plan in that
    representation. It is inspired by database optimizers such as the
    System-R Optimizer, or the Volcano Optimizer Framework.
2.  The [Job Graph
    Generator](pactcompiler#job_graph_generator "pactcompiler"),
    which takes the optimizer's representation and turns it into a
    Nephele DAG, specifying all parameters according to the optimizer's
    chosen plan.

Optimizer
---------

The optimizer is contained in the package
[eu.stratosphere.pact.compiler](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler").
The central class is
[PactCompiler](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/PactCompiler.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/PactCompiler.java"),
providing the *compile()* method that acts as the main entry point for
the compilation process. It accepts an instance of a [Pact
Plan](https://github.com/stratosphere-eu/stratosphere/tree/master//pact/pact-common/src/main/java/eu/stratosphere/pact/common/plan/Plan.java "https://github.com/stratosphere-eu/stratosphere/tree/master//pact/pact-common/src/main/java/eu/stratosphere/pact/common/plan/Plan.java")
and returns an instance of
[OptimizedPlan](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/plan/OptimizedPlan.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/plan/OptimizedPlan.java").

The optimizer uses an internal representation of the plan, which is a
DAG of nodes as the original Pact Plan. The nodes contain a large set of
properties, such as size estimates, cardinalities, cost estimates, known
properties of the data at a certain point in the plan, etc. In most
cases, the optimizer's nodes correspond directly to nodes in the Pact
Plan; however, additional nodes may be inserted, for example for
combiners and artificial dams. The classes for the internal
representation can be found in
[eu.stratosphere.pact.compiler.plan](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/plan "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/plan").

### Optimization Process

The optimization process starts with a preparatory phase, where the
optimizer connects to the JobManager and requests information about the
available instances. It selects a suitable instance type for the
program's tasks and records the amount of memory that the instances have
available. Refer to [Instances and
Scheduling](instancesandscheduling.html "instancesandscheduling")
for more information on how instances and resources are managed.

The actual optimization process is inspired by the design of the
System-R Optimizer, or the Volcano Optimizer Framework. It is similar to
System-R, because it uses a bottom-up approach to enumerate candidates,
and it is similar to Volcano, because it uses a generalized notion of so
called *interesting orders*. To understand these concepts, please refer
to the papers ”[Access path selection in a relational database
management
system](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.71.3735&rep=rep1&type=pdf "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.71.3735&rep=rep1&type=pdf")”
and ”[The Volcano optimizer generator: Extensibility and efficient
search](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.21.2197&rep=rep1&type=pdf "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.21.2197&rep=rep1&type=pdf")”.
Note that the optimizer contains no algorithm for the enumeration of
join orders, because a similar degree of freedom is not expressible in
the programming model. Currently, the structure of the Pact Plan is
fixed and the degrees of freedom are in the different shipping
strategies and the local strategies.

In our implementation, the optimization process has three phases, which
are schematically depicted in the following picture:

[![](media/wiki/optimization_process.png)](media/wiki/optimization_process.png "wiki:optimization_process.png")

All phases happen as a traversal of the graph defined by the Pact Plan.
The traversals start at the data sinks and proceed depth-first, with
certain rules. For example, nodes that have more than one successor
(which is possible, because we support DAGs rather than trees) traverse
only deeper when they have been visited coming from all successors.

**Phase 1:** The optimizer traverses the plan from sinks to sources and
creates nodes for its internal representation. When nodes for the
DataSourceContracts are created, it accesses the files, determines their
size and samples them to find out how many records they contain. That
information acts as the initial size and cardinality estimate. Once all
nodes are created, they are connected on the re-ascension of the
recursive depth-first traversal. When connecting nodes, the size and
cardinality estimates are created based on the predecessors' estimates
and the [Compiler
Hints](writepactprogram#compilerhints "writepactprogram").
In the absence of compiler hints, robust default assumptions are used.

**Phase 2:** Another traversal generates *interesting properties* for
each node. A node's interesting properties describe the properties of
the data that will help successor nodes to execute more efficiently. A
Reduce Contract, for example, will be more efficient, if the data is
already partition on the key, and lets its predecessors know that by
setting partitioning as an interesting property for them. Interesting
properties are propagated from the sinks towards the sources, but only,
if a certain contract has the potential to generate or preserve them. In
general, we must assume that the user function modifies fields in its
records. In that case, any partitioning on the keys that existed before
the application of the user function, is destroyed. Certain [User Code
Annotations](pactpm#user_code_annotations "pactpm"),
however, allow the compiler to infer that certain properties are
preserved nonetheless. In phase 2, the compiler also creates auxiliary
structures that are required to handle Pact Plans that are DAGs but not
trees. Those structures are for example required to find common
predecessors for two nodes.

**Phase 3:** The final phase generates alternative plan candidates,
where the [Shipping
Strategies](pactstrategies#shipping_strategies "pactstrategies")
and [Local
Strategies](pactstrategies#local_strategies "pactstrategies")
are selected. The candidates are generated bottom-up, starting at the
data sources. To create the candidates for a node, all predecessors are
taken (or all combinations of predecessors, if the node corresponds to a
Pact with multiple inputs), and are combined with the different
possibilities for shipping strategies and local strategies. Out of all
the candidates, the cheapest one is selected, plus all that produce
additional properties on the data, that correspond to interesting
properties defined in phase 2. That way, it is possible to get plans
that are more expensive at earlier stages, but where reusing certain
properties makes them overall cheaper.

### Example

The example below shows two different plans for the simplified [TPC-H
Query
Example](tpch-q3example.html "tpch-q3example").
The first candidate represents the optimizer's choice to realize the
*Match* contract though re-partitioning of both inputs. The presence of
the [User Code
Annotations](pactpm#user_code_annotations "pactpm")
announcing the unmodified fields lets the optimizer infer that the
partition still exists after the user function. The succeeding *Reduce*
contract can hence be realized without repartitioning the data. Because
no network is involved, the system decides not to use an extra combiner
here. The *Reduce* contract does have to re-sort the data, because the
optimizer cannot infer that any order of the Key/value pairs within a
partition would be preserved in this case.

The second candidate shows a variant, where the *Match* contract was
realized by broadcasting one input and leaving the other partitioned as
it is. Because no suitable partitioning exists after the *Match*
contract, the *Reduce* contract requires a re-partitioning step.

[![](media/wiki/alternative_candidates.png)](media/wiki/alternative_candidates.png "wiki:alternative_candidates.png")

### Cost Model

To compare a plan candidate against others, each plan is attributed with
certain costs. Costs are attributed to each node, in order to make
sub-plans comparable. Currently, the costs consist of total network
traffic and total disk I/O bytes. Because network traffic is typically
the major factor in distributed systems, we compare costs primarily by
the network traffic, and secondarily by disk I/O. It is possible that
costs are unknown, because the estimates they are based on, are unknown.
Unknown costs are always larger than known costs, wheres two unknown
costs are equally expensive.

Each optimizer node contains values for the costs that it contributes
itself, as well as the cumulative costs of itself and all of its
predecessors. In the case that some nodes are predecessors on multiple
paths in the DAG, their costs contribute only once to the cumulative
costs.

Job Graph Generator
-------------------

The
[eu.stratosphere.pact.compiler.jobgen.JobGraphGenerator](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/jobgen/JobGraphGenerator.java "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-compiler/src/main/java/eu/stratosphere/pact/compiler/jobgen/JobGraphGenerator.java")
takes an optimized plan and translates it into a Nephele
[JobGraph](writingnehelejobs#connectingtasks "writingnehelejobs").

In a first step, a JobGraph Vertex is created for each optimizer node.
The pact-runtime class for the input contract is set as the vertex' code
to run. That pact-runtime class executes the [Local
Strategy](pactstrategies#local_strategies "pactstrategies")
that the compiler selected for that Pact. Parameters like the stub class
name, or the amount of memory to use for the runtime algorithms, are set
in the task's configuration.

The [Shipping
Strategies](pactstrategies#shipping_strategies "pactstrategies")
selected by the compiler are expressed in the wiring of Job Vertices. In
a second step, the Job Graph Generator connects the vertices through
channels as described in the optimized plan. Three parameters are set
here:

1.  The channel type (in-memory or network) is assigned based on the
    co-location of tasks. In general, in-memory channels are used to
    connect two job vertices whenever the optimizer picked the *forward*
    shipping strategy, and the number of computing instances is the same
    for both of them. In that case, the vertices also share the same
    computing instance. In all other cases, namely when a connection
    re-partitions the data, broadcasts it, or redistributes it in any
    way over a different number of machines, a network channel is used.
2.  The behavior of the channels during parallelization is defined. For
    some channels, it is sufficient if they connect the *i'th* parallel
    instance of one vertex to the *i'th* instance of the other vertex.
    That is called a *pointwise pattern* and is typically used for
    *forward* shipping strategies. For strategies like partitioning and
    broadcasting, the channels need to connect every parallel instance
    of one job vertex with every parallel instance of the other job
    vertex. That is called a *bipartite pattern*. The type of connection
    pattern is also written into the task configuration.
3.  The strategy with which a task selects the channel it wants to send
    a datum to, like round-robin distribution, hash-partitioning,
    range-partitioning, or broadcasting.

### Example

[![](media/wiki/jobgraph.png)](media/wiki/jobgraph.png "wiki:jobgraph.png")

The above example shows the parallel dataflow (degree of parallelism is
3) created for the two example alternatives that were used to illustrate
the optimizer's behavior. The solid lines are network channels, the
dotted lines are in-memory channels. Note that in alternative one, for
both Map Pacts, every instance connects to every instance of the Match
Pact (*bipartite pattern*), because a repartitioning happens on both the
Match's inputs. All other connections are *pointwise* connections. In
alternative two, only the parallel instances of one Map Pact use the
(*bipartite pattern*), namely those that broadcast their data. The same
pattern is also used between the Combine and Reduce step, for
repartitioning.
