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
from stratosphere.plan.Environment import get_environment
from stratosphere.plan.InputFormat import TextInputFormat
from stratosphere.plan.OutputFormat import PrintingOutputFormat
from stratosphere.plan.Environment import Types

#1)
env = get_environment()

#2)
data = env.create_input(TextInputFormat("/test.txt"))

#3)
mapped_data = data.map("/example/basics/Map.py", Types.INT)

#4)
mapped_data.output(PrintingOutputFormat())

#5)
env.execute()
#=======================================================================================================================
"""
This is a basic example showing a plan written completely in python.

General structure:
A python plan that is supposed to be used by a stratosphere generally consists of 5 parts:
1) the call to get_environment()
2) loading data using input formats
3) manipulating the data using functions
4) outputting data using output formats
5) execute the plan

The path to the scripts has to be relative to the package containing it.

Functions and formats may require you to specify the output type.
mapped_data = data.map("/stratosphere/test/Map.py", Types.INT)
    returns a set containing ints.
mapped_data = data.map("/stratosphere/test/Map.py", [Types.INT])
    returns a set of tuples containing a single int.
mapped_data = data.map("/stratosphere/test/Map.py", [Types.INT, Types.STRING])
    returns a set of tuples containing an int and a string

Whether this is necessary can be determined from the signature.

The following operations can be executed on the result from an inputformat or operation (unless otherwise stated):
    coGroup
    cross (-with_huge/with_tiny)
    filter
    flatMap
    groupReduce
    join (-with_huge/with_tiny)
    map
    reduce
    groupBy
    union
    sort
    output
The following operations can be executed on the result from a groupBy operation:
    groupReduce
    reduce
    sort
The following operations can be executed on the result from a sort operation:
    groupReduce
    sort

The python plans are structurally very similar to java plans, as such refer to the official documentation at
https://www.stratosphere.eu for more information.
Note that python plans are a subset functionality-wise.

You can submit a python plan by passing the stratosphere-language-binding jar, along with paths to your plan and the
package containing the functions to stratosphere.

Notes:
Python IDE's frequently encounter problems with auto-completion after join (or similar) operations. This will be fixed
in a future version.
"""
