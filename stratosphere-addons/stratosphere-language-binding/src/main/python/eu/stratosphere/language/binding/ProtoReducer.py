from ProtoUtils import *
import sys
import os

""" 
    The current python implementation for streaming data into a map-operator. 
"""
class Reducer(object):
    
    def __init__(self, conn):
        self.__connectionType = conn
        self.f = open('pythonReducerOut.txt', 'w')
    
        # For sockets the same object should be used for iterator and collector,
        # therefore we are initializing a connection object here and give it to iterator and collector    
        if conn == ConnectionType.SOCKETS:
            connection = SocketConnection("localhost", int(sys.argv[1]))
        elif conn == ConnectionType.STDPIPES:
            connection = STDPipeConnection()
        else:
            raise BaseException("Connection-type which is not implemented so far")
        
        self.__iter = Iterator(self.f, connection)
        self.__collector = Collector(self.f, connection)
    
    def log(self, s):
        self.f.write(str(s) + "\n")
        
    def reduce(self, reduceFunc):
        while(self.__iter.hasMore()):
            reduceFunc(self.__iter, self.__collector)
            self.__collector.finish()
    