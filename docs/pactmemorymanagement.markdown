---
layout: documentation
---
PACT Memory Management
======================

Many of the [local execution
strategies](pactstrategies.html "pactstrategies")
such as sorting require main memory. Usually, the amount of assigned
memory significantly affects the performance of the strategy. Therefore,
memory assignment is an important issue in data processing systems.   

In the Stratosphere system, the [PACT
Compiler](pactcompiler.html "pactcompiler")
is responsible for assigning memory to local strategies. The current
implementation of memory assignment is straight-forward and is applied
after the best execution plan was determined. It works as follows:

-   All memory consuming strategies are enumerated. A strategy has a
    fixed weight that represents its requirements (for example,
    *SORT\_BOTH\_MERGE* which sorts both inputs requires twice as much
    memory as *SORT\_FIRST\_MERGE* which sorts only the first input).
-   Minimum amount of available available memory among all instances /
    TaskManagers is determined.
-   The memory is split among all memory consuming local strategies
    according to their weight.

