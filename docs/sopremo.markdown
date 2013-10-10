---
layout: documentation
---
Sopremo
=======

The Stratosphere data and procession model (Sopremo) builds upon the
PACT programming model and incorporates Json as a semantically rich data
model. In Sopremo, streams of Json objects are processed by operators
and result in one or more output streams. The operators are
interconnected to form a directed acyclic query plan that is optimized
and translated to a PACT plan.

In the following sections, we explain what an operator in Sopremo is,
how they process the data, and how they are translated into PACT plans.

Operator
--------

A Sopremo operator

-   Has multiple inputs and outputs,
-   Partitions input tuples from one or multiple inputs according to the
    semantic of the operator, and
-   Applies a transformation expression to these partitions to obtain
    resulting tuples.

All operators have at least one representation as a PactModule. However,
especially more complex operators can have more representations that can
be chosen using additional operator-specific parameters. While the
logical optimizer of Sopremo uses the three listed properties of a
Sopremo operator, the parameters are mostly ignored.

We distinguish between base and complex operators. The base operators
are heavily inspired by the relational algebra and constitute a mostly
complete set of basic operations. On the other hand, we provide
domain-specific, complex operators for tasks, such as text mining, data
mining, or data cleansing. Please refer to the [complete
list](#list " ↵") of all operators for more detailed information.

Data Model
----------

All entries in Sopremo are Json nodes. They might either be primitive
values, complex objects, or arrays.

Even multiplicity in Sopremo is expressed using this data model. More
specifically, the stream of objects coming from one operator and
streaming into another operator is interpreted as a unmaterialized array
of Json nodes. If a [function](#function " ↵") or
[expression](#expression " ↵") request multiple input parameters, the
paramaters are wrapped into one compact array node.

===== Expressions ===== \#expression

Since multiplicity can always be expressed with one node, there is also
only one method signature necessary to cover all cases.

    #!java
    JsonNode evaluate(JsonNode input, EvaluationContext context)

The currently supported expressions contain:

<table>
<thead>
<tr class="header">
<th align="left">Type</th>
<th align="left">Expressions</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left">Array</td>
<td align="left">ArrayCreation, ArrayAccess , ArrayMerger</td>
</tr>
<tr class="even">
<td align="left">Numeric</td>
<td align="left">ArithmeticExpression, CastExpression</td>
</tr>
<tr class="odd">
<td align="left">Object</td>
<td align="left">ObjectCreation, FieldAccess, PathExpression</td>
</tr>
<tr class="even">
<td align="left">Conditional</td>
<td align="left">BooleanExpression, UnaryExpression, ComparativeExpression, ConditionalExpression, ElementInSetExpression</td>
</tr>
<tr class="odd">
<td align="left">Other</td>
<td align="left">Constant, FunctionCall, InputSelection</td>
</tr>
</tbody>
</table>

===== Functions ===== \#function

There are two types of methods: Java functions and Sopremo functions.

Java functions::

    These function are implemented in Java as static methods in a service class. All built-in functions are implemented as Java functions. However, user-defined Java functions may also be registered manually. Overloading of such functions is supported and may either be a directly matching signature, a generic array signature, or a mixed signature with variable arguments.

Sopremo functions::

    The second type of functions are directly specified in Sopremo itself and mainly acts as shortcut for a complex expression similar to macros in C. They may be expanded by the optimizer. Currently, overloading is not supported, but it might be changed if a need arises.

Query Plan
----------

A query plan consists of interconnected operators where each output of
one operator is connected to at least one input of another operator. The
query plan forms a directed acyclic graph.

The data that flows from one operator to another is a stream of Json
nodes (mostly objects).

Modularization
--------------

It is planned to separate use-case-specific operators in different
modules/packages that have to be included in a query. The main reason is
the different nature of the use-case operators. In comparison to the
base operators, we assume that the set of use-case operators is
incomplete in two regards. First, new use cases may arise at any time.
We mainly support the three use cases text mining, data mining, and data
cleansing. However, the Stratosphere system is a general-purpose data
processing framework that may be easily extended to support more use
cases. Second, even within a use case, we expect more operators to be
added over time. The new operators are either the result of a shift of
perspective that demand new operators or refine existing workflows to
add convencience operators for common operations.

Each module is mainly self-contained except for some information
concerning [optimization](#optimization " ↵"). Namely, it contains all
operators, built-in functions, helper structures, and optimization
information. The actual format is still work in progress but can be
expected to utilize the java library system (jar+META\_INF).

The base operator module is included per default as all queries depend
on them. Other modules are implicitly included if they appear in a
Sopremo tree.

===== Logical Optimizer ===== \#optimization

The optimizer is currently not implemented. We favor a rule-based
rewrite engine since the rewrite rules can be conveniently integrated
into the modules. Rewrite rules between two modules can be formulated as
well, even if they limit the degree of independence between modules.
However, as long as only one module assumes the existance of another, a
basic degree of independence is guarateed.

The current model provides the following **degrees of freedom**

-   Reordering operators (pushing of selection)
-   (Not explicit yet) Manipulation of the order of binary operations
    generated from n-ary operations
-   (Not explicit yet) Choice between different implementation
    strategies

Related Information
-------------------

[Translation to
PACT](sopremotopact.html "sopremotopact")

[Implementation
Details](sopremoimplementation.html "sopremoimplementation")

Curent Limitations
------------------

-   no subqueries
-   caches all values of a stream in an array
-   function have to be registried in advanced since the
    *EvaluationContext* cannot be changed persistently in a stub

To be discussed
---------------

-   Are dependencies between operators allowed? How do we model them?
    (i.e. the relation extraction operator in the text mining tasks
    requires some preprocessing)
-   How are operator variants (different methods for one task) /
    extraction goals (high precision, high recall etc) modeled?
    Expressed in parameters?

===== Operator set ===== \#list

Operator

\#Input

\#Output

Transformation

Conditions

Parameters

Comments

Jaql

PACT mapping

IO

tbd: Are there different IO operators for different file formats?

Read

0

1

Write

1

0

\\\\

Primitives

Union

2 (N)

1

-

-

bag or set?

default set?

union function

Multiple inputs to one PACT (to be implemented)

Intersection

2 (N)

1

-

-

bag or set?

default set?

?

Match

Difference

2 (N)

1

-

-

bag or set?

default set?

?

Cogroup

Projection

1

1

projection(s)

-

-

includes rename

transform operator

Map

Selection

1

1

-

selection(s)

-

filter operator

Map

\\\\

Joins

Natural join

2 (N)

1

join projection

join attributes

-

join operator + explicit conditions

Match

Equijoin

2 (N)

1

join projection

join attributes

-

join operator + == condition

Match

Semijoin

2 (N)

1

join projection

join attributes

-

join operator + projection

Match

Antijoin

2 (N)

1

join projection

join attributes

-

join operator + != condition

Cogroup

Outer join

2 (N)

1

join projection

join attributes

preservation flags

join operator + preserve flag

Cogroup

Theta join

TPC-H join nations except n = n, often self-joins

N/A

Cross

\\\\

Grouping

Aggregation

1

1

group transformation

group attributes

aggregation

group by operator

Map + Reduce / Cogroup

\\\\

JAQL operations (tbd)

tbd: who needs which one of these operators for which tasks?

Sort

1

1

-

order condition

-

sort operator

Empty? Reduce with specific compiler hint?

Top

1

1

-

order condition

-

STOP AFTER

top operator

?

Unnest

1

1

unnesting

-

-

expand operator

Map

Pivot

1

1

pivotize

\\\\

Data cleansing (Arvid)

Record Linkage

1, 2 (N)

1

output transformation

similarity condition

blocking param

extension: link-to operator

Several strategies

Transitive closure

1

1

-

-

-

iterative

function? or extension: cluster operator

Iterative algorithm (to be designed!)

Fusion

1

1

fusion operators

-

weights

extension: fuse operator

Reduce + Map(s)

Substitution

2

1

substitution

identification cond

-

extension: substitute operator

Cogroups + Map

Text extraction (Astrid)

Named Entity Recognition

1

1

output transformation

entity types

extension: annotate-entities operator

Map

Relation Extraction

1

N

output transformation

relation types (optional)

extension: annotate-relations operator

Map

Sentence Splitter

1

N

output transformation

-

in a later stage, we might merge sentence splitter, tokenizer, and pos
tagger into a single preprocessing operator

transform operator

Map

Tokenizer

1

N

output transformation

-

transform operator

Map

POS Tagger

1

1

output transformation

-

transform operator

Map

Filter

1

0/1

-

filter condition

filter operator

?

Species Recognition

1

N

output transformation

species types

could be a special NER task, but some sophisticated method is also
possible. therefore, species recognition should be an operator

extension: annotate-species operator

Map

Data mining (Fabian)

Additional operators (tbd)

tbd: who needs these operators for which tasks?

Switch

1

N

Classification

1

N

Deferred (tbd)

tbd: who needs these operators for which tasks?

Rename

1

Included in projection

transform operator

Map

Division

Any use case?

?

Cogroup + Reduce?

Split

1

1

array projection

-

-

Implicit in DAG

tee function

Input to multiple PACTs
