---
layout: documentation
---
PACT Task Execution Strategies
==============================

Data analysis tasks implemented with the [PACT programming
model](pactpm.html "pactpm")
are declaratively specified with respect to data parallelism. The [PACT
Compiler](pactcompiler.html "pactcompiler")
optimizes PACT programs and chooses strategies to execute the program
that guarantee that all Parallelization Contract are fulfilled.
Execution strategies can be divided into [Shipping
Strategies](pactstrategies#shipping_strategies "pactstrategies")
that define how data is shipped over the network and [Local
Strategies](pactstrategies#local_strategies "pactstrategies")
that define how the data is processed within a Nephele subtask.   
 The [PACT Web
Frontend](executepactprogram#pactwebfrontend "executepactprogram")
and the [PACT Command-Line
Client](executepactprogram#pactcliclient "executepactprogram")
feature the displaying of compiled PACT programs, such that the chosen
execution strategies can be checked.   
 This page describes the execution strategies which are currently
available to the PACT Compiler.

Shipping Strategies
-------------------

Shipping Strategies define how data is shipped between two PACT tasks.

### Forward

The *Forward* strategy is used to simply forward data from one tasks to
another. The data is neither partitioned nor replicated. If the degree
of parallelism is equal for the sending and receiving task, in-memory
channels are used. In-memory channels are also used, if the same number
of computing instances are used for the sending and receiving task. For
example if five computing instances are used, the sender task has five
parallel instances (one per computing instance), and the receiver task
has 10 parallel instances (2 per computing instance), in-memory channels
are also applicable. Otherwise, the compiler will use network channels.
The connection pattern used for those channels is point-wise (a sending
tasks is connected to max(1,(dop,,receiver,, / dop,,sender,,)) receiving
tasks and a receiving tasks is connected to max(1,(dop,,sender,, /
dop,,receiver,,)) sending tasks.   
 The *Forward* is usually used in the following cases:

-   connect a *Map* contract
-   connect a *DataSink* contract (if no final sorting is requested)
-   reuse an existing partitioning (e.g., for *Reduce*, *Match*, and
    *CoGroup*)
-   connect the non-broadcasting input of an *Match* or *Cross*

### Repartition

The *Repartition* shipping strategy is used to distribute data among
subtasks. Thereby, each data record is shipped to exactly one subtask
(data is not replicated). The *Repartition* strategy uses a partitioning
function that is applied to the *Key* of an *!KeyValuePair* to
distinguish to which subtask the pair is shipped. Different partitioning
functions have different characteristics that can be exploited to
improve data processing. The most commonly used partitioning functions
are:

-   **hash-based**: Pairs with equal keys are shipped to the same
    subtask. Good hash-based partitioning functions frequently provide
    good load balancing with low effort.
-   **range-based**: Ranges over a sorted key domain are used to group
    Pairs. “Neighboring” pairs are send to the same subtask. In order to
    ensure good load balancing, the distribution of the keys must be
    known. Unknown distributions are usually approximated with sampling
    which produces a certain overhead.
-   **round-robin**: Pairs are evenly distributed of all subtasks. That
    produces *perfect* load balancing, with respect to the number of
    records, but no further usable properties on the data are generated.

The *Repartition* strategy is realized using a bipartite distribution
pattern (each sending subtask is connected to each receiving subtask)
and network channels.   

The *Repartition* strategy is typically used to establish a partitioning
such as for:

-   a *Reduce* contract
-   a *CoGroup* contract
-   a *Match* contract

### Broadcast

The *Broadcast* shipping strategy is used to fully replicate data among
all subtasks. Each data record is shipped to each receiving subtask.
Obviously, this is a very network intensive strategy.   
 The *Broadcast* strategy is realized through network channels and a
bipartite distribution pattern (each sending subtask is connected to
each receiving subtask).   

A *Broadcast* is used for example in the following cases:

-   connect one side of a *Cross* contract. The other side is connected
    with a *Forward* strategy.
-   connect one side of a *Match* contract. The other side is connected
    with a *Forward* strategy.

Local Strategies
----------------

Local Strategies define how data is processed within a Nephele subtask.

### Sort

The *Sort* strategy sorts records on their keys. The sort is switched
from an in-memory sort to an external sort-merge strategy if necessary.
  

*Sort* is for example used for *Reduce* contracts (streaming the sorted
data, all pairs with identical keys are grouped and handed to the
*reduce()* stub method) or sorted data sinks.

### Combining Sort

The *Combining Sort* strategy is similar to the *Sort* strategy.
However, in contrast to *Sort*, the *combine()* method of a *Reduce*
stub is applied on sorted subsets and during merging, in order to reduce
the amount of data to be processed afterward. This strategy is
especially beneficial for large number\_of\_records / number\_of\_keys
ratios. The *Combining Sort* strategy is only applicable for *Reduce*
tasks that implement the *combine()* method (see [Write PACT
Programs](writepactprogram.html "writepactprogram")).
  

*Combining Sort* is used for:

-   *Reduce* contracts: Intermediate results are reduced by *combine()*
    during sorting to a single pair per key.
-   *Combine* tasks: Data is reduced during sorting. The sorted pairs (a
    single pair per key) are shipped to a *Reduce* task using the
    *Repartitioning* shipping strategy.

### Sort Merge

The *Sort Merge* strategy operates on two inputs. Usually, both inputs
are sorted by key and finally aligned by key (merged). The *Sort Merge*
strategy comes in multiple variations, depending on whether none, the
second, the first, or both inputs are already sorted. The [PACT
Compiler](pactcompiler.html "pactcompiler")
derives existence of the sorting property from existing properties
(resulting from former sorts), Output Contracts, applied Shipping
Strategies.   

The *Sort Merge* strategy is applied for:

-   *Match* tasks: When merging both inputs, all combinations of pairs
    with the same key from both inputs are built and passed to the
    *match()* method.
-   *CoGroup* tasks: When merging both inputs, all values that share the
    same key from both inputs are passed to the *coGroup()* method.

### Hybrid-Hash

The *Hybrid Hash* strategy is a classical Hybrid Hash Join as in
Relational Database Systems. It reads one input into a partitioned hash
table and probes the other side against that hash table. If the hash
table grows larger than the available memory, partitions are gradually
spilled to disk and recursively hashed and joined.

The strategy is used for:

-   *Match* in most cases when no inputs are pre-sorted.

### Block-Nested-Loops

The *Block-Nested-Loops* strategy operates on two inputs and builds the
Cartesian product of both input's pairs. It works as follows: One input
is wrapped with a resettable iterator, which caches its data in memory,
or on disk, if necessary. The other input is read block-wise, i.e.
records are read into a fixed sized memory block until it is completely
filled. One record after another is read from the resettable iterator
and crossed with all values in the fixed-sized memory block. After all
records in the resettable iterator have been crossed with all values in
the memory block, the block is filled with the next values of the
unfinished input and the resettable iterator is reset. The Nested-Loop
is finished after the last records have been read into the memory block
and have been crossed with the resettable iterator.   

The *Block Nested-Loops* strategy comes in two variations, that
distinguish which input is read into the resettable iterator and which
input is read block-wise. The *Blocked Nested-Loop* strategy is usually
much more efficient than the *Streamed Nested-Loop*, due to the reduced
number of resets and disk reads (if the resettable iterator was spilled
to disk). However, it destroys the order on the outer side, which might
be useful for subsequent contracts.   

The *Block Nested-Loops* is used for:

-   *Cross* tasks to compute the Cartesian product of both inputs.
-   *Match* if a key occurs a large number of times.

### Streamed Nested-Loops

The *Streamed Nested-Loops* strategy operates on two inputs and builds
the Cartesian product of both input's pairs. It works as follows: One
input is wrapped with a resettable iterator, which caches its data in
memory, or on disk, if necessary. The other input is read one record at
a time. Each record is crossed with all values of the resettable
iterator. After the last record was read and crossed, the Nested-Loop
finishes.   

The *Streamed Nested-Loops* strategy comes in two variations, that
distinguish which input is read into the resettable iterator and which
input is read and crossed as stream. The *Streamed Nested-Loop* strategy
is usually much less efficient than the *Blocked Nested-Loop* strategy,
especially if the resettable iterator cannot cache its data in memory.
In that case, it causes a significantly larger number of resets and disk
reads. It does, however, preserve the order on the outer side, which
might be useful for subsequent contracts.   

The *Streamed Nested-Loops* is used for:

-   *Cross* tasks to compute the Cartesian product of both inputs.

