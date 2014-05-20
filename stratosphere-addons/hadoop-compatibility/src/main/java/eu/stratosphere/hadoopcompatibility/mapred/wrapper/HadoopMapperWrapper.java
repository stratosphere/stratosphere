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

import eu.stratosphere.api.java.record.functions.MapFunction;
import eu.stratosphere.hadoopcompatibility.mapred.utils.HadoopConfiguration;
import eu.stratosphere.hadoopcompatibility.mapred.record.datatypes.DefaultStratosphereTypeConverter;
import eu.stratosphere.hadoopcompatibility.mapred.record.datatypes.StratosphereTypeConverter;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.DefaultHadoopOutput;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.DummyHadoopReporter;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.HadoopOutputWrapper;
import eu.stratosphere.types.Record;
import eu.stratosphere.util.Collector;
import eu.stratosphere.util.InstantiationUtil;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class HadoopMapperWrapper<KEYIN extends WritableComparable,VALUEIN extends Writable,
		KEYOUT extends WritableComparable,VALUEOUT extends Writable> extends MapFunction implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobConf jobConf;
	private Mapper mapper;
	private String mapperName;
	private StratosphereTypeConverter<KEYOUT,VALUEOUT> stratosphereConverter;
	private HadoopOutputWrapper<KEYIN,VALUEIN,Record> output;
	private Reporter reporter;

	@SuppressWarnings("unchecked")
	public HadoopMapperWrapper(JobConf jobConf) {
		this(jobConf, new DefaultStratosphereTypeConverter(jobConf.getMapOutputKeyClass(),
				jobConf.getMapOutputValueClass()), new DefaultHadoopOutput<KEYIN,VALUEIN>(), new DummyHadoopReporter());
	}

	public HadoopMapperWrapper(JobConf jobConf, StratosphereTypeConverter<KEYOUT,VALUEOUT> stratosphereConverter,
								HadoopOutputWrapper<KEYIN,VALUEIN,Record> output,  Reporter reporter) {
		this.jobConf = jobConf;
		this.mapper = InstantiationUtil.instantiate(jobConf.getMapperClass());
		this.mapperName = mapper.getClass().getName();
		this.stratosphereConverter = stratosphereConverter;
		this.output = output;
		this.reporter = reporter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void map(Record record, Collector<Record> out) throws Exception {
		output.wrapStratosphereCollector(out);
		KEYOUT key = stratosphereConverter.convertKey(record);
		VALUEOUT value = stratosphereConverter.convertValue(record);
		mapper.map(key, value, output, reporter);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeUTF(mapperName);
		HadoopConfiguration.setOutputCollectorToConf(output.getClass(), jobConf);
		HadoopConfiguration.setReporterToConf(reporter.getClass(), jobConf);
		jobConf.write(out);
		out.writeObject(stratosphereConverter);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		mapperName = in.readUTF();
		if(jobConf == null) {
			jobConf = new JobConf();
		}
		jobConf.readFields(in);
		try {
			this.mapper = (Mapper) Class.forName(this.mapperName).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Unable to instantiate the hadoop mapper", e);
		}
		ReflectionUtils.setConf(mapper, jobConf);
		stratosphereConverter =  (StratosphereTypeConverter<KEYOUT,VALUEOUT>) in.readObject();
		output = InstantiationUtil.instantiate(HadoopConfiguration.getOutputCollectorFromConf(jobConf));
		reporter = InstantiationUtil.instantiate(HadoopConfiguration.getReporterFromConf(jobConf));
	}

}
