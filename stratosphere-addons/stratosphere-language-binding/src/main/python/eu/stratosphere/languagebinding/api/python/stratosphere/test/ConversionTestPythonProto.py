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
import sys

from stratosphere.proto import ProtoTuple_pb2
from stratosphere.connection import ProtoConversion
from stratosphere.connection import Connection


con = Connection.STDPipeConnection()

#test proto to python
tuple = ("test", True, 1, 1.2, con)

result = ProtoConversion.convert_python_to_proto(tuple)
if result.values[0].stringVal == "test":
    if result.values[1].boolVal:
        if result.values[2].int32Val == 1:
            print(result.values[3].floatVal)
            if result.values[3].floatVal == 1.2:
                if result.values[4].fieldType == ProtoTuple_pb2.ProtoTuple.String:
                    sys.stdout.write("1")
                    sys.stdout.flush()
sys.stdout.write("2")
sys.stdout.flush()