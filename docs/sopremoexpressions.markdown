---
layout: documentation
---
Evaluation Expressions
======================

These article describes the general purpose of the Evaluation
Expressions within Sopremo and provide the reader informations about how
to implements his own expressions.

### Evaluable expressions in Sopremo

The Evaluation Expressions in Sopremo are used for the specification of
certain conditions, rules or data transformations. To achive a complex
goal the nesting of these expressions to a tree is possible. Each
expression operates on and produces a single IJsonNode. These nodes
provide the data that should be evaluated by this expression and contain
the data produced by the evaluation. To determine the expectations an
expression has concerning the structure of this nodes see the
corresponding java doc. The expressions in Sopremo also support the
optimization of memory usage by providing the possibility to specify an
already existing node as the target. That means that instead of creating
a new object for the result, the provided one will be used.

### Expressions provided by Sopremo

With the package “eu.stratosphere.sopremo.expressions” Sopremo provides
a variety of different expressions that are specialized to handle
different kinds of datatypes. In the following section some of the basic
expressions are listed, grouped by there scope. To get a full list of
all available expressions see the package mentioned above.

**Array:** ArrayCreation, ArrayAccess, ArrayProjection, ArrayMerger

**Object:** ObjectCreation, ObjectAccess, PathExpression

**Numeric:** ArithmeticExpression

**Conditional:** ComparativeExpression, TernaryExpression,
ElementInSetExpression, UnaryExpression

**Other:** ConstantExpression, FunctionCall, InputSelection

### Writing custom expressions

To write own expressions it is important to extend from
“eu.stratosphere.sopremo.expressions.EvaluationExpression”. These allows
the usage of the newly implemented expression in expression trees. To
implement a custom condition, rule or data transformation, the
expression must override the inherited method evaluate(IJsonNode,
IJsonNode, EvaluationContext) and has to place his logic inside this
method. The first parameter in this methods signature contains the data
that are needed for the evaluation, e.g. the array that should be
accessed by the expression
“eu.stratosphere.sopremo.expressions.ArrayAccess”. In your custom
expressions you have to assume a certain structure of this node. The
second one is an optional IJsonNode that should be used as the target of
this evaluation, that means that the resultdata should be stored in this
target instead of a newly created node. If possible your custom
expressions should always try to reuse this target node to support the
already existing system of reusage within Sopremo. If needed you can
provide additional informations to the evaluation with the last
parameter.
