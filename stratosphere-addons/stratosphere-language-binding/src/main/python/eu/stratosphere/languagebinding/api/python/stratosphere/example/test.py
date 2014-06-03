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
from stratosphere.plan.Environment import get_environment
from stratosphere.plan.InputFormat import TextInputFormat
from stratosphere.plan.OutputFormat import PrintingOutputFormat
from stratosphere.plan.Environment import Types

env = get_environment()

data = env.create_input(TextInputFormat("/home/shiren/Documents/test.txt"))

data.map("src/main/python/eu/stratosphere/languagebinding/api/python/stratosphere/test/Map.py", Types.INT)\
    .output(PrintingOutputFormat())

"""
data2 = data.filter("test.py")

data3 = data.cogroup("test.py")

data4 = data2.map("test.py")

data.cogroup(data2).where(0).equal_to(1).using("path")

data3.output(CSVOutputFormat("/res.txt"))
"""
env.execute()
"""
print(data._id)
print(data2._id)
print(data3._id)
print(data4._id)
"""