package eu.stratosphere.test.exampleRecordPrograms;

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.common.operators.CollectionDataSource;
import eu.stratosphere.api.common.operators.FileDataSink;
import eu.stratosphere.api.common.operators.util.SerializableIterator;
import eu.stratosphere.api.java.record.functions.JoinFunction;
import eu.stratosphere.api.java.record.io.CsvOutputFormat;
import eu.stratosphere.api.java.record.operators.JoinOperator;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.example.java.record.wordcount.WordCount;
import eu.stratosphere.types.IntValue;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * test the collection and iterator data input using Wordcount example
 *
 */
public class WordCountCollectionCase extends WordCountITCase {
	public static class Join extends JoinFunction {

        @Override
        public void join(Record value1, Record value2, Collector<Record> out) throws Exception {
            out.collect(new Record(value1.getField(1,StringValue.class),value2.getField(1, IntValue.class)));

        }
    }

	public static class SerializableIteratorTest extends SerializableIterator<List<Object>> {
	
		private static final long serialVersionUID = 1L;
			private String [] s = COUNTS.split("\n");
			private int pos = 0;


        public List<Object> next() {
            List<Object> tmp = new ArrayList<Object>();
            tmp.add(pos);
            tmp.add(s[pos++].split(" ")[0]);
            return tmp;
        }
        public boolean hasNext() {
            return pos < s.length;
        }
	}
	
	/**
	 * modify the input format from file into collection
	 */
	public class WordCountCollection extends WordCount {
		
		public Plan getPlan(String arg1, String arg2) {
			// parse job parameters
			int numSubTasks   = Integer.parseInt(arg1);
			String output    = arg2;
			

			List<Object> tmp= new ArrayList<Object>();
			int pos = 0;
            for (String s: COUNTS.split("\n")) {
                List<Object> tmpInner= new ArrayList<Object>();
                tmpInner.add(pos++);
                tmpInner.add(Integer.parseInt(s.split(" ")[1]));
                tmp.add(tmpInner);
			}
			
			//test serializable iterator input, the input record is {id, word}
			CollectionDataSource source = new CollectionDataSource(new SerializableIteratorTest());
            //test collection input, the input record is {id, count}
            CollectionDataSource source2 = new CollectionDataSource(tmp);



            JoinOperator join = JoinOperator.builder(Join.class, IntValue.class, 0, 0)
                    .input1(source).input2(source2).build();


			FileDataSink out = new FileDataSink(new CsvOutputFormat(), output, join, "Word Counts");
			CsvOutputFormat.configureRecordFormat(out)
				.recordDelimiter('\n')
				.fieldDelimiter(' ')
				.field(StringValue.class, 0)
                .field(IntValue.class, 1);
			
			Plan plan = new Plan(out, "WordCount Example");
			plan.setDefaultParallelism(numSubTasks);
			return plan;
		}
	}
	
	public WordCountCollectionCase(Configuration config) {
		super(config);
	}

	
	@Override
	protected Plan getTestJob() {
		WordCountCollection wc = new WordCountCollection();
		/*
		 * split the test sentence into an array
		 */
		return wc.getPlan(config.getString("WordCountTest#NumSubtasks", "1"),
				resultPath);
	}
	
	@Test
	public void TestArrayInputValidation() throws Exception {

        /*
        valid array input
         */
        try {
            CollectionDataSource source = new CollectionDataSource("a","b","c");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            CollectionDataSource source = new CollectionDataSource(new Object[][]{{1,"a"},{2,"b"},{3,"c"}});

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        /*
        invalid array input
         */
        try {
            CollectionDataSource source = new CollectionDataSource(1,"b","c");
            Assert.fail("input type is different");
        } catch (Exception e) {

        }

        try {
            CollectionDataSource source = new CollectionDataSource(new Object[][]{{1,"a"},{2,"b"},{3,4}});
            Assert.fail("input type is different");
        } catch (Exception e) {
        }

	}

    @Test
    public void TestCollectionInputValidation() throws Exception {
        /*
        valid collection input
         */
        try {
            List<Object> tmp= new ArrayList<Object>();
            for (int i = 0; i < 100; i++)
                tmp.add(i);
            CollectionDataSource source = new CollectionDataSource(tmp);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            List<Object> tmp= new ArrayList<Object>();
            for (int i = 0; i < 100; i++) {
                List<Object> inner = new ArrayList<Object>();
                inner.add(i);
                inner.add('a' + i);
                tmp.add(inner);
            }
            CollectionDataSource source = new CollectionDataSource(tmp);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        /*
        invalid collection input
         */
        try {
            List<Object> tmp= new ArrayList<Object>();
            for (int i = 0; i < 100; i++)
                tmp.add(i);
            tmp.add("a");
            CollectionDataSource source = new CollectionDataSource(tmp);
            Assert.fail("input type is different");
        } catch (Exception e) {
        }

        try {
            List<Object> tmp= new ArrayList<Object>();
            for (int i = 0; i < 100; i++) {
                List<Object> inner = new ArrayList<Object>();
                inner.add(i);
                inner.add('a' + i);
                tmp.add(inner);
            }
            List<Object> inner = new ArrayList<Object>();
            inner.add('a');
            inner.add('a');
            tmp.add(inner);
            CollectionDataSource source = new CollectionDataSource(tmp);
            Assert.fail("input type is different");
        } catch (Exception e) {
        }
    }
}
