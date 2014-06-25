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
import logging
from abc import ABCMeta, abstractmethod


#Class to send or retrieve ProtoTuples via some medium.
class Connection(object):
    __metaclass__ = ABCMeta

    def __init__(self):
        logging.basicConfig(filename='python.log', level=logging.DEBUG)

    @abstractmethod
    def send(self, buffer):
        self.__send_func(buffer)

    @abstractmethod
    def receive(self, size):
        return self.__recv_func(size)


#Standard pipe connection.
class STDPipeConnection(Connection):
    def __init__(self):
        super(STDPipeConnection, self).__init__()

    def send(self, buffer):
        sys.stdout.write(buffer)
        sys.stdout.flush()

    def receive(self, size):
        return sys.stdin.read(size)