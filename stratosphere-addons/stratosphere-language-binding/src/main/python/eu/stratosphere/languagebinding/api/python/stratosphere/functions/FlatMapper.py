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


class FlatMapper(Function.Function):
    def __init__(self):
        super(FlatMapper, self).__init__()

    def flat_map(self, flat_map_func):
        while True:
            data = self.iterator.next()
            logging.debug("FlatMapper: executing udf")
            flat_map_func(self, data, self.collector, self.context)
            logging.debug("FlatMapper: sending end signal")
            self.collector.send_signal(Iterator.ProtoIterator.ITERATOR_SIGNAL_DONE)
