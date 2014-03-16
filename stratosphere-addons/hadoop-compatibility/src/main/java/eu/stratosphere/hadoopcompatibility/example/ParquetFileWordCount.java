package eu.stratosphere.hadoopcompatibility.example;
/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/

import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.common.operators.FileDataSink;
import eu.stratosphere.api.java.record.io.CsvOutputFormat;
import eu.stratosphere.api.java.record.operators.MapOperator;
import eu.stratosphere.api.java.record.operators.ReduceOperator;
import eu.stratosphere.client.LocalExecutor;
import eu.stratosphere.hadoopcompatibility.HadoopDataSource;
import eu.stratosphere.hadoopcompatibility.datatypes.parquet.DefaultParquetTypeConverter;
import eu.stratosphere.types.IntValue;
import eu.stratosphere.types.StringValue;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import parquet.hadoop.ParquetInputFormat;

import parquet.hadoop.example.GroupReadSupport;
import parquet.hadoop.mapred.DeprecatedParquetInputFormat;

public class ParquetFileWordCount extends WordCount {

    // A necessity for parquet in terms of schema.
    public static class ExampleGroupDeprecatedParquetInputFormat extends DeprecatedParquetInputFormat {

        public ExampleGroupDeprecatedParquetInputFormat(JobConf conf) {
            ParquetInputFormat.setReadSupportClass(conf, GroupReadSupport.class);
        }

        //This empty constructor is being used by Hadoop.
        public ExampleGroupDeprecatedParquetInputFormat() {

        }
    }

    @Override
    public Plan getPlan(String... args) {

        int numSubTasks = (args.length > 0 ? Integer.parseInt(args[0]) : 1);
        String dataInput = (args.length > 1 ? args[1] : "");
        String output = (args.length > 2 ? args[2] : "");

        Configuration configuration =new JobConf();
        HadoopDataSource source = new HadoopDataSource(new ExampleGroupDeprecatedParquetInputFormat((JobConf) configuration), (JobConf) configuration, "Input Lines", new DefaultParquetTypeConverter());
        ExampleGroupDeprecatedParquetInputFormat.addInputPath(source.getJobConf(), new Path(dataInput));

        MapOperator mapper = MapOperator.builder(new TokenizeLine())
                .input(source)
                .name("Tokenize Lines")
                .build();
        ReduceOperator reducer = ReduceOperator.builder(CountWords.class, StringValue.class, 0)
                .input(mapper)
                .name("Count Words")
                .build();
        FileDataSink out = new FileDataSink(new CsvOutputFormat(), output, reducer, "Word Counts");
        CsvOutputFormat.configureRecordFormat(out)
                .recordDelimiter('\n')
                .fieldDelimiter(' ')
                .field(StringValue.class, 0)
                .field(IntValue.class, 1);

        Plan plan = new Plan(out, "WordCount Example with a Parquet File as Input");
        plan.setDefaultParallelism(numSubTasks);
        return plan;
    }


    public static void main(String[] args) throws Exception {
        WordCount wc = new SequenceFileWordCount();

        if (args.length < 3) {
            System.err.println(wc.getDescription());
            System.exit(1);
        }

        Plan plan = wc.getPlan(args);
        LocalExecutor.execute(plan);
    }
}
