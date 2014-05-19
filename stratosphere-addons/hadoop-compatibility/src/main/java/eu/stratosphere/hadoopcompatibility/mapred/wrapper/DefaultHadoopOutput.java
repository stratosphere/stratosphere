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

import eu.stratosphere.hadoopcompatibility.mapred.record.datatypes.DefaultHadoopTypeConverter;
import eu.stratosphere.hadoopcompatibility.mapred.record.datatypes.HadoopTypeConverter;
import eu.stratosphere.types.Record;
import eu.stratosphere.util.Collector;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public class DefaultHadoopOutput<KEYOUT extends WritableComparable,VALUEOUT extends Writable>
		implements HadoopOutputWrapper<KEYOUT,VALUEOUT,Record> {

	private Collector<Record> collector;
	private HadoopTypeConverter<KEYOUT,VALUEOUT> converter;

	public DefaultHadoopOutput() {
		this( new DefaultHadoopTypeConverter<KEYOUT, VALUEOUT>(), null);
	}

	@SuppressWarnings("unused")
	public DefaultHadoopOutput(HadoopTypeConverter<KEYOUT,VALUEOUT> converter) {
		this( converter, null);
	}

	public DefaultHadoopOutput(HadoopTypeConverter<KEYOUT,VALUEOUT> converter, Collector<Record> collector) {
		this.converter = converter;
		this.collector = collector;
	}

	@Override
	public void wrapStratosphereCollector(Collector<Record> collector) {
		this.collector = collector;
	}

	@Override
	public void setHadoopConverter(HadoopTypeConverter<KEYOUT,VALUEOUT> converter) {
		this.converter = converter;
	}

	@Override
	public void collect(KEYOUT key, VALUEOUT value) {
		Record record = new Record();
		converter.convert(record, key, value);
		collector.collect(record);
	}
}