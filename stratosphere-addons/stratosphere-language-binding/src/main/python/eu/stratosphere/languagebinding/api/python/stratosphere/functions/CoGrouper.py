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
from stratosphere.connection import Iterator


class CoGrouper(Function.Function):
    def __init__(self):
        super(CoGrouper, self).__init__(Iterator.ProtoIterator.ITERATOR_MODE_CG)
        self.dummy1 = Iterator.Dummy(self.iterator, 0)
        self.dummy2 = Iterator.Dummy(self.iterator, 1)

    def co_group(self, co_group_func):
        while True:
            logging.debug("CoGrouper: executing udf")
            co_group_func(self, self.dummy1, self.dummy2, self.collector, self.context)
            logging.debug("CoGrouper: sending end signal")
            self.collector.send_signal(Iterator.ProtoIterator.ITERATOR_SIGNAL_DONE)
            self.iterator.reset()