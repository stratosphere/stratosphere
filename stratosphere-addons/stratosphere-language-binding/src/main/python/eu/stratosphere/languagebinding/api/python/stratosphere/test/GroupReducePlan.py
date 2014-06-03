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
from stratosphere.plan.InputFormat import CSVInputFormat
from stratosphere.plan.OutputFormat import PrintingOutputFormat
from stratosphere.plan.Environment import Types
from stratosphere.plan.Environment import Order

env = get_environment()

data = env.create_input(CSVInputFormat("/home/shiren/grtuples.txt", [Types.INT, Types.STRING]))

data\
    .group_by(0)\
    .sortgroup(0, Order.ASCENDING)\
    .groupreduce("src/main/python/eu/stratosphere/languagebinding/api/python/stratosphere/test/GroupReduce.py", Types.STRING)\
    .output(PrintingOutputFormat())

env.execute();