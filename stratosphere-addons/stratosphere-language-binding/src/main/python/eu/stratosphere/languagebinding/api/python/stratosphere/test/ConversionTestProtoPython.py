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
tuple = ProtoTuple_pb2.ProtoTuple()
tuple.atomicType = False
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.String
field.stringVal = "test"
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.Boolean
field.boolVal = True
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.Byte
field.int32Val = 1
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.Short
field.int32Val = 2
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.Integer
field.int32Val = 3
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.Long
field.int64Val = 4
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.Float
field.floatVal = 1.1
field = tuple.values.add()
field.fieldType = ProtoTuple_pb2.ProtoTuple.Double
field.doubleVal = 1.2

result = ProtoConversion.convert_proto_to_python(tuple)
if result[0] == "test":
    print(1)
    if result[1]:
        print(2)
        if result[2] == 1:
            print(3)
            if result[3] == 2:
                print(4)
                if result[4] == 3:
                    print(5)
                    if result[5] == 4:
                        print(6)
                        if result[6] == 1.1:
                            print(7)
                            if result[7] == 1.2:
                                sys.stdout.write(1)
                                sys.stdout.flush()
sys.stdout.write(0)
sys.stdout.flush()
