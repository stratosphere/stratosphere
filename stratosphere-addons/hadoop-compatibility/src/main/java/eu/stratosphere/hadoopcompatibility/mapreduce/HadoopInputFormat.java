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

package eu.stratosphere.hadoopcompatibility.mapreduce;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;

import eu.stratosphere.api.common.io.InputFormat;
import eu.stratosphere.api.common.io.statistics.BaseStatistics;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.ResultTypeQueryable;
import eu.stratosphere.api.java.typeutils.TupleTypeInfo;
import eu.stratosphere.api.java.typeutils.WritableTypeInfo;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.hadoopcompatibility.mapreduce.utils.HadoopUtils;
import eu.stratosphere.hadoopcompatibility.mapreduce.wrapper.HadoopInputSplit;
import eu.stratosphere.types.TypeInformation;

public class HadoopInputFormat<K extends Writable,V extends Writable> implements InputFormat<Tuple2<K,V>, HadoopInputSplit>, ResultTypeQueryable<Tuple2<K,V>> {
	
	private static final long serialVersionUID = 1L;
	
	private org.apache.hadoop.mapreduce.InputFormat<K, V> mapreduceInputFormat;
	private Class<K> keyClass;
	private Class<V> valueClass;
	private org.apache.hadoop.conf.Configuration configuration;
	
	public RecordReader<K, V> recordReader;
	private boolean fetched = false;
	private boolean hasNext;
	
	public HadoopInputFormat() {
		super();
	}
	
	public HadoopInputFormat(org.apache.hadoop.mapreduce.InputFormat<K,V> mapreduceInputFormat, Class<K> key, Class<V> value, Job job) {
		super();
		this.mapreduceInputFormat = mapreduceInputFormat;
		this.keyClass = key;
		this.valueClass = value;
		this.configuration = job.getConfiguration();
		HadoopUtils.mergeHadoopConf(configuration);
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
		configuration.setInt("mapreduce.input.fileinputformat.split.minsize", minNumSplits);
		
		JobContext jobContext = null;
		try {
			jobContext = HadoopUtils.instantiateJobContext(configuration, new JobID());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		List<org.apache.hadoop.mapreduce.InputSplit> splits;
		try {
			splits = this.mapreduceInputFormat.getSplits(jobContext);
		} catch (InterruptedException e) {
			throw new IOException("Could not get Splits.", e);
		}
		HadoopInputSplit[] hadoopInputSplits = new HadoopInputSplit[splits.size()];
		
		for(int i = 0; i < hadoopInputSplits.length; i++){
			hadoopInputSplits[i] = new HadoopInputSplit(splits.get(i), jobContext);
		}
		return hadoopInputSplits;
	}
	
	@Override
	public Class<? extends HadoopInputSplit> getInputSplitType() {
		return HadoopInputSplit.class;
	}
	
	@Override
	public void open(HadoopInputSplit split) throws IOException {
		TaskAttemptContext context = null;
		try {
			context = HadoopUtils.instantiateTaskAttemptContext(configuration, new TaskAttemptID());
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			this.recordReader = this.mapreduceInputFormat
					.createRecordReader(split.getHadoopInputSplit(), context);
			this.recordReader.initialize(split.getHadoopInputSplit(), context);
		} catch (InterruptedException e) {
			throw new IOException("Could not create RecordReader.", e);
		} finally {
			this.fetched = false;
		}
	}
	
	private void fetchNext() throws IOException {
		try {
			this.hasNext = this.recordReader.nextKeyValue();
		} catch (InterruptedException e) {
			throw new IOException("Could not fetch next KeyValue pair.", e);
		} finally {
			this.fetched = true;
		}
	}
	
	@Override
	public boolean reachedEnd() throws IOException {
		if(!this.fetched) {
			fetchNext();
		}
		return !this.hasNext;
	}
	
	@Override
	public Tuple2<K, V> nextRecord(Tuple2<K, V> record) throws IOException {
		if(!this.fetched) {
			fetchNext();
		}
		if(!this.hasNext) {
			return null;
		}
		try {
			record.f0 = this.recordReader.getCurrentKey();
			record.f1 = this.recordReader.getCurrentValue();
		} catch (InterruptedException e) {
			throw new IOException("Could not get KeyValue pair.", e);
		}
		this.fetched = false;
		
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
		out.writeUTF(this.mapreduceInputFormat.getClass().getName());
		out.writeUTF(this.keyClass.getName());
		out.writeUTF(this.valueClass.getName());
		this.configuration.write(out);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String hadoopInputFormatClassName = in.readUTF();
		String keyClassName = in.readUTF();
		String valueClassName = in.readUTF();
		
		org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
		configuration.readFields(in);
		
		if(this.configuration == null) {
			this.configuration = configuration;
		}
		
		try {
			this.mapreduceInputFormat = (org.apache.hadoop.mapreduce.InputFormat<K,V>) Class.forName(hadoopInputFormatClassName).newInstance();
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
	}
	
	public void setConfiguration(org.apache.hadoop.conf.Configuration configuration) {
		this.configuration = configuration;
	}
	
	public org.apache.hadoop.mapreduce.InputFormat<K,V> getHadoopInputFormat() {
		return this.mapreduceInputFormat;
	}
	
	public void setHadoopInputFormat(org.apache.hadoop.mapreduce.InputFormat<K,V> mapreduceInputFormat) {
		this.mapreduceInputFormat = mapreduceInputFormat;
	}
	
	public org.apache.hadoop.conf.Configuration getConfiguration() {
		return this.configuration;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TypeInformation<Tuple2<K,V>> getProducedType() {
		return new TupleTypeInfo<Tuple2<K,V>>(new WritableTypeInfo<Writable>((Class<Writable>) keyClass), new WritableTypeInfo<Writable>((Class<Writable>) valueClass));
	}
}
