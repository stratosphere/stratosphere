--- 
layout: inner_docs
title: Meteor Query Language
sublinks:
  - {anchor: "input", title: "Reading Data"}
  - {anchor: "operators", title: "Basic Meteor-Scripts"}
  - {anchor: "cleansing", title: "The Cleansing Package"}
---

<!--
some kind of general documentation
what meteor is good in and so on
-->

<section id="input">
Reading Data
======================
To process specific data within a script, you first need to read this data. This step can be achieved by using the
read-command. The general syntax is as follows $variable = read from 'path/to/data';
The following example illustrates how to read some company-data from a json-file:

	$companies = read from '../../resources/companies.json';

The path to the datafile can either be specified in absolute or in a relative fashion (to the location of the script).
The read-command automatically determines the dataformat by examining the fileending. However, you can specify a concrete
format by using <!-- Alternative einfügen --> instead. The following dataformats are supported by meteor:

<!--
json
???
-->

<section id="operators">
Meteor-Operators
======================

To process data meteor uses a whole set of operators. The following section covers the predefined ones within the standard package.
For simple tasks, this set should be sufficient. However, for more complex operations you need to be able to write your own,
usecase specific operators, how that is achieved is described in the next section.

General
------------------------
Each operator consists of a keyword that specifies the sopremo-operator that is used as the implementation and an arbitrary number of
properties.
To allow a more convenient way of writing meteor-scripts, every operator allows the renaming of the variables inside his own scope. The
following example shows this:

	$variable = transform $v in $unconvenient_variable_name into {
		id: $v.id					
	};

Classic Operators
------------------------

### Projection

The Projection-Operator works similar to the projection in SQL. The general script-syntax looks like this:

	$variable = transform $source_data into {
		field_1: $source_data.field1,
		field_2: $source_data.field2
	};

This operator simply transforms the data stored in the input variable in respect to the provided transformation specifications.
For a more convenient way of specifying this transformations, there are several ways of minimizing the writing effort:

- copy one field from the input directly to the output:
	some_field: $input.some_field --> $input.some_field
- copy all fields from the input directly to the output:
	some_field: $input.some_field,
	another_field: $input.another_field, --> $input.*
	last_field: $input.last_field

To allow more complex operations than just simply copying the input data, functions can be applied during the transfomation.

	transformed_field: some_function($data.field)

For a list of possible function see <!-- CoreFunctions -->.

### Selection

This operator is similar to the Select-Where statement in SQL. It simply filters all records that match the given conditions.
The following example illustrates that by filtering all companies that are founded before the year 2000:

	$young_companies = filter $companies where $companies.founded < 2000;

To allow multiple conditions to be checked via a single selection operator, an arbitrary number of conditions can be combined with "and" and "or".

<!-- Hier weiter wie in der alten Doku -->

<section id="cleansing">
The Cleansing Package
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

1	$scrubbed = scrub $companies with rules {
2		id: required,
3		founded: [required, hasPattern("\d{4}")],
4		name: required ?: default(""),
5		type: [required, notContainedIn([""])] ?: default("company"),
6		form: [required, illegalCharacters("?!,.") ?: removeIllegalCharacters],
7		category: [required, lower_case()]
8	};

As you can see, there are several ways of specifying rules and there corresponding fixes. To define a fix for one or more rules
you have to use "?:". It simply means that if the rule holds than do nothing with that record, otherwise try to fix it. Another feature of the scrubbing operator is the ability to specify a single fix for several rules (like line 5 in the previous example). Line 7 in the example shows the usage of a function inside the rule evaluation sequence. After its sure that the category is present, the value is converted to lower case.
The following list shows all rules (and corresponding fixes) actually implemented:

	- required: makes sure the field is not set to null
	- hasPattern(regex): makes sure that the value of the field matches the given regex
	- illegalCharacters(string): makes sure that none of the characters inside the given string is contained in the value of the field
		> fix: removeIllegalCharacters: removes all rule violating characters from the value
	- range(start, end): makes sure that the value of the field is between (inclusive) start and end. It is assumed that the values have 		an ordering in respect to .compareTo().
		> fix: chooseNearestBound: replaces the value with either start or end, whichever is nearest
	- containedIn(allowed_values): makes sure that the field only containes values specified in allowed_values
		> fix: chooseFirst: replaces the value with the first element of allowed_values
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

For any informations regarding the rules and fixes, see <!-- javadoc -->

<!-- HOWTO selber Regeln schreiben -->

Entity Extraction
----------------------

The entity extraction operator is able to create user defined dataobjects from existing data. It can merge the data from multiple datasources and is able to create multiple output schemata at once.

???

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

<!-- HOWTO selber Resolutions schreiben -->
