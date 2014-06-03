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
from abc import ABCMeta


class _OutputFormat(object):
    __metaclass__ = ABCMeta

    def __init__(self):
        self._arguments = []
        self._identifier = None


class TextOutputFormat(_OutputFormat):
    def __init__(self, file_path):
        super(TextOutputFormat, self).__init__()
        self._identifier = "text"
        self._arguments += (file_path,)


class JDBCOutputFormat(_OutputFormat):
    def __init__(self, drivername, url, query, username=None, password=None, batch_interval=None):
        super(JDBCOutputFormat, self).__init__()
        self._identifier = "jdbc"
        self._arguments.append(drivername)
        self._arguments.append(url)
        self._arguments.append(query)
        if username is not None:
            self._arguments.append(username)
        if password is not None:
            self._arguments.append(password)
        if batch_interval is not None:
            self._arguments.append(batch_interval)


class CSVOutputFormat(_OutputFormat):
    def __init__(self, file_path):
        super(CSVOutputFormat, self).__init__()
        self._identifier = "csv"
        self._arguments += (file_path,)


class PrintingOutputFormat(_OutputFormat):
    def __init__(self):
        super(PrintingOutputFormat, self).__init__()
        self._identifier = "print"