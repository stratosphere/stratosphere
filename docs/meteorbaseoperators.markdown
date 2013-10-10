---
layout: documentation
---
Meteor Base Operators
=====================

On this page, we describe the syntax and semantics of the [base
operators](sopremobase.html "wiki:sopremobase")
in Meteor by examples.

Relational operators
====================

filter
------

The filter operator filters his input by only retaining those elements
where the given predicate evaluates to true. This operators semantic is
equivalent to the WHERE-clause in SQL.

#### Syntax

> filter \<input\> *where* \<predicate\>;

input: an iterable set of objects of type T (e.g. array of T)

output: the filtered set of objects of type T (→ count(output) \<=
count(input)

The filter operator automatically binds an iteration variable that can
be used in the \<predicate\>. By default this variable has the same name
as the \<input\>, but can be renamed. To do so the \<input\> in the
described syntax of the operator has to be replaced with:

> \<iteration variable\> in \<input\>

predicate: an expression that evaluates to a boolean value, this
predicate decides which elements of the input are retained.

#### Example

default iteration variable

> **\$students** = read *from* 'hdfs://server/path/students.json';  
>  **\$teens** = filter **\$students** *where*
> (now()-**\$students**.birthDay).year \< 20;  
>  write **\$teens** *to* 'hdfs://server/path/result.json';

renaming iteration variable

> **\$input** = read *from* 'input.json';  
>  **\$result** = filter **\$emp** in **\$input** *where* **\$emp**.mgr
> or **\$emp**.income \> 30000;  
>  write **\$result** *to* 'output.json';

transform
---------

The transform operator allows the transformation of each element of an
input. This operator takes a set of type T1-elements and transforms
them, according to a given expression, to a set of type T2-elements. The
SQL-equivalent for this operator is the SELECT-clause.

#### Syntax

> transform \<input\> *into* \<transformation expression\>;

input: a set of type T1-elements (e.g. array of T1)

output: a set of transformed elements, each element of this set is of
type T2 (→ count(output) == count(input))

The transform operator automatically binds an iteration variable that
can be used inside the \<transformation expression\>. By default this
variable has the same name as the \<input\>, this means values from
records can be accessed via \$input.key and values from array via
\$input[index].

transformation expression: This expression defines the transformation.
It describes how each element of the result is constructed from his
corresponding input-element.

#### Example

> **\$input** = read *from* 'input.json';  
>  **\$result** = transform **\$input** *into* {sum: \$input.a +
> \$input.b; first: \$input[0]};  
>  write **\$result** *to* 'output.json';

join
----

The join operator allows to join two or more input sets into one
result-set. Although this operator only allows joins between two or more
inputs, a self-join can simply be realized by specifying the same
data-source as both inputs. The join condition between two inputs can be
any expression that evaluates to a boolean value, when joining more than
two inputs the condition is assumed to be a conjunction of such
expressions. This operator supports multiple types of joins like
natural, left- and right-outer and outer-joins. The semantic is
equivalent to SQL's JOIN.

#### Syntax

> join \<input 1\>, \<input 2\> …  
>  *preserve*? \<input name\> …   
>  *where* \<join conditions\>  
>  *into* \<output expression\>;

input: Two or more sets of objects

output: A set of the result of this join. The structure of the elements
of this set is defined by the \<output expression\>.

join conditions: These expressions define whether a tuple of the input
elements is part of the result or not. You can use all kinds of
comparisons inside this expressions.

output expression: This expression defines the structure of the elements
of the result set. To minimize the copy&paste work when keeping all
attributes of the input exactly the same in the output, \$input.\* can
be used instead of copying all input-attributes expilicitly to the
output.

preserve: If *preserve* is used for an input, all elements of the
specified input will appear in the result set, whether they have found
matching values or not. By using the preserve-option you are able to
achieve the same semantics as the different OUTER JOIN options in SQL.

The join operator automatically binds the variable **\$input i** to each
element of \<input i\>. These variables can be used by both, the \<join
conditions\> and the \<output expression\> to access the elements of
each input. To rename the default-name of the variable **\$input i** the
\<input i\> in the described syntax has to be replaced with \<variable
name\> in \<input i\>.

#### Example

default variable names without preserve option

> **\$users** = read *from* 'users.json';  
>  **\$pages** = read *from* 'pages.json';  
>  **\$result** = join **\$users**, **\$pages**  
>  *where* **\$users**.id == **\$pages**.userid  
>  *into* { **\$users**.name, **\$pages**.\* };  
>  write **\$result** *to* 'result.json';

this example renames all of the default variable names

> **\$users** = read *from* 'users.json';  
>  **\$pages** = read *from* 'pages.json';  
>  **\$result** = join **\$u** in **\$users**, **\$p** in **\$pages**  
>  *where* **\$u**.id == **\$p**.userid  
>  *into* { **\$u**.name, **\$p**.\* };  
>  write **\$result** *to* 'result.json';

the next example makes use of the preserve option

> **\$users** = read *from* 'users.json';  
>  **\$pages** = read *from* 'pages.json';  
>  **\$result** = join **\$u** in **\$users**, **\$p** in **\$pages**  
>  *preserve* **\$u**  
>  *where* **\$u**.id == **\$p**.userid  
>  *into* { **\$u**.name, **\$p**.\* };  
>  write **\$result** *to* 'result.json';

to show that not only equi-joins are possible, the following join
condition could be used

> …  
>  *where* **\$u**.id \< **\$p**.userid  
>  …

group
-----

The group operator groups the elements of one or more inputs on a
grouping key into one output. The resulting output contains one item for
each group. During the process of transforming a whole group into the
result item aggregate functions like count() and sum() can be applied.
If the group operator is specified with a single input, its semantic is
equivalent to SQL's GROUP BY clause.

#### Syntax

single input

> group \<input\> by \<grouping expression\> into \<aggregation
> expression\>;

input: a set of elements of type T (e.g. array of type T)

output: A set of the resulting groups, each group is represented by a
single item of type T2. The structure of this items is defined by the
\<aggregation expression\>.

grouping expression: This part is optional and defines the way of how to
extract the grouping key from the elements of the input. The resulting
key is used to group the elements together. If only one global aggregate
should be generated as the result the described syntax can be modified
like this example:

> group \<input\> into \<aggregation expression\>;

aggregation expression: This expression is evaluated for each group and
results in an item of type T2. This expression can apply functions like
count() or sum() to aggregate the elements of a group.

The group operator automatically binds a variable name. Both, \<grouping
expression\> and \<aggregation expression\> can use this name
(\<grouping expression\> → elements of the input, \<aggregation
expression\> → a single group). By default this name is the same as the
input, but as any variable in meteor operators, renaming is possible. To
do so the \<input\> in the described syntax has to be replaced with
\<variable name\> in \<input\>.

multiple inputs

> group \<input 1\> by \<grouping expression 1\>, \<input 2\> by
> \<grouping expression 2\> … into \<aggregation expression\>;

#### Example

Single input → single, global aggregate

> **\$employees** = read *from* 'employees.json';  
>  **\$result** = group **\$employees** *into* count(**\$employees**);  
>  write **\$result** *to* 'output.json';

Single input → partition into several groups and apply a function per
group

> **\$employees** = read *from* 'employees.json';  
>  **\$result** = group **\$employees** *by* **\$employees**.dept *into*
> {**\$employees**[0].dept, total: sum(**\$employees**[\*].income)};  
>  write **\$result** *to* 'output.json';

Single input → renaming default variable names, partition into several
groups and apply a function per group

> **\$employees** = read *from* 'employees.json';  
>  **\$result** = group **\$e** in **\$employees** *by* **\$e**.dept
> *into* {**\$e**[0].dept, total: sum(**\$e**[\*].income)};  
>  write **\$result** *to* 'output.json';

Multiple inputs → \> **\$employees** = read *from* 'employees.json';

> **\$depts** = read *from* 'departments.json';  
>  **\$result** = group **\$es** in **\$employees** *by* **\$es**.dept,
> **\$ds** in **\$depts** *by* **\$ds**.did  
>  *into* { dept: **\$ds**.did, deptName: **\$ds**[0].name, emps:
> **\$es**[\*].id, numEmps: count(**\$es**) };  
>  write **\$result** *to* 'output.json';

Set operators
=============

intersect
---------

The intersect operator computes the intersection between two or more
inputs.

#### Syntax

> intersect \<input 1\>, \<input 2\> …;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in all input sets

#### Example

> **\$users1** = read *from* 'users1.json';  
>  **\$users2** = read *from* 'users2.json';  
>  **\$commonUsers** = intersect **\$users1**, **\$users2**;  
>  write **\$commonUsers** *to* 'commonUsers.json';

union
-----

The union operator computes the set-based union of two or more inputs.

#### Syntax

> union \<input 1\>, \<input 2\> …;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in at least one
of the inputs

#### Example

> **\$users1** = read *from* 'users1.json';  
>  **\$users2** = read *from* 'users2.json';  
>  **\$allUsers** = union **\$users1**, **\$users2**;  
>  write **\$allUsers** *to* 'allUsers.json';

subtract
--------

The substract operator computes the difference between two or more
inputs. The result contains all elements of the first input that are not
part of the other inputs.

#### Syntax

> subtract \<input 1\>, \<input 2\> …;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in the first
input but not in all others

#### Example

> **\$oldUsers** = read *from* 'oldUsers.json';  
>  **\$currentUsers** = read *from* 'currentUsers.json';  
>  **\$newUsers** = subtract **\$currentUsers**, **\$oldUsers**;  
>  write **\$newUsers** *to* 'newUsers.json';

union all
---------

The union all operator computes the bag-based union of two or more
inputs.

#### Syntax

> union all \<input 1\>, \<input 2\> …;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in at least one
of the inputs (may contain duplicates)

#### Example

> **\$users1** = read *from* 'users1.json';  
>  **\$users2** = read *from* 'users2.json';  
>  **\$allUsers** = union all **\$users1**, **\$users2**;  
>  write **\$allUsers** *to* 'allUsers.json';

— replace pivot split
