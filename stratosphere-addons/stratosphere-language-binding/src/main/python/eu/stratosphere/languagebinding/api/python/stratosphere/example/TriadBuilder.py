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

from stratosphere.functions import GroupReducer
from stratosphere.example import Edge


def function(self, iterator, coll, context):
    vertices = []

    y = iterator.next()
    first_edge = Edge.Edge(y[0], y[1])

    vertices.append(first_edge.f1)

    while iterator.has_next():
        x = iterator.next()
        second_edge = Edge.Edge(x[0], x[1])
        higher_vertex_id = second_edge.f1

        for lowerVertexId in vertices:
            coll.collect(Edge.Triad(first_edge.f0, lowerVertexId, higher_vertex_id))
        vertices.append(higher_vertex_id)


GroupReducer.GroupReducer().group_reduce(function)