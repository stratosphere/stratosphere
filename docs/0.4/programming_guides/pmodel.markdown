---
layout: inner_docs_v04
title:  Stratosphere Programming Model
sublinks:
  - {anchor: "datamodel", title: "Record Data Model"}
  - {anchor: "operators", title: "Parallelizable Operators"}
  - {anchor: "dataflows", title: "Sequential Data Flows"}
---

## Stratosphere Programming Model

Stratosphere's programming model supports and eases the specification of complex, parallelizable data analysis tasks. Tasks are composed of parallelizable operators which are assembled in data flows. The programming model inherits some aspects from the well-known well-known [MapReduce Programming Model](http://en.wikipedia.org/wiki/MapReduce) but provides many more advanced features. 

This document explains the basic concepts of the Stratosphere programming model:

* [Record Data Model](#datamodel)
* [Operators](#operators)
* [Seqential Data Flows](#dataflows)
* [Iterative Data Flows](#iterations)

Stratosphere offers a [Scala](scala.html "Scala Programming Guide") and a [Java](java.html "Java Programming Guide") API for its programming model. We refer to the programming guides to learn how to develop and execute actual Stratosphere programs.

<section id="datamodel">
### Record Data Model

The Stratosphere operates on a record data model. A record consists of any number of data fields. A field can be of any data type that implements the *Key* or the *Value* interface. 

<img src="../img/recorddm.svg" width="800" alt="Record Data Model">

The Value interface requires that data types are serializable and deserializable. The Key interface requires as well de-/serialization and in addition methods to generate a deterministic hash and to compare keys of the same type. Hence, Key and Value have very similar requirements as their counterparts in Hadoop MapReduce.

<img src="../img/datatypes.svg" width="800" alt="Data Types">

A record field can be of any data type that implements the Value or Key interface, as for example a custom data type for geometric coordinates. Stratosphere provides a set of common data types in its distribution such as: 

* **Integer values:** Byte, Short, Integer, Long
* **Floating point values:** Float, Double
* **Text values:** Character, String
* **Special values:** Boolean, Null
* **Collections:** List, Map, Pair

A collection of records is called data set. Operators receive one or more data sets as input and produce one new data set. With exception of record keys (see [Operators]](#operators), Reduce), record fields are not inspected by the system but only by the user-defined functions of operators.

Compared to MapReduce's key-value pair data model, the Stratosphere's record data model reduces the amount of boilerplate code for data type handling. Every data type can be stored individually in a record. In contrast, the key-value pair model often requires that multiple data types are squeezed into a single key or value type resulting in additional data types. 

----

<section id="operators">
### Parallelizable Operators

Stratosphere's programming model is based on parallelizable operators. An operator consists for two components, a *user-defined function (UDF)* and a parallel *operator function*. The operator function parallelizes the execution of the user-defined function and applies the UDF on its input data. 

<img src="../img/operator.svg" width="800" alt="Stratosphere Operator">

Stratosphere's programming model provides six parallelizable operator functions:

* *Map*,
* *Reduce* (including an optional *Combine*),
* *Join*,
* *Cross*, 
* *CoGroup*, and
* *Union*.

Map, Join, and Cross operate on individual records, Reduce and CoGroup on groups of records. Map and Reduce operate on a single input, Join, Cross, and CoGroup combine the data of two input. The functions are described in detail in the following. The figures below show the operation of each operator function. Records which are passed together into a function call of an operator UDFs are grouped by the dashed lines.

#### Map

Map is a *Record-at-a-Time* operator with one input. It calls the user function for each individual input record. The user function accepts a single record as input and can emit any number of records (*0* to *n*). Typical applications for Map operators are filters or transformations. Stratosphere's Map has the same semantics as Hadoop MapReduce's Map.

<img src="../img/map.svg" width="800" alt="Map Operator">

#### Reduce

Reduce is a *Group-at-a-Time* operator with one input. It groups the records of its input on a so-called *Record Key*. The record key is specified as one or more fields of the input records. Each of these fields must implement the Key interface (see [Data Model](#datamodel)). Records that share the same record key are grouped together and handed together into the user function. The user function accepts a list of records as input and can emit any number of records. Common applications for Reduce operators are aggregations.

<img src="../img/reduce.svg" width="800" alt="Reduce Operator">

In many situations, a Reduce operator can be improved by providing an additional user-defined function called Combine. This is the case if the result of the Reduce operator can be computed from partial results that were computed on subgroups of a full key group. An example for a Reduce function with this property is the ```sum``` aggregation function. The sum of a list of numbers, say (1 + 2 + 3 + 4), can be computed by adding the sums of subgroups, e.g., (1 + 2) and (3 + 4). A Combine user function can be applied on subgroups and its result fed into the Reduce function (or another Combine function). A Combine function is beneficial, if it reduces the amount of data, as for example in the case of partial aggregations. Since it can be applied on subgroups, it is called before data is transferred over the network to establish the full group for the final Reduce function call. This can significantly reduce the amount of data that needs to be communicated and hence improve the efficiency of the job and its execution time.

Note that a Combine function is optional. It is not necessary (although usually highly recommended) to specify a Combine function. However, Stratosphere does not guarantee that the Combine function is actually executed. This is a cost-based decision done by Stratosphere's optimizer.

A common pattern in Reduce user functions is that all records of a group are collected and processed in a certain order. Many of these functions can be implemented as single pass operations over the records of a group, if the records within a group are in a specific order. Stratosphere's programming model allows to define the order of records within a group by specifying one or more group order keys and sorting directions (ascending, descending). The system will then make sure that the records that enter the Reduce user function are in the specified order.

#### Join

Join is a *Record-at-a-Time* operator with two inputs. Similar to Reduce, Join also requires the specification of record keys on both inputs. The operator function (equi-)joins both inputs on their record keys and hands matching pairs of records into the user function. The user function accepts one record of each input and can emit any number of records. Records whose keys do not match the key of any record in the other input are not processed and discarded. Typical applications of Join operations are equi-joins.

<img src="../img/join.svg" width="800" alt="Join Operator">

#### Cross

Cross is a *Record-at-a-Time* operator with two inputs. In contrast to Join, Cross does not require the specification of a record key on any input. The operator function builds the Cartesian product of the records of both inputs and calls the user function for each pair of records. The user function accepts one record of each input and can emit any number of records. Cross is usually a very expensive operation and should be used with care. In most use case, one of both inputs holds a very small number of records.

<img src="../img/cross.svg" width="800" alt="Cross Operator">

#### CoGroup

CoGroup is a *Group-at-a-Time* operator with two inputs. It requires the specification of a record key for each input. Records of both inputs are grouped on their key. Groups with matching keys are handed together to the user function. The user function accepts one list of records for each input and can emit any number of records. In contrast to the Join operator, also groups that have no matching group in the other input are given as a list to the user function and the other input list remains empty.

<img src="../img/cogroup.svg" width="800" alt="CoGroup Operator">

The Stratosphere programming model allows to individually specify the order of records within the groups of both inputs. Similar to the group order for Reduce operators, the system makes sure that the records of each group are handed to the CoGroup user function in the specified order.

#### Union

Union is an operator without a user-defined function. It merges two or more input data sets into a single output data set using bag semantics, i.e., duplicates are not removed.

#### User Code Annotations

Stratosphere features an optimizer that generates efficient execution plans for data processing tasks. A core concept of Stratosphere's programming model are user-defined functions. However, UDFs pose a big challenge for the optimizer because it is not aware of the internal semantics of an UDF. In many cases, the optimizer requires only little information about the behavior of an UDF to be able to apply a number of powerful optimizations. In the long run, Stratosphere will apply code analysis techniques to automatically extract that information. Until then, the programming model provides annotations for UDFs that explicitly provide the required information. The following annotations are available:

* **Constant fields**: Lists the fields of a record that are NOT modified by the UDF (neither value or position in the record).
* **All fields constant except**: Lists all fields that are modified by the UDF (value and/or position in the record). All other fields are considered to be constant.

Note that is it optional to provide these annotation. However, if incorrect annotations are provided, the result of the job might be incorrect!

----

<section id="dataflows">
### Data Flows

The Stratosphere programming model is based on data flows of parallelizable operators. 
A data flow consists of:

* Data Sources,
* Operators, and
* Data Sinks.

#### Data Sources

A data sources is the entry point of data into a data flow and produces exactly one data set. They provide a generic interface called *InputFormat* to connect to a variety of data stores as for example distributed or local file systems and relational or NoSQL database systems. An InputFormat reads data from such a data store and produces a data set by generating records from the read data. In general, InputFormats are also user-defined functions. Stratosphere provides InputFormats for a variety of file formats and data stores including:

* CSV files,
* Row-delimited text files,
* Binary files with constant record length,
* HBase connector, and
* JDBC database connector (non-parallel).

#### Operators

Stratosphere's operators are described in detail in the [Operators](#operators) section. They consume one or more data sets and produce exactly one data set.

#### Data Sinks

Data sinks are the exit point where data leaves a data flow. Similar to data sources, data sinks provide a generic interface called *OutputFormat* to write their input data set to a variety of data stores or streams. An OutputFormat serializes records into a format, such as a textual or binary representation, and writes it to an interface outside of the system as for example a file system or database. OutputFormats are as well user-defined functions such that data can be written to any external data store. At the moment, Stratosphere provides OutputFormats for:

* CSV files,
* Row-delimited text files, and
* Binary files.

#### Data Flow Composition

A data flow is composed of any number of data sources, operators, and data sinks by connecting their inputs and outputs. It is not possible to connect the operators defined in the [Operators](#operators) section in a cyclic data flow. Iterative data flows require special operators and abstractions which are described in the [Iterative Data Flow](#iterations) section.

<img src="../img/dataflow.svg" width="800" alt="Data Flow">

----
