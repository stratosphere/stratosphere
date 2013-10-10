---
layout: documentation
---
Eclipse Warning Settings
========================

\*\*Code Style\*\*
------------------

Setting

Value

Comment

Non-static access to static member:

Warning

Indirect access to static member:

Warning

Unqualified access to instance field:

Warning

Undocumented empty block:

Ignore

Or Warning?

Access to a non-accessible member of an enclosing type:

Ignore

Method with a constructor name:

Error

Parameter assignment:

Ignore

Or Warning?

Non-externalized strings (missing/unused -NLS\$ tag):

Ignore

Potential programming problems

Serializable class without serialVersionUID:

Warning

Assignment has no effect (e.g. 'x = x'):

Warning

Possible accidental boolean assignment (e.g. if (a=b)):

Ignore

Or Warning?

'finally' does not complete normally:

Warning

Empty statement:

Ignore

Or Warning?

Using a char array in string concatenation:

Warning

Hidden catch block:

Warning

Inexact type match for vararg arguments:

Warning

Boxing and unboxing conversions:

Ignore

Or Warning for potential performance issues?

Enum type constant not covered on 'switch':

Ignore

'switch' case fall-through:

Ignore

Null reference:

Warning

Potential null pointer access:

Warning

Comparing identical values ('x == x'):

Warning

Missing synchronized modifier on inherited method:

Warning

Or is there any use case, where this is desired?

Class overrides 'equals()' but not 'hashCode()':

Warning

May be very helpful. Use eclipse generators and it is not really
annoying.

Dead code (e.g. 'if (false)':

Warning

Name shadowing and conflicts

Field declaration hides another field or variable:

Warning

Local variable declaration hides another field or variable:

Ignore

Include constructor or setter method parameters

Type parameter hides another type:

Warning

Method overridden but not package visible:

Warning

This might actually save quite much debug time for newbies. → Error?

Interface method conflicts with protected 'Object' method:

Warning

Deprecated and restricted API

Deprecated API:

Warning

Signal use of deprecated API inside deprecated code

Off

Signal overriding or implementing deprecated method

Off

Forbidden reference (access rules):

Error

Discouraged reference (access rules):

Warning

Unnecessary code

Local variable is never read:

Warning

Parameter is never read: Warning

More concise APIs in my experience

Ignore in overriding and implementing methods

On

Ignore parameters documented with '@param' tag

On

Unused import:

Warning

Unused local or private member:

Warning

Redundant null check:

Warning

Unnecessary else statement:

Warning

More consice code

Unnecessary cast or 'instanceof' operation:

Warning

Unnecessary declaration of thrown checked exception:

Warning

Ignore overriding and implementing methods

On

Ignore exceptions documented with '@throws' or '@exception' tags

On

Ignore 'Exception' and 'Throwable'

On

Unused break/continue label:

Warning

Redundant super interface:

Ignore

Generic types

Unchecked generic type operation

Warning

Usage of a raw type

Warning

Generic type parameter declared with a final type bound

Warning

Annotations

Missing '@Override' annotation

Warning

Include implementations of interface methods (1.6 or higher)

On

Missing '@Deprecated' annotation

Warning

Annotation is used as super interface

Warning

Unhandled token in '@!SuppressWarnings'

Warning

Enable '@!SuppressWarnings' annotations

On

Unused '@!SuppressWarnings' token

Warning

Suppress optional errors with '@!SuppressWarnings'

On

Javadoc
-------

<table>
<tbody>
<tr class="odd">
<td align="left">Setting</td>
<td align="left">Value</td>
<td align="left">Comment</td>
</tr>
<tr class="even">
<td align="left">Process Javadoc comments (includes search and refactoring)</td>
<td align="left">On</td>
<td align="left"></td>
</tr>
<tr class="odd">
<td align="left">Malformed Javadoc comments</td>
<td align="left">Protected</td>
<td align="left"></td>
</tr>
<tr class="even">
<td align="left">Only consider members as visible as</td>
<td align="left">Public</td>
<td align="left"></td>
</tr>
<tr class="odd">
<td align="left">Validate tag arguments (@param, @throws, @exception, @see, @link)</td>
<td align="left">Off</td>
<td align="left"></td>
</tr>
<tr class="even">
<td align="left">Report non visible references</td>
<td align="left">On</td>
<td align="left"></td>
</tr>
<tr class="odd">
<td align="left">Report deprecated references</td>
<td align="left">Off</td>
<td align="left"></td>
</tr>
<tr class="even">
<td align="left">Missing tag descriptions</td>
<td align="left">'Validate @return tags'</td>
<td align="left"></td>
</tr>
<tr class="odd">
<td align="left">Missing Javadoc tags</td>
<td align="left">Ignore</td>
<td align="left"></td>
</tr>
<tr class="even">
<td align="left">Only consider members as visible as</td>
<td align="left">Protected</td>
<td align="left"></td>
</tr>
<tr class="odd">
<td align="left">Check overriding and implementing methods</td>
<td align="left">On</td>
<td align="left"></td>
</tr>
<tr class="even">
<td align="left">Missing Javadoc comments</td>
<td align="left">Ignore</td>
<td align="left"></td>
</tr>
<tr class="odd">
<td align="left">Only consider members as visible as</td>
<td align="left">Protected</td>
<td align="left"></td>
</tr>
<tr class="even">
<td align="left">Check overriding and implementing methods</td>
<td align="left">On</td>
<td align="left"></td>
</tr>
</tbody>
</table>


