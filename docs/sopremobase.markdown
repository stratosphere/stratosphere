---
layout: documentation
---
Sopremo Base
------------

Common operators for dealing with structured, semi-structured, and
unstructured data.

The current set of operators listed with their Sopremo and Meteor names
as well as their properties.

Category

Sopremo Name

Meteor Name

\#Inputs

Meteor Properties

Comments

relational

Selection

filter

1

condition

Projection

transform

1

projection

Join

join

n

condition, projection

Grouping

group

n

keys, aggregation

co-group with n\>1

set

Intersection

intersect

n

-

set intersection

Union

union

n

-

set union

Subtraction

subtract

n

-

set difference

bag

UnionAll

union all

n

-

bag union

semi-structures

Replace

replace

1

path, dictionary

dictionary lookup

Pivotization

pivot

1

path

(un)nesting of tree

Split

split

1

path

denormalizes arrays

Please refer to the [Meteor Base
operator](meteorbaseoperators.html "wiki:meteorbaseoperators")
page for their usage in Meteor.
