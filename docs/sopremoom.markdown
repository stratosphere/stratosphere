---
layout: documentation
---
Sopremo Operator Model
----------------------

Sopremo is a framework to manage an extensible collection of
semantically rich operators organized into packages. It acts as a target
for the Meteor parser and ultimately produces an executable Pact
program. Nevertheless, we designed Sopremo to be a common base for
additional query languages such as XQuery or even GUI builders.

The data model builds upon Json to process unstructured and
semi-structured data. A *Sopremo value* thus represents a tree structure
consisting of objects, arrays, and atomic values. A *data set* is an
unordered collection of such trees under bag semantics. The base model
does not support any constraints such as schema definition in the first
place, but those may be enforced implicitly through specific operators.

An *operator* acts as a building block for a query. It consumes one or
more input data sets, processes the data, and produces one or more
output data sets. *Operator sub-types* share common characteristics, but
are specialized versions of the generic operator, e.g., IE operators for
entityand relationship annotation require different algorithms.
Operators are *instantiated* to reflect specific configuration and
adjustments, e.g., a join is an operator but a join over the attribute
`id` is an *operator instantiation*. A *Sopremo plan* is a directed
acyclic graph of interconnected operator instantiations.

### Examplary Sopremo Plan

[![](media/wiki/sopremo_plan_bw.png)](media/wiki/sopremo_plan_bw.png "wiki:sopremo_plan_bw.png")
The Sopremo plan represents a query that was formulated in
[Meteor](meteorquery#mixing_packages "wiki:meteorquery").
Relational operators co-exist with application-specific operators, e.g.,
data cleansing and information extraction operations such as
DuplicateRemoval and EntityAnnotation. The edges represent the flow of
data.

### Operators

Operators may have several properties, e.g., the `remove duplicates`
operator has a similarity measure and a threshold as properties. The
values of properties belong to a set of expressions that process
individual Sopremo values or groups thereof. These expressions can be
nested to perform more complex calculations and transformations. For
example, the selection condition of the example plan compares the year
of the calculated age with the constant 20 for each value in the student
data set.

Sopremo operators and packages can be developed independently. To be
able to use and combine operators from different packages, operators
must be self-descriptive in two ways. First, each operator provides meta
information about itself including their own configurable properties and
certain characteristics that can be used during optimization. Second,
all operator instantiations must define in which way they are executed
and parallelized. In particular, each operator must provide at least one
Pact workflow that executes the desired operation.

#### Operator Instantiations

An operator may be implemented in different ways. These operator
instantiations must provide a direct or undirect Pact implementation.

[![](media/wiki/pivotization_transformation.png)](media/wiki/pivotization_transformation.png "wiki:pivotization_transformation.png")
The figure shows how the instantiated operator `Pivotization` is
implemented as a *partial* Pact plan that consists of a Map and a
Reduce. The Pact plan for operators are only partial because they cannot
be executed due to the missing sources and sinks. Further, these partial
Pact plans have the same amounts of incoming and outgoing data flows as
the corresponding Sopremo operator. For complex Sopremo operators such
as `Duplicate Removal`, partial plans may easily consist of 20 Pacts.

Operator instantiations may also reuse other operators in their
implementation. These *composite* operators recursively translate the
reused operators into partial Pact plans and rewire the input and
outputs to form even larger partial Pact plans. Composition of operators
reduces implementation complexity. Future improvements of a reused
operator also improve the composite operator.

Additionally, operators may also have different implementation
strategies. The strategy can either be selected by properties or by an
optimizer. For example, a Sopremo join with an arbitrary join condition
may require a theta join with a cross Pact, while a join with a equality
condition can be efficiently executed with a match Pact.

### Query Compilation

In Sopremo, all operator instantiations have a direct Pact
implementation. Thus, the compilation of a complete Sopremo plan
consists of two steps. First, all operator instantiations in the Sopremo
plan are translated into partial Pact plans. Second, the inputs and
outputs of the partial Pact plans are rewired to form a single,
consistent Pact plan.

[![](media/wiki/sopremo_pact_plan_bw.png)](media/wiki/sopremo_pact_plan_bw.png "wiki:sopremo_pact_plan_bw.png")
The picture shows the result of translation process. Please note that we
here used naive duplicate detection for visualization purposes only.

To improve the runtime efficiency of a compiled plan, the translation
process is augmented with two more steps: First, a Sopremo plan is
logically optimized (work in progress). A separate, physical
optimization is performed later in the Pact layer. Second, Pact uses a
flat, schema-less data model that is necessary to reorder Pacts. For
pure Pact programs, the schema interpretation is performed by Pact users
in their UDFs. However, the additional semantics of Sopremo operator
allows Sopremo to infer an efficient data layout and bridge the gap
between the flat data model of the Pact model and the nested data model
of Sopremo. Meteor or Sopremo users thus do not have to specify the data
layout explicitly.

### Architecture

The following figure shows the architecture of the higher level language
layer. Meteor users formulate a query that is parsed into a Sopremo
plan. To import packages, Meteor requests the package loader of Sopremo
to inspect the packages and register the discovered operators and
predefined functions. Meteor uses this information to validate the
script and to translate it into a Sopremo plan. The plan is analyzed by
the schema inferencer to obtain a global schema that is used in
conjunction with the Sopremo plan to create a consistent Pact plan.

[![](media/wiki/sopremo_arch.png)](media/wiki/sopremo_arch.png "wiki:sopremo_arch.png")
