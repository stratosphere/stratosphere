---
layout: documentation
---
Pact Module
-----------

Modularization in programming languages divides a complex program in
small modules that perform a specific task. The modules can be
maintained separately as they expose a public API and hide their
implementation details. Therefore, new programs can be composed by
connecting the modules using their public API.

The concept can also be transferred to [Pact
programs](writepactprogram.html "writepactprogram").
Since programs are directed acyclic graphs, the API consists a list of
input and output channels. The implementation are the contracts and
stubs that are connected to the input and output channels.

Since the graph structures are traversed from their sinks to their
sources, it is also necessary to manage a list of internal sinks to
support a broad spectre of modules. These internal sinks do not belong
to the public API and may not be connected to any contract that is not
part of the module.

Examples
--------

### Aggregation with key extraction

The following Pact module calculates the sum of the prices of all items
li purchased by a customer c. It therefore groups all entries by the
customer id in the Map and calculates the sum in the Reduce stubs.

The module has one input and one output channel and has two contracts
and respective stubs as the implementation.

[300px)]](image_aggregation.svg "image_aggregation.svg")

### Join with key extraction

The next join Pact module performs an simple equi join by extracting the
respective join key from the input tuples in the two Maps and projects
the fields of both entries to one entry in the Match.

The module has two input and one output channels and has three contracts
and respective stubs as the implementation.

[500px)]](image_join.svg "image_join.svg")

However, this is obviously not the only implementation that does exactly
the specified operation. The second contract exposes the exact same API
and performs the same operation but with three more contracts. However,
this version is easily adjustable for an arbitrary number of inputs.

The module first wraps all elements in an array by appending a Map
contract to each source. Before each Match (or CoGroup for outer joins),
it extracts the respective join key in two Maps. The Matches merge the
arrays of input tuples to ultimately fill in all the empty positions.
Finally, a last Map performs the projection of the joined elements to
the final result.

[500px)]](image_complexjoin.svg "image_complexjoin.svg")
