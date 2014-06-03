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
from stratosphere.connection import Connection
from stratosphere.connection import Iterator
from stratosphere.connection import Collector
import logging


def get_environment():
    return Environment()


class Switch(object):
    def __init__(self, value):
        self.value = value
        self.fall = False

    def __iter__(self):
        yield self.match
        raise StopIteration

    def match(self, *args):
        if self.fall or not args:
            return True
        elif self.value in args:
            self.fall = True
            return True
        else:
            return False


class Environment(object):
    def __init__(self):
        self._connection = Connection.STDPipeConnection()
        self._iterator = Iterator.ProtoIterator(self._connection, Iterator.ProtoIterator.ITERATOR_MODE_DEF)
        self._collector = Collector.ProtoCollector(self._connection)

        self._dop = None
        self._sources = []
        self._cached_files = []
        self._counter = 0

    def execute(self):
        self._send_plan()
        self._serve()

    def create_input(self, format):
        new_set = DataSet(self)
        self._sources.append((format, new_set))
        return new_set

    #def register_cached_file(self, name, path):
    #   self._cached_files.append((name, path))

    def set_degree_of_parallelism(self, degree):
        self._dop = degree

    def _serve(self):
        #TODO request handler, e.g for accumulator merge
        pass

    def _send_plan(self):
        self._send_environment()
        self._send_sources()
        self._collector.collect(len(self._sources))
        for source in self._sources:
            self._send_set(source)

    def _send_environment(self):
        self._send_parameters()
        self._send_cached_files()

    def _send_parameters(self):
        if self._dop is not None:
            self._collector.collect(self._dop)
        else:
            self._collector.send_signal(-1)

    def _send_cached_files(self):
        for file in self._cached_files:
            name = file[0]
            path = file[1]
            self._collector.collect(name)
            self._collector.collect(path)
        self._collector.send_signal(-1)

    def _send_sources(self):
        logging.debug("_send_sources")
        for source in self._sources:
            format = source[0]
            set = source[1]
            self._collector.collect(set._id)
            self._collector.collect(format._identifier)
            self._collector.collect(format._arguments)
        self._collector.send_signal(-1)

    def _send_set(self, parent):
        logging.debug("_send_set_sinks")
        self._send_sinks(parent[1])
        logging.debug("_send_set_childs")
        self._send_child_sets(parent[1]._id, parent[1]._child_sets)
        logging.debug("_send_set_rec")
        self._collector.collect(len(parent[1]._child_sets))
        for child in parent[1]._child_sets:
            self._send_set(child)

    def _send_child_sets(self, parent_id, child_sets):
        logging.debug("_send_child_sets_"+str(parent_id)+"|"+str(len(child_sets)))
        for child in child_sets:
            identifier = child[0]
            self._collector.collect(identifier)
            for case in Switch(identifier):
                if case("sort"):
                    set = child[1]
                    field = child[2]
                    order = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(field)
                    self._collector.collect(order)
                    break
                if case("groupby"):
                    set = child[1]
                    keys = child[2]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(keys)
                    break
                if case("cogroup"):
                    set = child[1]
                    other_set = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    self._collector.collect(types)
                    self._collector.collect(set._parameters)
                    break
                if case("cross"):
                    set = child[1]
                    other_set = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    self._collector.collect(types)
                    self._collector.collect(set._parameters)
                    break
                if case("cross_h"):
                    set = child[1]
                    other_set = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    self._collector.collect(types)
                    self._collector.collect(set._parameters)
                    break
                if case("cross_t"):
                    set = child[1]
                    other_set = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    self._collector.collect(types)
                    self._collector.collect(set._parameters)
                    break
                if case("join"):
                    set = child[1]
                    other_set = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    self._collector.collect(types)
                    self._collector.collect(set._parameters)
                    break
                if case("join_h"):
                    set = child[1]
                    other_set = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    self._collector.collect(types)
                    self._collector.collect(set._parameters)
                    break
                if case("join_t"):
                    set = child[1]
                    other_set = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    self._collector.collect(types)
                    self._collector.collect(set._parameters)
                    break
                if case("union"):
                    set = child[1]
                    other_set = child[2]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(other_set._id)
                    break
                if case("filter"):
                    set = child[1]
                    script = child[2]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(script)
                    break
                if case("reduce"):
                    set = child[1]
                    script = child[2]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(script)
                    break
                if case():
                    set = child[1]
                    script = child[2]
                    types = child[3]
                    self._collector.collect(parent_id)
                    self._collector.collect(set._id)
                    self._collector.collect(script)
                    self._collector.collect(types)
                    break
        self._collector.send_signal(-1)

    def _send_sinks(self, set):
        for sink in set._sinks:
            format = sink[0]
            set = sink[1]
            self._collector.collect(set._id)
            self._collector.collect(format._identifier)
            self._collector.collect(format._arguments)
        self._collector.send_signal(-1)


class Set(object):
    def __init__(self, env):
        self._child_sets = []
        self._sinks = []
        self._specials = []
        self._parameters = []
        self._env = env
        self._id = env._counter
        env._counter += 1

    def groupreduce(self, script_name, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("groupreduce", new_set, script_name, types))
        return new_set


class ReduceSet(Set):
    def __init__(self, env):
        super(ReduceSet, self).__init__(env)

    def reduce(self, script_name):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("reduce", new_set, script_name))
        return new_set


class DataSet(ReduceSet):
    def __init__(self, env):
        super(DataSet, self).__init__(env)

    def output(self, format):
        self._sinks.append((format, self))

    def group_by(self, keys):
        new_set = UnsortedGrouping(self._env)
        if not isinstance(keys, (list, tuple)):
            self._child_sets.append(("groupby", new_set, (keys,)))
        else:
            self._child_sets.append(("groupby", new_set, keys))
        return new_set

    def cogroup(self, other_set, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("cogroup", new_set, other_set, types))
        return StepWhere(new_set)

    def cross(self, other_set, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("cross", new_set, other_set, types))
        return StepUsing(new_set)

    def cross_with_huge(self, other_set, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("cross_h", new_set, other_set, types))
        return StepUsing(new_set)

    def cross_with_tiny(self, other_set, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("cross_t", new_set, other_set, types))
        return StepUsing(new_set)

    def filter(self, script_name):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("filter", new_set, script_name))
        return new_set

    def flatmap(self, script_name, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("flatmap", new_set, script_name, types))
        return new_set

    def join(self, other_set, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("join", new_set, other_set, types))
        return StepWhere(new_set)

    def join_with_huge(self, other_set, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("join_h", new_set, other_set, types))
        return StepWhere(new_set)

    def join_with_tiny(self, other_set, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("join_t", new_set, other_set, types))
        return StepWhere(new_set)

    def map(self, script_name, types):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("map", new_set, script_name, types))
        return new_set

    def union(self, other_set):
        new_set = OperatorSet(self._env)
        self._child_sets.append(("union", new_set, other_set))
        return new_set


class OperatorSet(DataSet):
    def __init__(self, env):
        super(OperatorSet, self).__init__(env)

    def with_accumulator(self, name, acc):
        self._specials.append(("accumulator", name, acc))
        return self

    def with_broadcast(self, name, set):
        self._specials.append(("broadcast", name, set))


class UnsortedGrouping(ReduceSet):
    def __init__(self, env):
        super(UnsortedGrouping, self).__init__(env)

    def sortgroup(self, field, order):
        new_set = SortedGrouping(self._env)
        self._child_sets.append(("sort", new_set, field, order))
        return new_set


class SortedGrouping(Set):
    def __init__(self,env):
        super(SortedGrouping, self).__init__(env)

    def sortgroup(self, field, order):
        new_set = SortedGrouping(self._env)
        self._child_sets.append(("sort", new_set, field, order))
        return new_set


class StepWhere:
    def __init__(self, set):
        self._set = set

    def where(self, fields):
        if not isinstance(fields, (list, tuple)):
            self._set._parameters += (fields,)
        else:
            self._set._parameters += fields
        return StepEqualTo(self._set)


class StepEqualTo:
    def __init__(self, set):
        self._set = set

    def equal_to(self, fields):
        if not isinstance(fields, (list, tuple)):
            self._set._parameters += (fields,)
        else:
            self._set._parameters += fields
        return StepUsing(self._set)


class StepUsing:
    def __init__(self, set):
        self._set = set

    def using(self, path):
        self._set._parameters.append(path)
        return self._set


class Order(object):
    NONE = 0
    ASCENDING = 1
    DESCENDING = 2
    ANY = 3

class Types(object):
    BOOL = True
    INT = 1
    LONG = 1L
    FLOAT = 2.5
    STRING = "type"