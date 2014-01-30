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

package eu.stratosphere.api.common.operators;

import java.util.HashMap;
import java.util.Map;

import eu.stratosphere.api.common.operators.util.FieldSet;

/**
 * Container for the semantic properties associated to a dual input operator.
 */
public class DualInputSemanticProperties extends SemanticProperties {
	
	/**
	 * Mapping from fields in the original record(s) to fields in the destination
	 * record(s).  
	 */
	private Map<Integer,FieldSet> forwardedFields1;
	
	/**
	 * Mapping from fields in the original record(s) to fields in the destination
	 * record(s).  
	 */
	private Map<Integer,FieldSet> forwardedFields2;
	
	/**
	 * Set of fields that are read in the origin record(s) from the
	 * first input.
	 */
	private FieldSet readFields1;

	/**
	 * Set of fields that are read in the origin record(s) from the
	 * second input.
	 */
	private FieldSet readFields2;

	
	public DualInputSemanticProperties() {
		super();
		this.init();
	}
	
	
	public void addForwardedField1(int sourceField, int destinationField) {
		FieldSet fs;
		if((fs = this.forwardedFields1.get(sourceField)) != null)
			fs.add(destinationField);
		else {
			fs = new FieldSet(destinationField);
			this.forwardedFields1.put(sourceField, fs);
		}
	}
	
	public void addForwardedField1(int sourceField, FieldSet destinationFields) {
		FieldSet fs;
		if((fs = this.forwardedFields1.get(sourceField)) != null)
			fs.addAll(destinationFields);
		else {
			fs = new FieldSet(destinationFields);
			this.forwardedFields1.put(sourceField, fs);
		}
	}
	
	public void setForwardedField1(int sourceField, FieldSet destinationFields) {
		this.forwardedFields1.put(sourceField, destinationFields);
	}
	
	public FieldSet getForwardedField1(int source) {
		return this.forwardedFields1.get(source);
	}
	
	public void addForwardedField2(int sourceField, int destinationField) {
		FieldSet fs;
		if((fs = this.forwardedFields2.get(sourceField)) != null)
			fs.add(destinationField);
		else {
			fs = new FieldSet(destinationField);
			this.forwardedFields2.put(sourceField, fs);
		}
	}
	
	public void addForwardedField2(int sourceField, FieldSet destinationFields) {
		FieldSet fs;
		if((fs = this.forwardedFields2.get(sourceField)) != null)
			fs.addAll(destinationFields);
		else {
			fs = new FieldSet(destinationFields);
			this.forwardedFields2.put(sourceField, fs);
		}
	}
	
	public void setForwardedField2(int sourceField, FieldSet destinationFields) {
		this.forwardedFields2.put(sourceField, destinationFields);
	}
	
	public FieldSet getForwardedField2(int source) {
		return this.forwardedFields2.get(source);
	}
	
	public void addReadFields1(FieldSet readFields1) {
		if(this.readFields1 == null)
			this.readFields1 = new FieldSet(readFields1);
		else
			this.readFields1.addAll(readFields1);
	}
	
	public void setReadFields1(FieldSet readFields1) {
		this.readFields1 = readFields1;
	}
	
	public FieldSet getReadFields1() {
		return this.readFields1;
	}
	
	public void addReadFields2(FieldSet readFields2) {
		if(this.readFields2 == null)
			this.readFields2 = new FieldSet(readFields2);
		else
			this.readFields2.addAll(readFields2);
	}
	
	public void setReadFields2(FieldSet readFields2) {
		this.readFields2 = readFields2;
	}
	
	public FieldSet getReadFields2() {
		return this.readFields2;
	}
	
	@Override
	public void clearProperties() {
		this.init();
		super.clearProperties();
	}
	
	private void init() {
		this.forwardedFields1 = new HashMap<Integer,FieldSet>();
		this.forwardedFields2 = new HashMap<Integer,FieldSet>();
		this.readFields1 = null;
		this.readFields2 = null;
	}
		
}
