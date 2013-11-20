/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package eu.stratosphere.pact.common.io;

import java.io.IOException;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.fs.FileInputSplit;
import eu.stratosphere.pact.common.contract.CompilerHints;
import eu.stratosphere.pact.common.contract.FileDataSource;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.Value;
import eu.stratosphere.pact.common.type.base.PactDouble;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactLong;
import eu.stratosphere.pact.common.type.base.parser.FieldParser;
import eu.stratosphere.pact.generic.contract.Contract;
import eu.stratosphere.pact.generic.io.GenericCsvInputFormat;

/**
 * Input format to parse text files and generate PactRecords. 
 * The input file is structured by record delimiters and field delimiters (CSV files are common).
 * Record delimiter separate records from each other ('\n' is common).
 * Field delimiters separate fields within a record. 
 * Record and field delimiters must be configured using the InputFormat {@link Configuration}.
 * 
 * The number of fields to parse must be configured as well.  
 * For each field a {@link FieldParser} must be specified using the {@link RecordInputFormat#FIELD_PARSER_PARAMETER_PREFIX} config key.
 * FieldParsers can be configured by adding config entries to the InputFormat configuration. The InputFormat forwards its configuration to each {@link FieldParser}.
 * 
 * The position within the text record can be configured for each field using the {@link RecordInputFormat#TEXT_POSITION_PARAMETER_PREFIX} config key.
 * Either all text positions must be configured or none. If none is configured, the index of the config key is used.
 * 
 * The position within the {@link PactRecord} can be configured for each field using the {@link RecordInputFormat#RECORD_POSITION_PARAMETER_PREFIX} config key.
 * Either all {@link PactRecord} positions must be configured or none. If none is configured, the index of the config key is used.
 * 
 * @see FieldParser
 * @see Configuration
 * @see PactRecord
 */
public class RecordInputFormat extends GenericCsvInputFormat<PactRecord> {
	
	private static final long serialVersionUID = 5892337953840605822L;
	

	private int[] targetPositions;
	
	private transient Value[] parsedValues;
	
	
	// --------------------------------------------------------------------------------------------
	//  Constructors and getters/setters for the configurable parameters
	// --------------------------------------------------------------------------------------------
	
	public RecordInputFormat() {
		super();
	}
	
	public RecordInputFormat(char fieldDelimiter) {
		super(fieldDelimiter);
	}
	
	public RecordInputFormat(Class<? extends Value> ... fields) {
		super(fields);
	}
	
	public RecordInputFormat(char fieldDelimiter, Class<? extends Value> ... fields) {
		super(fieldDelimiter, fields);
	}

	// --------------------------------------------------------------------------------------------
	
	public int[] getTargetPositions() {
		return targetPositions;
	}
	
	public void setTargetPositions(int[] targetPositions) {
		this.targetPositions = targetPositions;
	}
	
	// --------------------------------------------------------------------------------------------
	//  Pre-flight: Configuration
	// --------------------------------------------------------------------------------------------

	@Override
	public void configure(Configuration config) {
		super.configure(config);
		
		final String fieldDelimStr = config.getString(FIELD_DELIMITER_PARAMETER, null);
		if (fieldDelimStr != null) {
			if (fieldDelimStr.length() != 1) {
				throw new IllegalArgumentException("Invalid configuration for RecordInputFormat: " +
						"Field delimiter must be a single character");
			} else {
				setFieldDelim(fieldDelimStr.charAt(0));
			}
		}
		
		// read number of field configured via configuration
		int numConfigFields = config.getInteger(NUM_FIELDS_PARAMETER, -1);
		if (numConfigFields != -1) {
		
			int[] textPosIdx = new int[numFields];
			boolean anyTextPosSet = false;
			boolean allTextPosSet = true;
			int maxTextPos = -1;
			
			// parse text positions
			for (int i = 0; i < numFields; i++)
			{
				int pos = config.getInteger(TEXT_POSITION_PARAMETER_PREFIX + i, -1);
				if (pos == -1) {
					allTextPosSet = false;
					textPosIdx[i] = i;
					maxTextPos = i;
				} else {
					anyTextPosSet = true;
					textPosIdx[i] = pos;
					maxTextPos = pos > maxTextPos ? pos : maxTextPos;
				}
			}
			// check if either none or all text positions have been set
			if(anyTextPosSet && !allTextPosSet) {
				throw new IllegalArgumentException("Invalid configuration for RecordInputFormat: " +
						"Not all text positions set");
			}
			
			int[] recPosIdx = new int[numFields];
			boolean anyRecPosSet = false;
			boolean allRecPosSet = true;
			
			// parse record positions
			for (int i = 0; i < numFields; i++)
			{
				int pos = config.getInteger(RECORD_POSITION_PARAMETER_PREFIX + i, -1);
				if (pos == -1) {
					allRecPosSet = false;
					recPosIdx[i] = i;
				} else {
					anyRecPosSet = true;
					recPosIdx[i] = pos;
				}
			}
			// check if either none or all record positions have been set
			if(anyRecPosSet && !allRecPosSet) {
				throw new IllegalArgumentException("Invalid configuration for RecordInputFormat: " +
						"Not all record positions set");
			}
			
			// init parse and position arrays
			this.fieldParsers = (FieldParser<Value>[]) new FieldParser[maxTextPos+1];
			this.fieldValues = new Value[maxTextPos+1];
			this.recordPositions = new int[maxTextPos+1];
			for (int j = 0; j < maxTextPos; j++) 
			{
				fieldParsers[j] = null;
				fieldValues[j] = null;
				recordPositions[j] = -1;
			}
			
			for (int i = 0; i < numFields; i++)
			{
				int pos = textPosIdx[i];
				recordPositions[pos] = recPosIdx[i];
				Class<FieldParser<Value>> clazz = (Class<FieldParser<Value>>) config.getClass(FIELD_TYPE_PARAMETER_PREFIX + i, null);
				if (clazz == null) {
					throw new IllegalArgumentException("Invalid configuration for RecordInputFormat: " +
						"No field parser class for parameter " + i);
				}
				
				try {
					fieldParsers[pos] = clazz.newInstance();
				} catch(InstantiationException ie) {
					throw new IllegalArgumentException("Invalid configuration for RecordInputFormat: " +
							"No field parser could not be instanciated for parameter " + i);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException("Invalid configuration for RecordInputFormat: " +
							"No field parser could not be instanciated for parameter " + i);
				}
				fieldValues[pos] = fieldParsers[pos].createValue();
			}
		}
	}
	
	
	@Override
	public void open(FileInputSplit split) throws IOException {
		super.open(split);
		
		FieldParser<Value>[] fieldParsers = getFieldParsers();
		
		// create the value holders
		this.parsedValues = new Value[fieldParsers.length];
		for (int i = 0; i < fieldParsers.length; i++) {
			this.parsedValues[i] = fieldParsers[i].createValue();
		}
		
		// adjust the output positions
		if (this.targetPositions == null) {
			this.targetPositions = new int[0];
		}
		
		// if we do not have enough target positions, fill the positions with x->x values
		if (this.targetPositions.length < fieldParsers.length) {
			int[] newTargets = new int[fieldParsers.length];
			System.arraycopy(this.targetPositions, 0, newTargets, 0, this.targetPositions.length);
			
			for (int i = this.targetPositions.length; i < fieldParsers.length; i++) {
				newTargets[i] = i;
			}
			this.targetPositions = newTargets;
		}
	}
	
	@Override
	public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes) throws ParseException {
		if (parseRecord(parsedValues, bytes, offset, numBytes)) {
			// valid parse, map values into pact record
			for (int i = 0; i < targetPositions.length; i++) {
				target.setField(targetPositions[i], parsedValues[i]);
			}
			return true;
		} else {
			return false;
		}
	}
	
	// ============================================================================================
	//  Parameterization via configuration
	// ============================================================================================
	
	// ------------------------------------- Config Keys ------------------------------------------
	
	private static final String FIELD_DELIMITER_PARAMETER = "recordinformat.delimiter.field";
	
	private static final String NUM_FIELDS_PARAMETER = "recordinformat.field.number";
	
	private static final String FIELD_TYPE_PARAMETER_PREFIX = "recordinformat.field.parser_";
	
	private static final String TEXT_POSITION_PARAMETER_PREFIX = "recordinformat.text.position_";
	
	private static final String RECORD_POSITION_PARAMETER_PREFIX = "recordinformat.record.position_";
	
	/**
	 * Creates a configuration builder that can be used to set the input format's parameters to the config in a fluent
	 * fashion.
	 * 
	 * @return A config builder for setting parameters.
	 */
	public static ConfigBuilder configureRecordFormat(FileDataSource target) {
		return new ConfigBuilder(target, target.getParameters());
	}
	
	/**
	 * An abstract builder used to set parameters to the input format's configuration in a fluent way.
	 */
	protected static class AbstractConfigBuilder<T> extends DelimitedInputFormat.AbstractConfigBuilder<T> {
		
		protected final RecordFormatCompilerHints hints;
		
		/**
		 * Creates a new builder for the given configuration.
		 * 
		 * @param targetConfig The configuration into which the parameters will be written.
		 */
		protected AbstractConfigBuilder(Contract contract, Configuration config) {
			super(config);
			this.hints = new RecordFormatCompilerHints(contract.getCompilerHints());
			contract.swapCompilerHints(this.hints);
			
			// initialize with 2 bytes length for the header (its actually 3, but one is skipped on the first field
			this.hints.addWidthRecordFormat(2);
		}
		
		// --------------------------------------------------------------------
		
		/**
		 * Sets the delimiter that delimits the individual fields in the records textual input representation.
		 * 
		 * @param delimiter The character to be used as a field delimiter.
		 * @return The builder itself.
		 */
		public T fieldDelimiter(char delimiter) {
			this.config.setString(FIELD_DELIMITER_PARAMETER, String.valueOf(delimiter));
			@SuppressWarnings("unchecked")
			T ret = (T) this;
			return ret;
		}
		
		public T field(Class<? extends Value> type, int textPosition) {
			return field(type, textPosition, Float.NEGATIVE_INFINITY);

		}
		
		public T field(Class<? extends Value> type, int textPosition, float avgLen) {
			// register field
			final int numYet = this.config.getInteger(NUM_FIELDS_PARAMETER, 0);
			this.config.setClass(FIELD_TYPE_PARAMETER_PREFIX + numYet, type);
			this.config.setInteger(TEXT_POSITION_PARAMETER_PREFIX + numYet, textPosition);
			this.config.setInteger(NUM_FIELDS_PARAMETER, numYet + 1);
			
			// register length
			if (avgLen == Float.NEGATIVE_INFINITY) {
				if (type == PactInteger.class) {
					avgLen = 4f;
				} else if (type == PactDouble.class || type == PactLong.class) {
					avgLen = 8f;
				}
			}
			
			if (avgLen != Float.NEGATIVE_INFINITY) {
				// add the len, plus one byte for the offset coding
				this.hints.addWidthRecordFormat(avgLen + 1);
			}
			
			@SuppressWarnings("unchecked")
			T ret = (T) this;
			return ret;
		}
	}
	
	/**
	 * A builder used to set parameters to the input format's configuration in a fluent way.
	 */
	public static class ConfigBuilder extends AbstractConfigBuilder<ConfigBuilder> {
		
		protected ConfigBuilder(Contract target, Configuration targetConfig) {
			super(target, targetConfig);
		}
	}
	
	private static final class RecordFormatCompilerHints extends CompilerHints {
		
		private float width = 0.0f;
		
		private RecordFormatCompilerHints(CompilerHints parent) {
			copyFrom(parent);
		}

		@Override
		public float getAvgBytesPerRecord() {
			float superWidth = super.getAvgBytesPerRecord();
			if (superWidth > 0.0f || this.width <= 0.0f) {
				return superWidth;
			} else {
				return this.width;
			}
		}

		private void addWidthRecordFormat(float width) {
			this.width += width;
		}
	}
}