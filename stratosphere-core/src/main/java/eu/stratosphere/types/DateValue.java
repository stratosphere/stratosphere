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
package eu.stratosphere.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValue implements Value {

	private static final long serialVersionUID = 1L;

	private Date date;

	public DateValue() {}
	public DateValue(String str) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //use ISO standard in case the format is not given.
		try {
			this.date = df.parse(str);
		} catch (java.text.ParseException e) {
			throw new RuntimeException("Could not Parse Date for the prescribed method");
		}
	}

	public DateValue(String str, String format) {
		DateFormat df = new SimpleDateFormat(format);
		try {
			this.date = df.parse(str);
		} catch (java.text.ParseException e) {
			throw new RuntimeException("Could not Parse Date for the prescribed method");
		}
	}

	public DateValue(String str, DateFormat df) {
		try {
			this.date = df.parse(str);
		} catch (java.text.ParseException e) {
			throw new RuntimeException("Could not Parse Date for the prescribed method");
		}
	}

	public void setDate(long dt) {
		this.date = new Date(dt);
	}

	public Date getDate() {
		return this.date;
	}

	public long asLong() {
		return date.getTime();
	}

	@Override
	public String toString() {
		return this.date.toString();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(date.getTime());
	}

	@Override
	public void read(DataInput in) throws IOException {
		setDate(in.readLong());
	}
}
