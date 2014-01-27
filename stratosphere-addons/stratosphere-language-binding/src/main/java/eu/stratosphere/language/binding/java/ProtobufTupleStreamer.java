package eu.stratosphere.language.binding.java;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.nephele.jobmanager.JobManager;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.Value;
import eu.stratosphere.util.Collector;

/**
 * General class for streaming records fromthe java-language binding framework to a subprocess.
 * Implements the whole logic for calling the subprocess(python so far)
 * and setting up the connection and implements an open() and close() function.
 * 
 * Currently one must use streamSingleRecord() for Map-Operator and streamMultipleRecord for Reduce-Operator
 */
public class ProtobufTupleStreamer {

	// Signal that all records of a single map/reduce/... call are sent 
	public static final int SIGNAL_SINGLE_CALL_DONE = -1;
	// Signal that all map/reduce/... calls for one Operator in the plan are finished
	// and that the sub-process can terminate
	public static final int SIGNAL_ALL_CALLS_DONE = -2;
	
	private static final String[] ENV = { "PYTHONPATH=src/main/python/eu/stratosphere/language/binding/protos/:src/main/python/eu/stratosphere/language/binding/" };
	private final String pythonFilePath;
	private final int port;
	private final ArrayList<Class<?extends Value>> inputRecordClasses;
	private final ConnectionType connectionType;
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private Process pythonProcess;
	private ServerSocket serverSocket;
	private BufferedReader err;
	
	private static final Log LOG = LogFactory.getLog(JobManager.class);
	
	protected RecordReceiver receiver;
	protected RecordSender sender;
	
	public ProtobufTupleStreamer(String pythonFilePath, int port, ArrayList<Class<?extends Value>> outputRecordClasses,
			ConnectionType connectionType){
		this.pythonFilePath = pythonFilePath;
		this.port = port;
		this.inputRecordClasses = outputRecordClasses;
		this.connectionType = connectionType;
	}
	
	public ProtobufTupleStreamer(String pythonFilePath, ArrayList<Class<?extends Value>> outputRecordClasses,
			ConnectionType connectionType){
		this(pythonFilePath, -1, outputRecordClasses, connectionType);
	}
	
	public void open() throws Exception{
		if(port == -1){
			pythonProcess = Runtime.getRuntime().exec(pythonFilePath, ENV);
		}else{
			pythonProcess = Runtime.getRuntime().exec(pythonFilePath + " " + port, ENV);
		}
		err = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));
		LOG.debug("Proto-AbstractOperator - open() called");
		
		switch(connectionType){
		case STDPIPES:
			outputStream = pythonProcess.getOutputStream(); // this thing is buffered with 8k
			inputStream = pythonProcess.getInputStream(); // this thing is buffered with 8k
			LOG.debug("Proto-AbstractOperator - started connection via stdin/stdout");
			break;
		case SOCKETS:
			// Currently not in the python code
			serverSocket = new ServerSocket(port);
			Socket pythonSocket = serverSocket.accept();
			inputStream = pythonSocket.getInputStream();
			outputStream = pythonSocket.getOutputStream();
			LOG.debug("Proto-AbstractOperator - initialized connection over port " + port);
			break;
		default:
			throw new Exception("Currently not implemented connection type, use STDPIPES");
		}
		
		sender = new RecordSender(outputStream, inputRecordClasses);
		receiver = new RecordReceiver(inputStream);
	}
	
	public void close() throws Exception{
		LOG.debug("Proto-AbstractOperator - close() called");
		// Send signal to the python process that it is done
		sender.sendSize(SIGNAL_ALL_CALLS_DONE);
		
		String line;
		while ((line = err.readLine()) != null) {
			LOG.error("Python Error: "+line);
		}
		
		pythonProcess.destroy();
		if(connectionType == ConnectionType.SOCKETS){
			serverSocket.close();
		}
		inputStream.close();
		outputStream.close();
	}

	/**
	 * Sends a single record to the sub-process (without and signal afterwards) 
	 * and directly starts to receive records afterwards
	 */
	public void streamSingleRecord(Record record, Collector<Record> collector) throws Exception {
        sender.sendSingleRecord(record);
        receiver.receive(collector);
	}
	
	/**
	 * Sends multiple records to the sub-process. After all are send a signal is send to the sub-process
	 * and then it starts to receive records
	 */
    public void streamMultipleRecords(Iterator<Record> records, Collector<Record> collector) throws Exception {
        sender.sendAllRecords(records);
        receiver.receive(collector);
    }
}
