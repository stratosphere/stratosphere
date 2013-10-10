---
layout: documentation
---
Formulating a Query with Meteor
-------------------------------

This article briefly outlines how a meteor query looks and guides users
to manage complex data analysis tasks.

### A Basic Query

Meteor is a query language specifically designed for complex data
analysis tasks. Thus, the inputs and outputs of a query are usually
files that reside within the hdfs. Meteor resembles Jaql but is at the
same time simpler in the grammatical features and more powerful in the
overall usage as we will show throughout this article.

The following Meteor query performs a selection of all students that are
teen-aged and writes the result out to the HDFS.

> **\$students** = read *from* 'hdfs://server/path/students.json';  
>  **\$teens** = filter **\$students**  
>      *where* (now()-**\$students**.birthDay).year \< 20;  
>  write **\$teens** *to* 'hdfs://server/path/result.json';

In the first line, the data set that is read from the 'students.json' is
associated with the *variable* *'\$students*'. Variables always start
with a *'\$*' in Meteor. Note that for Meteor input and output paths the
same rules apply as for paths of data sources and sinks in PACT
programs, i.e. [filesystem prefix and absolute
paths](executepactprogram#specify_data_paths "wiki:executepactprogram").

The next two lines filter the data by a boolean expression that only
retains students whose age is less than 20. The boolean expression is
parsed to an expression tree that evaluates all incoming tuples to a
boolean value.

Finally, the results are written back to the HDFS. We assume that in
most complex analysis workflows, the result set is too large to print it
to the user in any meaningful way. Consequently, there is currently no
options to directly inspect the result set but users will have to open
the result files.

In addition to the already described usecase, where the data is stored
within the hdfs, meteor also supports other filesystems. To show this
case the example from above stays the same, only the data-read and
-write steps are altered.

> **\$students** = read *from* 'file:///home/user/students.json';  
>  **\$teens** = filter **\$students**  
>      *where* (now()-**\$students**.birthDay).year \< 20;  
>  write **\$teens** *to* 'file:///home/user/result.json';

In this case the input and output files are stored in the users
homedirectory.

### Adding a Package

One of the main design goals of the higher level language layer of
Stratosphere is the support for a large variety of applications. As we
neither want to nor can develop and maintain all possible operators, we
encourage power users to [SopremoOperators develop their own operators].
We will collect all packages that are developed for Stratopshere on a
[SopremoPackages specific page] for reference.

In the following, we add a duplicate removal step to the script.

> \<wrap hi\>using cleansing;\</wrap\>  
>  **\$students** = read *from* 'hdfs://server/path/students.json';  
>  \<wrap hi\>**\$students** = remove duplicates
> **\$students**\</wrap\>  
>  \<wrap hi\>    *where* average(levenshtein(name), dateSim(birthDay))
> \> 0.95\</wrap\>  
>  \<wrap hi\>    *retain* maxDate(enrollmentDate);\</wrap\>  
>  **\$teens** = filter **\$students**  
>      *where* (now()-**\$student**.birthDay).year \< 20;  
>  write **\$teens** *to* 'hdfs://server/path/result.json';

The `using` commands looks for a sopremo-*cleansing*.jar in the current
classpath or directory and makes all contained operators available for
future usage.

In the second highlighted fragment, we use and configure the
`remove duplicates` operator from the [cleansing
package](sopremocleansing.html "wiki:sopremocleansing").
The operator has two properties *where* and *retain*, which specify the
similarity condition and the conflict resolution function. For more
details, please refer to the [cleansing package
page](sopremocleansing.html "wiki:sopremocleansing").

Aside from operators, each Sopremo package may also contain
[functions](#functions " ↵") and constants. For example, the cleansing
package contains the `levenshtein` function that is used in the query.

### Meteor Grammar

Meteor differs quite heavily from most script languages because the
syntax of operators are not defined statically. All operators follow the
same syntax rules that are shown as an excerpt in the form of an EBNF.

> operator ::= \<wrap warning\>name+\</wrap\> inputs? properties? ';'  
>  \<wrap hi\>inputs\</wrap\> ::= (alias 'in')? variable (',' inputs)?  
>  properties ::= property properties?  
>  property ::= \<wrap safety\>property\_name\</wrap\> \<wrap
> box\>expression\</wrap\>  
>  variable ::= '\$' name

When we apply this grammar rule to the `remove duplicates` operator, we
extract the following fragments that have matching colors to the syntax
rules.

> \<wrap warning\>remove duplicates\</wrap\> \<wrap
> hi\>**\$students**\</wrap\>  
>      \<wrap safety\>*where*\</wrap\> \<wrap
> box\>average(levenshtein(name), dateSim(birthDay)) \> 0.95\</wrap\>  
>      \<wrap safety\>*retain*\</wrap\> \<wrap
> box\>maxDate(enrollmentDate);\</wrap\>

The configuration of the operators resembles the message passing
concepts of Smalltalk and similar programming languages. Operators are
first-class citizens and they validate the property values by
themselves.

### Mixing Packages

There is no restriction in how many packages can be combined in one
script. We expanded the running example to incorporate information
extraction operators.

> using cleansing;  
>  \<wrap hi\>using ie;\</wrap\>  
>    
>  **\$students** = read *from* 'hdfs://server/path/students.json';  
>  **\$students** = remove duplicates **\$students**  
>      *where* average(levenshtein(name), dateSim(birthDay)) \> 0.95  
>      *retain* maxDate(enrollmentDate);  
>  **\$teens** = filter **\$students**  
>      *where* (now()-**\$student**.birthDay).year \< 20;  
>    
>  \<wrap hi\>**\$articles** = read *from* 'news.json'; \</wrap\>  
>  \<wrap hi\>**\$articles** = annotate sentences
> **\$articles**\</wrap\>  
>  \<wrap hi\>    *using* morphAdorner;\</wrap\>  
>  \<wrap hi\>**\$articles** = annotate entities in
> **\$articles**\</wrap\>  
>  \<wrap hi\>    *using* type.person and regex 'names.txt';\</wrap\>  
>  \<wrap hi\>**\$peopleInNews** = pivot **\$articles** around
> **\$person**=**\$article**.annotations[\*].entity\</wrap\>  
>  \<wrap hi\>    *into* {\</wrap\>  
>  \<wrap hi\>        name: **\$person**,\</wrap\>  
>  \<wrap hi\>        articles: **\$articles**\</wrap\>  
>  \<wrap hi\>    };\</wrap\>  
>    
>  \<wrap hi\>**\$teensInNews** = join **\$teen** in **\$teens**,
> **\$person** in **\$peopleInNews**\</wrap\>  
>  \<wrap hi\>    *where* **\$teen**.name ==
> **\$person**.name\</wrap\>  
>  \<wrap hi\>    *into* {\</wrap\>  
>  \<wrap hi\>        student: **\$teen**,\</wrap\>  
>  \<wrap hi\>        articles: **\$person**.articles[\*].url\</wrap\>  
>  \<wrap hi\>    };\</wrap\>  
>    
>  write **\$teensInNews** *to* 'hdfs://server/path/result.json';

The additional operators coexist with the data cleansing operators. In
fact, both operators also coexist with the base operator package that is
implicitly included in Meteor scripts.

When only one operator or function from a package is used, they can be
directly referenced via namespacing. `cleansing:remove duplicates` has
the same effect in this context even when `using cleansing` is omitted.
Namespacing also helps when packages have conflicting operator names.

### Functions

Functions can be defined in three ways:

-   Packages may include functions
-   Sopremo functions may be directly defined in the script and
    basically act as macros
-   User-defined functions written in Java and imported with *javaudf*

We already saw an example for the first function (`levenshtein` in
*cleansing*).

The next snippets show how the other two function types are defined.

> square = fn(x) x \* x;  
>  sum\_udf= javaudf('packageName.JavaClass.sum');

The corresponding Java definition must satisfy a special format. We
strongly favor object reusage in Stratosphere and thus use a passthrough
return parameter. The first parameter of a UDF must be the previous
result of the function. Then there may be an arbitrary number of
parameters.
```java
public static IJsonNode sum(final IJsonNode oldResult, final IntNode node1, final IntNode node2) {
    IJsonNode result = oldResult;
    if(!(oldResult instanceof IntNode))
        result = new IntNode(node1.getIntValue() + node2.getIntValue());
    // reuse old result object
    else result.setValue(node1.getIntValue() + node2.getIntValue());
    return result;
}
```
As can be seen, all parameters must be subclasses of `IJsonNode`.
Overloaded methods are resolved at `runtime` by invoking the most
specific method.

Finally, there if the return type is always of the same type, the method
may also be shortened to

    public static void sum(final IntNode result, final IntNode node1, final IntNode node2) {
        result.setValue(node1.getIntValue() + node2.getIntValue());
    }
