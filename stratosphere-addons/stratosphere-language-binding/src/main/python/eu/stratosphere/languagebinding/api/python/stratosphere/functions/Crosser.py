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

from stratosphere.functions import Function


class Crosser(Function.Function):
    def __init__(self):
        super(Crosser, self).__init__()

    def cross(self, cross_func):
        while True:
            data1 = self.iterator.next()
            data2 = self.iterator.next()
            logging.debug("Crosser: executing udf")
            result = cross_func(self, data1, data2, self.context)
            logging.debug("Crosser: sending result")
            self.collector.collect(result)