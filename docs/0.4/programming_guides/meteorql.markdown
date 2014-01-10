--- 
layout: inner_docs_v04
title: Meteor Query Language
sublinks:
  - {anchor: "server", title: "Server Setup"}
  - {anchor: "syntax", title: "General Syntax"}
  - {anchor: "core", title: "Core Operators"}
  - {anchor: "cleansing", title: "Cleansing Package"}
---

<section id="server">
Server Setup
========================
In this article, we describe the execution of a Meteor query. Meteor provides a standalone client that can be executed on a different computer than the actual Stratosphere cluster.

As a precondition, the Nephele Jobmanager and the Sopremo Server must run.

Starting Sopremo Server
------------------------

First of all, the Meteor client translates a query into a SopremoPlan. The plan is then send to the Sopremo server and executed. 

The Sopremo server must run on the same computer as the Nephele Jobmanager. 

To start the Sopremo server, first adjust the server address in the sopremo-user.xml configuration file in the conf folder.

	<property>
		<key>sopremo.rpc.address</key>
		<value>localhost</value>
	</property>

Then launch the server.

	$ ./bin/start-sopremo-server.sh

Currently, the server only executes one SopremoPlan at a time but that will be subject to change.

Executing the Script
------------------------

The script itself may be executed on an arbitrary computer. To execute a Meteor script, please store it in an external file. There is currently no support for an interactive shell.

	usage: meteor-client.sh <scripts>
		--configDir <config>        Uses the given configuration
		--port <port>               Uses the specified port
		--server <server>           Uses the specified server
		--updateTime <updateTime>   Checks with the given update time in ms for the current status
		--wait                      Waits until the script terminates on the server

The Meteor client first of all requires at least one script file to execute. Additionally, it needs the server address that can be specified in three ways.

- Manually specified with the --server option.
- Written in sopremo-user.xml that resides in the folder ../conf relative to the meteor-client.sh
- Written in another sopremo-user.xml. In that case, the configuration directory that contains that sopremo-user.xml must be specified.

Without additional parameters, the client exits immediately and prints out whether the script was successfully enqueued.

To monitor the progress of the script, users can add the --wait option that shows additional information about the job at the given updateTime (default 1000 ms).

	Submitted script meteor_test.script.
	Executing script meteor_test.script.............
	Executed in 7734 ms

Referenced Packages
------------------------

All packages that are used from Meteor scripts must either be in the classpath or in the current directory. To adjust the classpath, please edit the meteor-script.sh or invoke it with additional -cp options. 

For each package, the !Meteor/Sopremo duo checks whether there is a current version of that package on the server and transfers it when needed. Thus, custom Sopremo operators may be tested quite easily with Meteor.

<section id="syntax">
General Syntax
========================
This article briefly outlines how a meteor query looks and guides users to manage complex data analysis tasks.

A Basic Query
------------------------

Meteor is a query language specifically designed for complex data analysis tasks. Thus, the inputs and outputs of a query are usually files that reside within the hdfs. Meteor resembles Jaql but is at the same time simpler in the grammatical features and more powerful in the overall usage as we will show throughout this article.

The following Meteor query performs a selection of all students that are teen-aged and writes the result out to the HDFS.

	$students = read from 'hdfs://server/path/students.json';
	$teens = filter $students where (now()-$students.birthDay).year < 20;
	write $teens to 'hdfs://server/path/result.json'; 

In the first line, the data set that is read from the 'students.json' is associated with the variable '$students'. Variables always start with a '$' in Meteor. The input and output paths in meteor can be either specified in an absolute or in a relative fashion (in respect to the location of the script).

The next two lines filter the data by a boolean expression that only retains students whose age is less than 20. The boolean expression is parsed to an expression tree that evaluates all incoming tuples to a boolean value.

Finally, the results are written back to the HDFS. We assume that in most complex analysis workflows, the result set is too large to print it to the user in any meaningful way. Consequently, there is currently no options to directly inspect the result set but users will have to open the result files.

In addition to the already described usecase, where the data is stored within the hdfs, meteor also supports other filesystems. To show this case the example from above stays the same, only the data-read and -write steps are altered.

	$students = read from 'file:///home/user/students.json';
	$teens = filter $students where (now()-$students.birthDay).year < 20;
	write $teens to 'file:///home/user/result.json';

In this case the input and output files are stored in the users homedirectory.

Adding a Package
------------------------

One of the main design goals of the higher level language layer of Stratosphere is the support for a large variety of applications. As we neither want to nor can develop and maintain all possible operators, we encourage power users to develop their own operators.

In the following, we add a duplicate removal step to the script.

	using cleansing;
	$students = read from 'hdfs://server/path/students.json';
	$students = remove duplicates $students
		where average(levenshtein(name), dateSim(birthDay)) > 0.95
		retain maxDate(enrollmentDate);
	$teens = filter $students where (now()-$student.birthDay).year < 20;
	write $teens to 'hdfs://server/path/result.json'; 

The ''using'' commands looks for a sopremo-cleansing.jar in the current classpath or directory and makes all contained operators available for future usage. 

With ''remove duplicates'' we use and configure this operator that is contained in the cleansing package. The operator has two properties where and retain, which specify the similarity condition and the conflict resolution function.

Aside from operators, each Sopremo package may also contain functions and constants. For example, the cleansing package contains the ''levenshtein'' function that is used in the query.

Meteor Grammar
------------------------

Meteor differs quite heavily from most script languages because the syntax of operators are not defined statically. All operators follow the same syntax rules that are shown as an excerpt in the form of an EBNF.

	operator   ::= name + inputs? properties? ';'
	inputs     ::= (alias 'in')? variable (',' inputs)?
	properties ::= property properties?
	property   ::= property_name expression
	variable   ::= '$' name

When we apply this grammar rule to the ''remove duplicates'' operator, we extract the following fragments that have matching colors to the syntax rules.

	remove duplicates $students
		where average(levenshtein(name), dateSim(birthDay)) > 0.95
		retain maxDate(enrollmentDate);

The configuration of the operators resembles the message passing concepts of Smalltalk and similar programming languages. Operators are first-class citizens and they validate the property values by themselves. 

Mixing Packages
------------------------

There is no restriction in how many packages can be combined in one script. We expanded the running example to incorporate information extraction operators.

	using cleansing;
	using ie;
	
	$students = read from 'hdfs://server/path/students.json';
	$students = remove duplicates $students
		where average(levenshtein(name), dateSim(birthDay)) > 0.95
		retain maxDate(enrollmentDate);
	$teens = filter $students
		where (now()-$student.birthDay).year < 20;
	
	$articles = read from 'news.json';
	$articles = annotate sentences $articles
		using morphAdorner;
	$articles = annotate entities in $articles
		using type.person and regex 'names.txt';
	$peopleInNews = pivot $articles around $person = $article.annotations[*].entity
		into {
			name: $person,
			articles: $articles
		};
	
	$teensInNews = join $teen in $teens, $person in $peopleInNews
		where $teen.name == $person.name
		into {
			student: $teen,
			articles: $person.articles[*].url
		};
	
	write $teensInNews to 'hdfs://server/path/result.json'; 

The additional operators coexist with the data cleansing operators. In fact, both operators also coexist with the base operator package that is implicitly included in Meteor scripts.

When only one operator or function from a package is used, they can be directly referenced via namespacing. ''cleansing:remove duplicates'' has the same effect in this context even when ''using cleansing'' is omitted. Namespacing also helps when packages have conflicting operator names.

Functions
------------------------

Functions can be defined in three ways:
  * Packages may include functions
  * Sopremo functions may be directly defined in the script and basically act as macros
  * User-defined functions written in Java and imported with ''javaudf''

We already saw an example for the first function (''levenshtein'' in cleansing).

The next snippets show how the other two function types are defined.

	square = fn(x) x * x;
	sum_udf= javaudf('packageName.JavaClass.sum');

The corresponding Java definition must satisfy a special format. We strongly favor object reusage in Stratosphere and thus use a passthrough return parameter. The first parameter of a UDF must be the previous result of the function. Then there may be an arbitrary number of parameters.

	public static IJsonNode sum(final IJsonNode oldResult, final IntNode node1, final IntNode node2) {
		IJsonNode result = oldResult;
		if(!(oldResult instanceof IntNode))
			result = new IntNode(node1.getIntValue() + node2.getIntValue());
		// reuse old result object
		else result.setValue(node1.getIntValue() + node2.getIntValue());
		return result;
	}

As can be seen, all parameters must be subclasses of ''IJsonNode''. Overloaded methods are resolved at ''runtime'' by invoking the most specific method.

Finally, there if the return type is always of the same type, the method may also be shortened to

	public static void sum(final IntNode result, final IntNode node1, final IntNode node2) {
		result.setValue(node1.getIntValue() + node2.getIntValue());
	}

<section id="core">
Core Operators
======================
To process data meteor uses a whole set of operators. The following section covers the predefined ones within the base package that is included automatically in each script.

Base Operators
------------------------
In this section we describe the syntax and semantics of the base operators in Meteor by examples.

### Relational operators

#### filter
The filter operator filters his input by only retaining those elements where the given predicate evaluates to true. This operators semantic is equivalent to the WHERE-clause in SQL.

##### Syntax
	filter <input> where <predicate>;

input: an iterable set of objects of type T (e.g. array of T)

output: the filtered set of objects of type T (-> count(output) <= count(input)

The filter operator automatically binds an iteration variable that can be used in the \<predicate\>. By default this variable has the same name as the \<input\>, but can be renamed. To do so the \<input\> in the described syntax of the operator  has to be replaced with:
	\<iteration variable\> in \<input\>

predicate: an expression that evaluates to a boolean value, this predicate decides which elements of the input are retained.

##### Example
default iteration variable
	
	$students = read from 'hdfs://server/path/students.json';
	$teens = filter $students where (now()-$students.birthDay).year < 20;
	write $teens to 'hdfs://server/path/result.json';

renaming iteration variable

	$input = read from 'input.json';
	$result = filter $emp in $input where $emp.mgr or $emp.income > 30000;
	write $result to 'output.json';

#### transform
The transform operator allows the transformation of each element of an input. This operator takes a set of type T1-elements and transforms them, according to a given expression, to a set of type T2-elements. The SQL-equivalent for this operator is the SELECT-clause.

##### Syntax
	transform <input> into <transformation expression>;

input: a set of type T1-elements (e.g. array of T1)

output: a set of transformed elements, each element of this set is of type T2 (-> count(output) == count(input))

The transform operator automatically binds an iteration variable that can be used inside the \<transformation expression\>. By default this variable has the same name as the \<input\>, this means values from records can be accessed via $input.key and values from array via $input[index].

transformation expression: This expression defines the transformation. It describes how each element of the result is constructed from his corresponding input-element.

##### Example
	$input = read from 'input.json';
	$result = transform $input
		into {
			sum: $input.a + $input.b;
			first: $input[0]
		};
	write $result to 'output.json';

#### join
The join operator allows to join two or more input sets into one result-set. Although this operator only allows joins between two or more inputs, a self-join can simply be realized by specifying the same data-source as both inputs. The join condition between two inputs can be any expression that evaluates to a boolean value, when joining more than two inputs the condition is assumed to be a conjunction of such expressions. This operator supports multiple types of joins like natural, left- and right-outer and outer-joins. The semantic is equivalent to SQL's JOIN.

##### Syntax
	join <input 1>, <input 2> ...
		preserve? <input name> ... 
		where <join conditions>
		into <output expression>;

input: Two or more sets of objects

output: A set of the result of this join. The structure of the elements of this set is defined by the \<output expression\>.

join conditions: These expressions define whether a tuple of the input elements is part of the result or not. You can use all kinds of comparisons inside this expressions. 

output expression: This expression defines the structure of the elements of the result set. To minimize the copy&paste work when keeping all attributes of the input exactly the same in the output, $input.* can be used instead of copying all input-attributes expilicitly to the output.

preserve: If preserve is used for an input, all elements of the specified input will appear in the result set, whether they have found matching values or not. By using the preserve-option you are able to achieve the same semantics as the different OUTER JOIN options in SQL.

The join operator automatically binds the variable $input i to each element of \<input i\>. These variables can be used by both, the \<join conditions\> and the \<output expression\> to access the elements of each input. To rename the default-name of the variable $input i the \<input i\> in the described syntax has to be replaced with \<variable name\> in \<input i>.

##### Example

default variable names without preserve option
	
	$users = read from 'users.json';
	$pages = read from 'pages.json';
	$result = join $users, $pages
		where $users.id == $pages.userid
		into {
			$users.name,
			$pages.*
		};
	write $result to 'result.json';

this example renames all of the default variable names

	$users = read from 'users.json';
	$pages = read from 'pages.json';
	$result = join $u in $users, $p in $pages
		where $u.id == $p.userid
		into {
			$u.name,
			$p.*
		};
	write $result to 'result.json';

the next example makes use of the preserve option

	$users = read from 'users.json';
	$pages = read from 'pages.json';
	$result = join $u in $users, $p in $pages
		preserve $u
		where $u.id == $p.userid
		into {
			$u.name,
			$p.*
		};
	write $result to 'result.json';

to show that not only equi-joins are possible, the following join condition could be used

	...
	where $u.id < $p.userid
	...

#### group
The group operator groups the elements of one or more inputs on a grouping key into one output. The resulting output contains one item for each group. During the process of transforming a whole group into the result item aggregate functions like count() and sum() can be applied. If the group operator is specified with a single input, its semantic is equivalent to SQL's GROUP BY clause. 

##### Syntax
single input

	group <input> by <grouping expression> into <aggregation expression>;

input: a set of elements of type T (e.g. array of type T)

output: A set of the resulting groups, each group is represented by a single item of type T2. The structure of this items is defined by the \<aggregation expression\>.

grouping expression: This part is optional and defines the way of how to extract the grouping key from the elements of the input. The resulting key is used to group the elements together. If only one global aggregate should be generated as the result the described syntax can be modified like this example:
	
	group <input> into <aggregation expression>;

aggregation expression: This expression is evaluated for each group and results in an item of type T2. This expression can apply functions like count() or sum() to aggregate the elements of a group.

The group operator automatically binds a variable name. Both, \<grouping expression\> and \<aggregation expression\> can use this name (\<grouping expression\> -> elements of the input, \<aggregation expression\> -> a single group). By default this name is the same as the input, but as any variable in meteor operators, renaming is possible. To do so the \<input\> in the described syntax has to be replaced with \<variable name\> in \<input\>.

multiple inputs

	group <input 1> by <grouping expression 1>, <input 2> by <grouping expression 2> ... into <aggregation expression>;

##### Example
Single input -> single, global aggregate

	$employees = read from 'employees.json';
	$result = group $employees
		into count($employees);
	write $result to 'output.json';

Single input -> partition into several groups and apply a function per group

	$employees = read from 'employees.json';
	$result = group $employees
		by $employees.dept
		into {
			$employees[0].dept,
			total: sum($employees[*].income)
		};
	write $result to 'output.json';

Single input -> renaming default variable names, partition into several groups and apply a function per group

	$employees = read from 'employees.json';
	$result = group $e in $employees
		by $e.dept
		into {
			$e[0].dept,
			total: sum($e[*].income)
		};
	write $result to 'output.json';

Multiple inputs

	$employees = read from 'employees.json';
	$depts = read from 'departments.json';
	$result = group $es in $employees by $es.dept, $ds in $depts by $ds.did
		into {
			dept: $ds.did,
			deptName: $ds[0].name,
			emps: $es[*].id,
			numEmps: count($es)
		};
	write $result to 'output.json';

### Set operators

#### intersect
The intersect operator computes the intersection between two or more inputs.

##### Syntax
	intersect <input 1>, <input 2> ...;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in all input sets

##### Example
	$users1 = read from 'users1.json';
	$users2 = read from 'users2.json';
	$commonUsers = intersect $users1, $users2;
	write $commonUsers to 'commonUsers.json';

#### union
The union operator computes the set-based union of two or more inputs.

##### Syntax
	union <input 1>, <input 2> ...;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in at least one of the inputs

##### Example ===
	$users1 = read from 'users1.json';
	$users2 = read from 'users2.json';
	$allUsers = union $users1, $users2;
	write $allUsers to 'allUsers.json';

#### subtract
The substract operator computes the difference between two or more inputs. The result contains all elements of the first input that are not part of the other inputs.

##### Syntax
	subtract <input 1>, <input 2> ...;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in the first input but not in all others

##### Example
	$oldUsers = read from 'oldUsers.json';
	$currentUsers = read from 'currentUsers.json';
	$newUsers = subtract $currentUsers, $oldUsers;
	write $newUsers to 'newUsers.json';

#### union all
The union all operator computes the bag-based union of two or more inputs.

##### Syntax
	union all <input 1>, <input 2> ...;

input: two or more sets of elements of type T (e.g. array of type T)

output: a set of elements of type T that are contained in at least one of the inputs (may contain duplicates)

##### Example
	$users1 = read from 'users1.json';
	$users2 = read from 'users2.json';
	$allUsers = union all $users1, $users2;
	write $allUsers to 'allUsers.json';

<section id="cleansing">
Cleansing Package
======================

The cleansing package containes a set of operators that can be used to conveniently write cleansing tasks. Althought all of these tasks
could be solved only with the standard operators described above, using this package has some advantages.

- the scripts become much shorter because a single cleansing operator had to be expressed with several standard operators
- the scripts are much easier to understand because each cleansing operator has a clear purpose
- the writing effort is minimized

The operators within the cleansing package cover all steps of the standard cleansing pipeline.
	
scrubbing --> entity extraction --> duplicate detection --> fusion

Scrubbing
----------------------

The scrubbing operator is used to simply clean the input data from errors. These errors can be something like missing values in required fields, a wrong format of the data, wrong datatypes or unallowed values. For each field that should be scrubbed you can specify a set of rules, any not specified field of the input is copied unchanged to the output. These rules are then sequentialy (in the order of specification) applied to the records. If all rules hold on a specific record, this record is treated as clean. Should one rule for a specific field not hold, a so called fix is applied to the record (default: deletion of the whole record). After the first rule has failed, all subsequent rules for that field are not evaluated because it is assumed that the value of this field is now clean. In addition to rules and fixes, you can also specify certain function within the sequence of rule-evaluations. This functions are simply executed on the data.
he following example shows the scrubbing operator:

	$scrubbed = scrub $companies with rules {
		id: required,
		founded: [required, hasPattern("\d{4}")],
		name: required ?: default(""),
		type: [required, notContainedIn([""])] ?: default("company"),
		form: [required, illegalCharacters("?!,.") ?: removeIllegalCharacters],
		category: [required, lower_case()]
	};

As you can see, there are several ways of specifying rules and there corresponding fixes. To define a fix for one or more rules
you have to use "?:". It simply means that if the rule holds than do nothing with that record, otherwise try to fix it. Another feature of the scrubbing operator is the ability to specify a single fix for several rules (like line 5 in the previous example). Line 7 in the example shows the usage of a function inside the rule evaluation sequence. After its sure that the category is present, the value is converted to lower case.
The following list shows all rules (and corresponding fixes) actually implemented:

- required: makes sure the field is not set to null
- hasPattern(regex): makes sure that the value of the field matches the given regex
- illegalCharacters(string): makes sure that none of the characters inside the given string is contained in the value of the field
	* fix: removeIllegalCharacters: removes all rule violating characters from the value
- range(start, end): makes sure that the value of the field is between (inclusive) start and end. It is assumed that the values have an ordering in respect to .compareTo().
	* fix: chooseNearestBound: replaces the value with either start or end, whichever is nearest
- containedIn(allowed_values): makes sure that the field only containes values specified in allowed_values
	* fix: chooseFirst: replaces the value with the first element of allowed_values
- notContainedIn(illegal_values): makes sure the field doesn't contain any of the values in illegal_values

In addition to these rules and fixes, there is a common fix that can be applied to any rule.

- default(default_value): replaces the value of the field with the given default_value 

The following example is a little more complex than the previous one, but shows many of the described features of the scrubbing operator:

	$companies_scrubbed = scrub $companies_preparedForScrubbing with rules {
		_id: [required, concat_strings("_L")],
		id: required,
		founded: [required, hasPattern("\d{4}")],
		subsidiaryCompanies: [required?: default([]), deempty()],
		parentCompany: required?: default(""),
		name: [required, notContainedIn([""])],
		industry: required?: default(""),
		locations: required?: default([]),
		companyType: required?: default(""),
	
		companyName: [required, illegalCharacters("\'")?:removeIllegalCharacters],
		companyForm: [required?:default(""), lower_case(), illegalCharacters(".")?:removeIllegalCharacters],
		companyCategory: required?:default("company")
	};

Entity Extraction
----------------------

The entity extraction operator is able to create user defined dataobjects from existing data. It can merge the data from multiple datasources and is able to create multiple output schemata at once.

The following example shows this operator:

	$person, $legal_entities  = map entities from $employees, $companies where ($employees.company[1:1] == $companies.id[1:1]) as [
		group $employees by $employees.id into {
			name: $employees.name,
			worksFor: $companies.id
		},
		group $companies by $companies.id into {
			name: $companies.name,
			type: $companies.category
		}
	];

Each of the grouping operators produce the schemata of one of the defined outputs. Its important to know that the value of the grouping key of the grouping operator is used as the value for the id field of the resulting record. This id field is automatically created. The ordering of the grouping operators corresponse to the order of the output variables.

Duplicate Detection
----------------------

Fusion
----------------------

The fusion operator is responsable for "merging" several records that are marked as duplicates. Therefore the operator uses a set of resolutions that specify how the values of the fields of duplicates are merged. For each field that should be merged the given resolutions are applied in a sequencial order where each resolution merges the result of the previous one. Ideally, after the last resolution has been applied only one value remains. This value is then treated as the value of the merged duplicates. A warning will be shown if there are more than one value remaining, but the merged duplicate will nevertheless contain the result of the last resolution as the value (in this case: an array of the remaining values). If there is only one value remaining while there are still resolutions to apply, the sequence will be interupted and the remaining value is the one for the merged record. The following example shows the fusion operator in a simple scenario:

	$persons = fuse $persons_dup with resolutions {
		name: [mostFrequent, defaultResolution("default")],
		age: mergeDistinct
	};

With an input of [{"name": "P1", "age": 25},{"name": "P1", "age": 24},{"name": "P2", "age": 25}] the previous example yields to the following fused entity: {"name": "P1", "age": [25,24]}. Because not all datasources in a datacleansing environment are at the same level of trust, the fusion operator can take such relations into account. For this purpose the operator is able to handle weights, where each datasource has its own set of weights that define how trustworthy the whole source or specific datafields are. While processing the resolutions these weights are than taken into account to determine the merge result. The following example shows such weights:

	$persons = fuse $persons_dup
		with weights {
			A: 0.6,
			B : 0.7*{name: 0.8, age: 0.3}
		}
		with resolutions {
			name: [mostFrequent, defaultResolution("default")],			
			age: mergeDistinct
		};

As you can see, the datasource B has nested weights assigned to himself. In this example the datasource itself has a weight of 0.7, but when accessing the name field, the weights are multiplied. This yields to a weight for that field of 0.7 * 0.8 = 0.56. To allow the fusion operator to take these weights into account, each input record needs a field "_source" which points to that records origin. The previous example with an input of
[
	{"name": "P1", "age": 25, "_source": "A"},
	{"name": "P2", "age": 24, "_source": "B"}
]
would output the following merged record:
{"name": "P1", "age": [25,24]}
The name "P1" is choosen because both name values are equaly frequent in the input, but the name field of datasource A has a higher weight than the one from source B.

The following list contains all implemented resolutions:

- defaultResolution(default_value) --> the merged value is always the default_value
- mostFrequent --> chooses the value that occures the most
- mergeDistinct --> merges several values into a set of distinct values
- chooseRandom --> chooses one of the input values randomly as the merged value
