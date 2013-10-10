---
layout: documentation
---
Serialization to PactRecords
============================

This section assumes you are familiar with the general structure of
PactRecords.

### Schema inference

The efficient storage management of data inside PactRecords infers a
semi-structured schema. These schema especially extracts all keys that
are needed in reduce, match and cogroup jobs inside an Sopremo operator.
Thus these jobs are able to access there specificly needed keys more
quickly. To determine the actual schema implementation, all Sopremo
operators are able to return a set of key expressions. An analysis of
these expressions leads to an actual implementation.

### Schema types

In “eu.stratosphere.sopremo.serializaton” Sopremo defines a varity of
schemas. In the following section each of them is described with there
resulting PactRecord structure.

**DirectSchema:** These schema is used when no keys are needed. The
resulting PactRecord contains only one field which specifies a single
Sopremo value.

![](media/wiki/sopremo_directschema.png)

**GeneralSchema:** These schema is the most general one because it
stores arbitrary key expressions. The PactRecord contains one field for
each key expression specified and an additional one for the data these
expressions work with. An disadvantage of this schema is the redundant
storage of values. That means that if e.g. one key expression is an
ObjectAccess than the result of the evaluation is stored in an explicit
field of the resulting PactRecord. In addition to this, the same value
is also stored in the field that contains the data for the expressions.

![](media/wiki/sopremo_generalschema.png)

**ObjectSchema:** These schema saves keys who are all direct children of
an object. The resulting PactRecord contains one field for each
specified key and an additional one for all other keys.

![](media/wiki/sopremo_objectschema.png)

**HeadArraySchema:** These schema is the first one of the two provided
schemas for arrays. Accordingly to the specified size of the head x, the
first x elements will be stored explicitly in fields. All remaining
elements are grouped together and will be stored in an additional field
located behind this “head”.

![](media/wiki/sopremo_headarrayschema.png)

**TailArraySchema:** These schema is the second one of the two provided
schemas for arrays. Similiar to the HeadArraySchema, this schema stores
the last x elements explicitly in fields. All other elements are grouped
together and will be stored in an additional field located before this
“tail”

![](media/wiki/sopremo_tailarrayschema.png)
