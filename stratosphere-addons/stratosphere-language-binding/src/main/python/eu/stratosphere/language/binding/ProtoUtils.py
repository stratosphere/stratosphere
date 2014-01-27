#from enum import Enum
import sys
import socket
import google.protobuf.internal.decoder as decoder
import google.protobuf.internal.encoder as encoder
import stratosphereRecord_pb2

# define our own enum type, because enums are only supported in python 3.4
def enum(**enums):
    return type('Enum', (), enums)        
ConnectionType = enum(STDPIPES=1, PIPES=2, SOCKETS=3)

SIGNAL_SINGLE_CALL_DONE = 4294967295;
SIGNAL_ALL_CALLS_DONE = 4294967294;

class SocketConnection(object):
    def __init__(self, host, port):
        self.__sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.__sock.connect((host, port))
    
    def send(self, buffer):
        self.__sock.send(buffer)
        
    def receive(self, size):
        return self.__sock.recv(size)
        
class STDPipeConnection(object):
    def __init__(self):
        pass
    
    def send(self, buffer):
        sys.stdout.write(buffer)
        sys.stdout.flush()
        
    def receive(self, size):
        return sys.stdin.read(size)
        
"""
    Class for receiving protobuf records and parsing them into python tuple.
    There are two ways to use it: 
    - call readSingleRecord() to only read one record - only used for mapper
    - iterate over the object. This is used for the reducer, that way
      the user get's the iterator and can that way receive one record after the other 
      inside the UDF.
"""
class Iterator(object):
        
    def __init__(self, f, connection):
        self.finished = False
        self.f = f
        self.__connection = connection
        
    def log(self,s):
        self.f.write(str(s) + "\n")
        
    def hasMore(self):
        return not self.finished
    
    def __iter__(self): 
        while(True):
            size = self.readSize()
            # If we are done with this map/reduce/... record-collection we get a -1 from java
            if size == SIGNAL_SINGLE_CALL_DONE:
                break
            # If we get a -2 it means that the operator is done completely and we can close this process
            elif size == SIGNAL_ALL_CALLS_DONE:
                self.finished = True
                break
            # Otherwise we read the record, parse and yield it
            else:
                yield self.readRecord(size)
    
    # Function for reading a single record, should only be used by the Map-Operator
    def readSingleRecord(self):
        size = self.readSize()
        if size == SIGNAL_ALL_CALLS_DONE:
            self.finished = True
            return None
        else:
            return self.readRecord(size);
    
    # General help-function for reading and parsing records, without any signal handling 
    def readRecord(self, size):
        buf = self.__connection.receive(size)
        record = stratosphereRecord_pb2.ProtoStratosphereRecord()
        record.ParseFromString(buf)
        return self.getTuple(record)
    
    # Reads a size from the connection and returns it's value
    def readSize(self):
        size = stratosphereRecord_pb2.ProtoRecordSize()
        sizeBuf = self.__connection.receive(5)
        size.ParseFromString(sizeBuf)
        return size.value
    
    # Function which gets the ProtoStratosphereRecord (Record representation of the protocol buffer classes)
    # and returns the corresponding tuple
    def getTuple(self, record):
        tuple = ()
        for value in record.values:
            if(value.valueType == stratosphereRecord_pb2.ProtoStratosphereRecord.IntegerValue32):
                tuple += (value.int32Val,)
            elif(value.valueType == stratosphereRecord_pb2.ProtoStratosphereRecord.StringValue):
                tuple += (value.stringVal,)
            else:
                raise BaseException("A currently not implemented valueType")
        return tuple

"""
    Class for sending python tuples as protobuf records to the java process.
    A single tuple is sent via the collect() function. 
    When all records for a single map/reduce/... function are send the finish()
    function must be called to tell the java process that we're finished.
"""
class Collector(object):
        
    def __init__(self, f, connection):
        self.f = f
        self.__connection = connection

    def log(self,s):
        self.f.write(str(s) + "\n")
            
    def collect(self, tuple):
        
        result = self.getProtoRecord(tuple);
        recordBuf = result.SerializeToString()
        
        #encoder._EncodeVarint(sys.stdout.write, len(recordBuf))
        #sys.stdout.write(recordBuf)
        
        size = stratosphereRecord_pb2.ProtoRecordSize()
        size.value = len(recordBuf)
        self.__connection.send(size.SerializeToString() + recordBuf)

    def finish(self):
        size = stratosphereRecord_pb2.ProtoRecordSize()
        size.value = SIGNAL_ALL_CALLS_DONE
        self.__connection.send(size.SerializeToString())

    # Function which gets the a python tuple and returns a 
    # ProtoStratosphereRecord (Record representation of the protocol buffer classes)
    def getProtoRecord(self, tuple):
        result = stratosphereRecord_pb2.ProtoStratosphereRecord()
        for value in tuple:
            protoVal = result.values.add()
            if isinstance(value, str) or isinstance(value, unicode):
                protoVal.valueType = stratosphereRecord_pb2.ProtoStratosphereRecord.StringValue
                protoVal.stringVal = value
            elif isinstance(value, int):
                protoVal.valueType = stratosphereRecord_pb2.ProtoStratosphereRecord.IntegerValue32
                protoVal.int32Val = value
            else:
                raise BaseException("A currently not implemented valueType")
        return result