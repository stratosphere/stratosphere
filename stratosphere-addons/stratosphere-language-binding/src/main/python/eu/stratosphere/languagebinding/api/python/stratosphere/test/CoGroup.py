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

from stratosphere.functions import CoGrouper


def function(self, iterator1, iterator2, coll, context):
    while iterator1.has_next():
        logging.debug("loop 1")
        base = iterator1.next()[1]
        logging.debug("udf(): base= %s", str(base))
        while iterator2.has_next():
            logging.debug("loop 2")
            coll.collect(base + iterator2.next()[1])


CoGrouper.CoGrouper().co_group(function)
