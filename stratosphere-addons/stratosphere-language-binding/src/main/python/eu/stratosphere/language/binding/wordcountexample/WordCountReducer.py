from ProtoUtils import ConnectionType
from ProtoReducer import Reducer
import sys

sys.stderr = open('pythonReducerError.txt', 'w')

def count(iter, collector):
    sum = 0
    element = None
    
    for val in iter:
        element = val
        sum += 1
        
    if(element != None):
        collector.collect((element[0], int(sum)))
    
Reducer(ConnectionType.STDPIPES).reduce(count)