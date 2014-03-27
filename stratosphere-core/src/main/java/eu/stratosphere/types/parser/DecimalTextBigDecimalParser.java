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

package eu.stratosphere.types.parser;

import java.math.BigDecimal;
import java.math.RoundingMode;

import eu.stratosphere.types.DecimalValue;

/**
 * Parses a text field into a {@link DecimalValue}
 */
public class DecimalTextBigDecimalParser extends FieldParser<DecimalValue> {
	
	private DecimalValue result;
	
	// optional fields if the user wants to convert the input to a certain scale (using a rounding mode).
	private int scale;
	private RoundingMode rounding = null;
	
	public void enforceScale(int scale, RoundingMode rounding) {
		this.scale = scale;
		this.rounding = rounding;
	}
	
	@Override
	public int parseField(byte[] bytes, int startPos, int limit, char delim, DecimalValue reusable) {
		
		int i = startPos;
		final byte delByte = (byte) delim;
		
		while (i < limit && bytes[i] != delByte) {
			i++;
		}
		
		String str = new String(bytes, startPos, i-startPos);
		BigDecimal bd = new BigDecimal(str);
		if(rounding != null) {
			bd = bd.setScale(scale, rounding);
		}
		reusable.setValue(bd);
		this.result = reusable;
		return (i == limit) ? limit : i+1;
	}
	
	@Override
	public DecimalValue createValue() {
		return new DecimalValue();
	}

	@Override
	public DecimalValue getLastResult() {
		return this.result;
	}
}
