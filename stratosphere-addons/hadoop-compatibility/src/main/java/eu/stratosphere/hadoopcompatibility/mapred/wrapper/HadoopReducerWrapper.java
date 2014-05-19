/***********************************************************************************************************************
 * Copyright (C) 2010-2014 by the Stratosphere project (http://stratosphere.eu)
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

package eu.stratosphere.hadoopcompatibility.mapred.wrapper;

import eu.stratosphere.api.java.record.functions.ReduceFunction;
import eu.stratosphere.api.java.record.operators.ReduceOperator.Combinable;
import eu.stratosphere.hadoopcompatibility.mapred.utils.HadoopConfiguration;
import eu.stratosphere.hadoopcompatibility.mapred.record.datatypes.DefaultStratosphereTypeConverter;
import eu.stratosphere.hadoopcompatibility.mapred.record.datatypes.StratosphereTypeConverter;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.DefaultHadoopOutput;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.DummyHadoopReporter;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.HadoopOutputWrapper;
import eu.stratosphere.types.Record;
import eu.stratosphere.util.Collector;
import eu.stratosphere.util.InstantiationUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.ReflectionUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Combinable
public class HadoopReducerWrapper<KEYIN extends WritableComparable,VALUEIN extends Writable,
		KEYOUT extends WritableComparable, VALUEOUT extends Writable> extends ReduceFunction  implements Serializable{

	private static final long serialVersionUID = 1L;

	private JobConf jobConf;
	private Reducer reducer;
	private String reducerName;
	private StratosphereTypeConverter<KEYIN, VALUEIN> stratosphereConverter;
	private HadoopOutputWrapper<KEYOUT,VALUEOUT,Record> output;
	private Reporter reporter;

	@SuppressWarnings("unchecked")
	public HadoopReducerWrapper(JobConf jobConf) {
		this(jobConf, new DefaultStratosphereTypeConverter(Text.class, LongWritable.class),
				new DefaultHadoopOutput<KEYOUT,VALUEOUT>(), new DummyHadoopReporter());
	}

	public HadoopReducerWrapper(JobConf jobConf, StratosphereTypeConverter<KEYIN, VALUEIN> stratosphereConverter,
								HadoopOutputWrapper<KEYOUT,VALUEOUT,Record> output, Reporter reporter) {
		this.jobConf = jobConf;
		this.reducer = InstantiationUtil.instantiate(jobConf.getReducerClass());
		this.reducerName = reducer.getClass().getName();
		this.stratosphereConverter = stratosphereConverter;
		this.output = output;
		this.reporter = reporter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void reduce(Iterator<Record> records, Collector<Record> out) throws Exception {
		output.wrapStratosphereCollector(out);
		KEYIN key = null;
		List<VALUEIN> values = new ArrayList<VALUEIN>();
		while (records.hasNext()) {
			Record rec = records.next();
			if (key == null) {
				key = stratosphereConverter.convertKey(rec);
			}
			values.add(stratosphereConverter.convertValue(rec));
		}
		reducer.reduce(key, values.iterator(), output, reporter);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeUTF(reducerName);

		jobConf.write(out);
		out.writeObject(stratosphereConverter);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		reducerName = in.readUTF();
		if(jobConf == null) {
			jobConf = new JobConf();
		}
		jobConf.readFields(in);
		try {
			this.reducer = (Reducer) Class.forName(this.reducerName).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Unable to instantiate the hadoop reducer", e);
		}
		ReflectionUtils.setConf(reducer, jobConf);
		stratosphereConverter = (StratosphereTypeConverter<KEYIN, VALUEIN>) in.readObject();
		output = InstantiationUtil.instantiate(HadoopConfiguration.getOutputCollectorFromConf(jobConf));
		reporter = InstantiationUtil.instantiate(HadoopConfiguration.getReporterFromConf(jobConf));
	}
}
