from ProtoUtils import *
import sys
import os

""" 
The current python implementation for streaming data into a map-operator. 
"""
class Mapper(object):
    
    def __init__(self, conn):
        self.__connectionType = conn
        self.f = open('pythonMapperOut.txt', 'w')

        # For sockets the same object should be used for iterator and collector,
        # therefore we are initializing a connection object here and give it to iterator and collector
        if conn == ConnectionType.SOCKETS:
            connection = SocketConnection("localhost", int(sys.argv[1]))
        elif conn == ConnectionType.STDPIPES:
            connection = STDPipeConnection()
        else:
            raise BaseException("Connectiontype which is not implemented so far")

            
        self.__iter = Iterator(self.f, connection)
        self.__collector = Collector(self.f, connection)
    
    def log(self, s):
        self.f.write(str(s) + "\n")
    
    """ 
    As long java hasn't send a signal that all map-functions are done(and hasMore() returns False
    always read a single record and call the mapfunction with it
    """    
    def map(self, mapFunc):
        while(self.__iter.hasMore()):
            record = self.__iter.readSingleRecord()
            if record != None:
                mapFunc(record, self.__collector)
            self.__collector.finish()
    