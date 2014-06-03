######################################################################################################################
# Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
# an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.
######################################################################################################################
#=======================================================================================================================
import re

#1)
from stratosphere.functions import Mapper


#2)
def function(self, input, env):
    split_input = re.split("\W+", input.lower())
    return len(split_input)

#3)
Mapper.Mapper().map(function)
#=======================================================================================================================
"""
This is a basic example showing a map function that counts the number of words in a string.

General structure:
A python function that is supposed to be used by a stratosphere plan consists of 3 parts:
1) an import of the corresponding operator class
2) an operator function
3) a call to the corresponding operator class

Basic examples for each function can be found in the test and example folder in the stratosphere package.
These show the function signatures, and how data can received and returned.

Arguments:
Iterator/collector:
Several functions receive iterators instead of simple objects, and collectors instead of having a return type.

Iterators support the following operations:
has_next(): returns a boolean value indicating whether another values is available. Note that for CoGroup functions,
the first call will always return true.
next(): returns the next value. This is a blocking operation.
all(): returns all values in the iterator as a list. This is a blocking operation.

Collectors support the following operations:
collect(): collects a single value.

Context:
The context argument will, in the future, allow access to certain stratosphere features like BroadcastVariables
and Accumulators.

Types:

If the plan was written in python:
The data input type for the function will be a basic python type (int, float, bool, string) or a tuple containing basic
python types.

If the plan was written in java:
The data input type for the function will be a basic type or a tuple containing basic types. Custom types are converted
to strings (if no converter was supplied), and (may) have to be reconverted in python.

Type(flag) conversion table (Java -> Python)
bool      -> bool
byte      -> int
short     -> int
int	  -> int
long      -> long
float     -> float
double    -> float
string    -> string
  ?       -> string
Tuple(x,y)-> (x,y)/[x,y]


type(flag) conversion table (Python -> Java)
bool       -> bool
int        -> int
int        -> int
int        -> int
long       -> long
float      -> float
float      -> float
string     -> string
  ?        -> string
(x,y)/[x,y]-> Tuple(x,y)
"""

