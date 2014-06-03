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

from stratosphere.functions import Reducer
from stratosphere.example import Edge


def add(self, input1, input2, context):
    edge1 = Edge.EdgeWithDegrees(input1[0], input1[1], input1[2], input1[3])
    edge2 = Edge.EdgeWithDegrees(input2[0], input2[1], input2[2], input2[3])

    out_edge = Edge.EdgeWithDegrees(edge1.f0, edge1.f1, edge1.f2, edge1.f3)
    if edge1.f1 == 0 and (not edge1.f3 == 0):
        out_edge.f1 = edge2.f1
    else:
        if (not edge1.f1 == 0) and edge1.f3 == 0:
            out_edge.f1 = edge2.f3
    return out_edge


Reducer.Reducer().reduce(add)