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


class Edge(object):
    def __init__(self, int1, int2):
        self.f0 = int1
        self.f1 = int2

    def __str__(self):
        return str(self.f0) + "," + str(self.f1)

    def flip(self):
        self.f0, self.f1 = self.f1, self.f0


class Triad(object):
    def __init__(self, int1, int2, int3):
        self.f0 = int1
        self.f1 = int2
        self.f2 = int3

    def __str__(self):
        return str(self.f0) + "," + str(self.f1) + "," + str(self.f2)


class EdgeWithDegrees(object):
    def __init__(self, int1, int2, int3, int4):
        self.f0 = int1
        self.f1 = int2
        self.f2 = int3
        self.f3 = int4

    def __str__(self):
        return str(self.f0) + "," + str(self.f1) + "," + str(self.f2) + "," + str(self.f3)