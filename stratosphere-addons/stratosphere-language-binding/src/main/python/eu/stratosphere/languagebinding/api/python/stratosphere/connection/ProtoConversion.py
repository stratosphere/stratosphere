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
import logging

from stratosphere.proto import ProtoTuple_pb2


#Converts a ProtoTuple to a tuple usable in python, or a single variable if atomicType flag is set.
def convert_proto_to_python(parsed_data):
    if parsed_data.atomicType:
        field = parsed_data.values[0]
        if field.fieldType == ProtoTuple_pb2.ProtoTuple.Boolean:
            return field.boolVal
        elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Byte:
            return field.int32Val
        elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Short:
            return field.int32Val
        elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Integer:
            return field.int32Val
        elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Long:
            return field.int64Val
        elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Float:
            return field.floatVal
        elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Double:
            return field.doubleVal
        elif field.fieldType == ProtoTuple_pb2.ProtoTuple.String:
            return field.stringVal
    else:
        result = ()
        for field in parsed_data.values:
            if field.fieldType == ProtoTuple_pb2.ProtoTuple.Boolean:
                result += (field.boolVal,)
            elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Byte:
                result += (field.int32Val,)
            elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Short:
                result += (field.int32Val,)
            elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Integer:
                result += (field.int32Val,)
            elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Long:
                result += (field.int64Val,)
            elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Float:
                result += (field.floatVal,)
            elif field.fieldType == ProtoTuple_pb2.ProtoTuple.Double:
                result += (field.doubleVal,)
            elif field.fieldType == ProtoTuple_pb2.ProtoTuple.String:
                result += (field.stringVal,)
        logging.debug("Conversion.protopy: result = %s", str(result))
        return result


#Converts python data into a ProtoTuple.
def convert_python_to_proto(data):
    result = ProtoTuple_pb2.ProtoTuple()
    result.atomicType = not isinstance(data, (list, tuple))
    if not isinstance(data, (list, tuple)):
        data = (data,)
    for value in data:
        field = result.values.add()
        if isinstance(value, basestring):
            logging.debug("Conversion.pytopro: adding string " + str(value))
            field.fieldType = ProtoTuple_pb2.ProtoTuple.String
            field.stringVal = value
        elif isinstance(value, bool):
            logging.debug("Conversion.pytopro: adding bool " + str(value))
            field.fieldType = ProtoTuple_pb2.ProtoTuple.Boolean
            field.boolVal = value
        elif isinstance(value, int):
            logging.debug("Conversion.pytopro: adding int " + str(value))
            field.fieldType = ProtoTuple_pb2.ProtoTuple.Integer
            field.int32Val = value
        elif isinstance(value, long):
            logging.debug("Conversion.pytopro: adding long " + str(value))
            field.fieldType = ProtoTuple_pb2.ProtoTuple.Long
            field.int64Val = value
        elif isinstance(value, float):
            logging.debug("Conversion.pytopro: adding float " + str(value))
            field.fieldType = ProtoTuple_pb2.ProtoTuple.Double
            field.floatVal = value
        else:
            logging.debug("Conversion.pytopro: adding object " + str(value))
            field.fieldType = ProtoTuple_pb2.ProtoTuple.String
            field.stringVal = str(value)
    return result
