package eu.stratosphere.example.java.record.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.common.Program;
import eu.stratosphere.api.common.operators.CollectionDataSource;
import eu.stratosphere.api.common.operators.FileDataSink;
import eu.stratosphere.api.common.operators.GenericDataSource;
import eu.stratosphere.api.java.record.io.CsvOutputFormat;
import eu.stratosphere.api.java.record.io.FileOutputFormat;
import eu.stratosphere.api.java.record.io.GenericInputFormat;
import eu.stratosphere.client.LocalExecutor;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;


/**
 * A simple Stratosphere Job that generates input data for wordcount.
 *
 */
public class WordCountDataGenerator implements  Program {

	public static class GeneratingIterator extends GenericInputFormat {

		private Random rnd = new Random(1337);
		int limit = 0;
		StringValue line = new StringValue();
		
		public GeneratingIterator(int l) {
			this.limit = l;
		}
		@Override
		public boolean reachedEnd() throws IOException {
			return limit-- <= 0;
		}
		@Override
		public boolean nextRecord(Record record) throws IOException {
			int words = rnd.nextInt(50);
			StringBuffer r = new StringBuffer();
			for(int i = 0; i < words; i++) {
				r.append(RandomStringUtils.randomAlphabetic(rnd.nextInt(15)));
				r.append(" ");
			}
			line.setValue(r);
			record.setField(0, line);
			return true;
		}
		
	}
	@Override
	public Plan getPlan(String... args) {
		if(args.length < 2) {
			System.err.println("Wrong parameters. <outPath> <Lines per node> <Nodes>");
			System.exit(1);
		}
		String outPath = args[0];
		int l = Integer.parseInt(args[1]);
		int dop = Integer.parseInt(args[2]);
		
		GenericDataSource<GeneratingIterator> src = new GenericDataSource<GeneratingIterator>(new GeneratingIterator(l), "Generate");
		FileDataSink sink = new FileDataSink(new CsvOutputFormat(StringValue.class), outPath);
		sink.setInput(src);
		Plan p = new Plan(sink, "Generate Wordcount data");
		p.setDefaultParallelism(dop);
		return p;
	}
	public static void generateWCInput(String to) throws Exception {
		WordCountDataGenerator wcg = new WordCountDataGenerator();
		Plan p = wcg.getPlan(to, "100", "1");
		System.err.println("Generating wordcount input to "+to);
		LocalExecutor.execute(p);
	}

	public static void main(String[] args) throws Exception {
		String tmp = System.getProperty("java.io.tmpdir");
		String path = "file://"+tmp+"/wcinput";
		generateWCInput(path);
	}
}
