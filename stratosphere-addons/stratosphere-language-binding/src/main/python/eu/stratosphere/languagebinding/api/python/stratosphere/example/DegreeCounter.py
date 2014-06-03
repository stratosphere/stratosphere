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
    other_vertices = []

    data = iterator.next()
    edge = Edge.Edge(data[0], data[1])

    group_vertex = edge.f0
    other_vertices.append(edge.f1)

    while iterator.has_next():
        data = iterator.next()
        edge = Edge.Edge(data[0], data[1])
        other_vertex = edge.f1

        contained = False
        for v in other_vertices:
            if v == other_vertex:
                contained = True
                break
        if not contained and not other_vertex == group_vertex:
            other_vertices.append(other_vertex)

    degree = len(other_vertices)

    for other_vertex in other_vertices:
        if group_vertex < other_vertex:
            output_edge = Edge.EdgeWithDegrees(group_vertex, degree, other_vertex, 0)
        else:
            output_edge = Edge.EdgeWithDegrees(other_vertex, 0, group_vertex, degree)
        coll.collect(output_edge)


GroupReducer.GroupReducer().group_reduce(function)