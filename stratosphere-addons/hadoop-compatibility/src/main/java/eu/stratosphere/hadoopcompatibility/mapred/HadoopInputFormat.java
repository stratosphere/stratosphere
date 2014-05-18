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

package eu.stratosphere.hadoopcompatibility.mapred;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.util.ReflectionUtils;

import eu.stratosphere.api.common.io.InputFormat;
import eu.stratosphere.api.common.io.statistics.BaseStatistics;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.ResultTypeQueryable;
import eu.stratosphere.api.java.typeutils.TupleTypeInfo;
import eu.stratosphere.api.java.typeutils.WritableTypeInfo;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.hadoopcompatibility.mapred.utils.HadoopConfiguration;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.HadoopDummyReporter;
import eu.stratosphere.hadoopcompatibility.mapred.wrapper.HadoopInputSplit;
import eu.stratosphere.types.TypeInformation;

public class HadoopInputFormat<K extends Writable,V extends Writable> implements InputFormat<Tuple2<K,V>, HadoopInputSplit>, ResultTypeQueryable<Tuple2<K,V>> {

	private static final long serialVersionUID = 1L;

	private org.apache.hadoop.mapred.InputFormat<K, V> hadoopInputFormat;
	private Class<K> keyClass;
	private Class<V> valueClass;
	private JobConf jobConf;
	
	public transient K key;
	public transient V value;
	
	public RecordReader<K, V> recordReader;
	private boolean fetched = false;
	private boolean hasNext;
		
	public HadoopInputFormat() {
		super();
	}
	
	public HadoopInputFormat(org.apache.hadoop.mapred.InputFormat<K,V> hadoopInputFormat, Class<K> key, Class<V> value, JobConf job) {
		super();
		this.hadoopInputFormat = hadoopInputFormat;
		this.keyClass = key;
		this.valueClass = value;
		HadoopConfiguration.mergeHadoopConf(job);
		this.jobConf = job;
	}
	
	@Override
	public void configure(Configuration parameters) {
		
	}
	
	@Override
	public BaseStatistics getStatistics(BaseStatistics cachedStatistics) throws IOException {
		return null;
	}
	
	@Override
	public HadoopInputSplit[] createInputSplits(int minNumSplits)
			throws IOException {
		org.apache.hadoop.mapred.InputSplit[] splitArray = hadoopInputFormat.getSplits(jobConf, minNumSplits);
		HadoopInputSplit[] hiSplit = new HadoopInputSplit[splitArray.length];
		for(int i=0;i<splitArray.length;i++){
			hiSplit[i] = new HadoopInputSplit(splitArray[i], jobConf);
		}
		return hiSplit;
	}
	
	@Override
	public Class<? extends HadoopInputSplit> getInputSplitType() {
		return HadoopInputSplit.class;
	}
	
	@Override
	public void open(HadoopInputSplit split) throws IOException {
		this.recordReader = this.hadoopInputFormat.getRecordReader(split.getHadoopInputSplit(), jobConf, new HadoopDummyReporter());
		key = this.recordReader.createKey();
		value = this.recordReader.createValue();
		this.fetched = false;
	}
	
	private void fetchNext() throws IOException {
		hasNext = this.recordReader.next(key, value);
		fetched = true;
	}
	
	@Override
	public boolean reachedEnd() throws IOException {
		if(!fetched) {
			fetchNext();
		}
		return !hasNext;
	}
	
	@Override
	public Tuple2<K, V> nextRecord(Tuple2<K, V> record) throws IOException {
		if(!fetched) {
			fetchNext();
		}
		if(!hasNext) {
			return null;
		}
		record.f0 = key;
		record.f1 = value;
		fetched = false;
		return record;
	}
	
	@Override
	public void close() throws IOException {
		this.recordReader.close();
	}
	
	/**
	 * Custom serialization methods.
	 *  @see http://docs.oracle.com/javase/7/docs/api/java/io/Serializable.html
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeUTF(hadoopInputFormat.getClass().getName());
		out.writeUTF(keyClass.getName());
		out.writeUTF(valueClass.getName());
		jobConf.write(out);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String hadoopInputFormatClassName = in.readUTF();
		String keyClassName = in.readUTF();
		String valueClassName = in.readUTF();
		if(jobConf == null) {
			jobConf = new JobConf();
		}
		jobConf.readFields(in);
		try {
			this.hadoopInputFormat = (org.apache.hadoop.mapred.InputFormat<K,V>) Class.forName(hadoopInputFormatClassName).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Unable to instantiate the hadoop input format", e);
		}
		try {
			this.keyClass = (Class<K>) Class.forName(keyClassName);
		} catch (Exception e) {
			throw new RuntimeException("Unable to find key class.", e);
		}
		try {
			this.valueClass = (Class<V>) Class.forName(valueClassName);
		} catch (Exception e) {
			throw new RuntimeException("Unable to find value class.", e);
		}
		ReflectionUtils.setConf(hadoopInputFormat, jobConf);
	}
	
	public void setJobConf(JobConf job) {
		this.jobConf = job;
	}
		
	public org.apache.hadoop.mapred.InputFormat<K,V> getHadoopInputFormat() {
		return hadoopInputFormat;
	}
	
	public void setHadoopInputFormat(org.apache.hadoop.mapred.InputFormat<K,V> hadoopInputFormat) {
		this.hadoopInputFormat = hadoopInputFormat;
	}
	
	public JobConf getJobConf() {
		return jobConf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TypeInformation<Tuple2<K,V>> getProducedType() {
		return new TupleTypeInfo<Tuple2<K,V>>(new WritableTypeInfo<Writable>((Class<Writable>) keyClass), new WritableTypeInfo<Writable>((Class<Writable>) valueClass));
	}
}
