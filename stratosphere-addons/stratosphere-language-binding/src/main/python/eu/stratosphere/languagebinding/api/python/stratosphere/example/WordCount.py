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
import os
env = get_environment()
stratosphere_path = os.getenv("STRATOSPHERE_ROOT_DIR")

data = env.create_input(TextInputFormat(stratosphere_path+"/resources/python/stratosphere/documentation/Functions.py"))


data.map("/example/basics/Map.py", Types.INT)\
    .reduce("/example/basics/Reduce.py")\
    .output(PrintingOutputFormat())

env.execute()