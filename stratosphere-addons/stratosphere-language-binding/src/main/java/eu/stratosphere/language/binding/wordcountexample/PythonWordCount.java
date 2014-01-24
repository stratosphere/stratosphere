package eu.stratosphere.language.binding.wordcountexample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.common.Program;
import eu.stratosphere.api.common.ProgramDescription;
import eu.stratosphere.api.common.operators.FileDataSink;
import eu.stratosphere.api.common.operators.FileDataSource;
import eu.stratosphere.api.java.record.functions.FunctionAnnotation.ConstantFields;
import eu.stratosphere.api.java.record.functions.MapFunction;
import eu.stratosphere.api.java.record.functions.ReduceFunction;
import eu.stratosphere.api.java.record.io.CsvOutputFormat;
import eu.stratosphere.api.java.record.io.TextInputFormat;
import eu.stratosphere.api.java.record.operators.MapOperator;
import eu.stratosphere.api.java.record.operators.ReduceOperator;
import eu.stratosphere.client.LocalExecutor;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.language.binding.java.ConnectionType;
import eu.stratosphere.language.binding.java.ProtobufTupleStreamer;
import eu.stratosphere.nephele.jobmanager.JobManager;
import eu.stratosphere.types.IntValue;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.types.Value;
import eu.stratosphere.util.Collector;
		

public class PythonWordCount implements Program, ProgramDescription {

	/**
	 * Converts a Record containing one string in to multiple string/integer
	 * pairs. The string is tokenized by whitespaces. For each token a new
	 * record is emitted, where the token is the first field and an Integer(1)
	 * is the second field.
	 */
	public static class TokenizeLine extends MapFunction implements
			Serializable {
		private static final long serialVersionUID = 1L;
		// Some so far hardcoded values, which will be replaced in the future
		// Just made them static that they are written in italic :D
		public static final int PORT = 8080;
		public static final String MAPSCRIPTPATH = "python src/main/python/eu/stratosphere/language/binding/wordcountexample/WordCountMapper.py";
		public static final Class<?>[] MAPCLASSES = { StringValue.class };
		
		private ProtobufTupleStreamer streamer;
		
		@SuppressWarnings("unchecked")
		public void open(Configuration parameters) throws Exception {
			super.open(parameters);
			ArrayList<Class<?extends Value>> classes = new ArrayList<Class<? extends Value>>();
			for(int i = 0; i < MAPCLASSES.length; i++){
				classes.add((Class<? extends Value>) MAPCLASSES[i]);
			}
			streamer = new ProtobufTupleStreamer(MAPSCRIPTPATH, classes, ConnectionType.STDPIPES);
			// Example for sockets:
			// mapper = new Mapper(MAPSCRIPTPATH, 8080, classes, ConnectionType.SOCKETS);
			streamer.open();
		}

		@Override
		public void close() throws Exception {
			streamer.close();
			super.close();
		}

		@Override
		public void map(Record record, Collector<Record> collector) throws Exception{
			streamer.streamSingleRecord(record, collector); 
		}
	}

	/**
	 * Sums up the counts for a certain given key. The counts are assumed to be
	 * at position <code>1</code> in the record. The other fields are not
	 * modified.
	 */
	//@Combinable
	@ConstantFields(0)
	public static class CountWords extends ReduceFunction implements
			Serializable {
		private static final long serialVersionUID = 1L;
		
		// Some so far hardcoded values, which will be replaced in the future
		// Just made them static that they are written in italic :D
		public static final int PORT = 8081;
		public static final String REDUCESCRIPTPATH = "python src/main/python/eu/stratosphere/language/binding/wordcountexample/WordCountReducer.py";
		public static final Class<?>[] REDUCECLASSES = { StringValue.class, IntValue.class };
		
		private ProtobufTupleStreamer streamer;
		
		@SuppressWarnings("unchecked")
		@Override
		public void open(Configuration parameters) throws Exception {
			super.open(parameters);
			ArrayList<Class<?extends Value>> reduceClasses = new ArrayList<Class<? extends Value>>();
			for(int i = 0; i < REDUCECLASSES.length; i++){
				reduceClasses.add((Class<? extends Value>) REDUCECLASSES[i]);
			}
			streamer = new ProtobufTupleStreamer(REDUCESCRIPTPATH, reduceClasses, ConnectionType.STDPIPES);
			// Example for sockets:
			// reducer = new Reducer(REDUCESCRIPTPATH, 8081, reduceClasses, ConnectionType.SOCKETS);
			streamer.open();
		}

		@Override
		public void close() throws Exception {
			streamer.close();
			super.close();
		}

		@Override
		public void reduce(Iterator<Record> records, Collector<Record> out)
				throws Exception {
			streamer.streamMultipleRecords(records, out);
		}

		@Override
		public void combine(Iterator<Record> records, Collector<Record> out)
				throws Exception {
			// the logic is the same as in the reduce function, so simply call
			// the reduce method
			reduce(records, out);
		}
	}

	@Override
	public Plan getPlan(String... args) {
		// parse job parameters
		int numSubTasks = (args.length > 0 ? Integer.parseInt(args[0]) : 1);
		String dataInput = (args.length > 1 ? args[1] : "");
		String output = (args.length > 2 ? args[2] : "");

		FileDataSource source = new FileDataSource(new TextInputFormat(),
				dataInput, "Input Lines");
		MapOperator mapper = MapOperator.builder(new TokenizeLine())
				.input(source).name("Tokenize Lines")
				.build();
		ReduceOperator reducer = ReduceOperator
				.builder(CountWords.class, StringValue.class, 0).input(mapper)
				.name("Count Words").build();
		FileDataSink out = new FileDataSink(new CsvOutputFormat(), output,
				reducer, "Word Counts");
		CsvOutputFormat.configureRecordFormat(out).recordDelimiter('\n')
				.fieldDelimiter(' ').field(StringValue.class, 0)
				.field(IntValue.class, 1);

		Plan plan = new Plan(out, "WordCount Example");
		plan.setDefaultParallelism(numSubTasks);
		return plan;
	}

	@Override
	public String getDescription() {
		return "Parameters: [numSubStasks] [input] [output]";
	}
	
	private static final Log LOG = LogFactory.getLog(JobManager.class);
	
	public static void main(String[] args) throws Exception {
		PythonWordCount wc = new PythonWordCount();

		if (args.length < 3) {
			System.err.println(wc.getDescription());
			System.exit(1);
		}

		Plan plan = wc.getPlan(args);

		long ts1 = System.currentTimeMillis();
		LocalExecutor.execute(plan);
		LOG.debug("Needed: " + (System.currentTimeMillis() - ts1)/1000.0f + "s");
	}

}
