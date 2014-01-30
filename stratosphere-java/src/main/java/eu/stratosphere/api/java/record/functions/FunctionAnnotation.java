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

package eu.stratosphere.api.java.record.functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.stratosphere.api.common.operators.DualInputSemanticProperties;
import eu.stratosphere.api.common.operators.SingleInputSemanticProperties;
import eu.stratosphere.api.common.operators.util.FieldSet;
import eu.stratosphere.api.common.operators.util.UserCodeWrapper;


/**
 * This class defines the PACT annotations, realized as java annotations. To use
 * a PACT annotation, simply add the annotation above the class declaration of
 * the class that realized the user function. For example, to declare the <i>ConstantFieldsExcept</i> 
 * annotation for a map-type function that realizes a simple absolute function,
 * use it the following way:
 * 
 * <pre><blockquote>
 * \@ConstantFieldsExcept(fields={2})
 * public class MyMapper extends MapFunction
 * {
 *     public void map(Record record, Collector out)
 *     {
 *        int value = record.getField(2, IntValue.class).getValue();
		  record.setField(2, new IntValue(Math.abs(value)));
		  
		  out.collect(record);
 *     }
 * }
 * </blockquote></pre>
 * 
 * Be aware that some annotations should only be used for stubs with as single input 
 * ({@link MapFunction}, {@link ReduceFunction}) and some only for stubs with two inputs 
 * ({@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction}).
 */
public class FunctionAnnotation {
	
	/**
	 * Specifies the fields of an input record that are unchanged in the output of 
	 * a stub with a single input ( {@link MapFunction}, {@link ReduceFunction}).
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a PACT program can not be guaranteed.
	 * So if in doubt, do not add a field to this set.
	 * </b>
	 * 
	 * This annotation is mutually exclusive with the {@link ConstantFieldsExcept} annotation.
	 * 
	 * If this annotation and the {@link ConstantFieldsExcept} annotation is not set, it is 
	 * assumed that <i>no</i> field is constant.
	 *
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstantFields {
		int[] value();
	}
	
	/**
	 * Specifies the fields of an input record of the first input that are unchanged in 
	 * the output of a stub with two inputs ( {@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction})
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a PACT program can not be guaranteed.
	 * So if in doubt, do not add a field to this set.
	 * </b>
	 *
	 * This annotation is mutually exclusive with the {@link ConstantFieldsFirstExcept} annotation.
	 * 
	 * If this annotation and the {@link ConstantFieldsFirstExcept} annotation is not set, it is 
	 * assumed that <i>no</i> field is constant.
	 * 
	 *
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstantFieldsFirst {
		int[] value();
	}
	
	/**
	 * Specifies the fields of an input record of the second input that are unchanged in 
	 * the output of a stub with two inputs ( {@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction})
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a PACT program can not be guaranteed.
	 * So if in doubt, do not add a field to this set.
	 * </b>
	 *
	 * This annotation is mutually exclusive with the {@link ConstantFieldsSecondExcept} annotation.
	 * 
	 * If this annotation and the {@link ConstantFieldsSecondExcept} annotation is not set, it is 
	 * assumed that <i>no</i> field is constant.
	 * 
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstantFieldsSecond {
		int[] value();
	}
	
	/**
	 * Specifies the fields of an input record that are changed in the output of 
	 * a stub with a single input ( {@link MapFunction}, {@link ReduceFunction}). All other 
	 * fields are assumed to be constant.
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a PACT program can not be guaranteed.
	 * So if in doubt, do not add a field to this set.
	 * </b>
	 * 
	 * This annotation is mutually exclusive with the {@link ConstantFields} annotation.
	 * 
	 * If this annotation and the {@link ConstantFields} annotation is not set, it is 
	 * assumed that <i>no</i> field is constant.
	 *
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstantFieldsExcept {
		int[] value();
	}
	
	/**
	 * Specifies the fields of an input record of the first input that are changed in 
	 * the output of a stub with two inputs ( {@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction})
	 * All other fields are assumed to be constant.
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a PACT program can not be guaranteed.
	 * So if in doubt, do not add a field to this set.
	 * </b>
	 *
	 * This annotation is mutually exclusive with the {@link ConstantFieldsFirst} annotation.
	 * 
	 * If this annotation and the {@link ConstantFieldsFirst} annotation is not set, it is 
	 * assumed that <i>no</i> field is constant.
	 * 
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstantFieldsFirstExcept {
		int[] value();
	}
	
	
	/**
	 * Specifies the fields of an input record of the second input that are changed in 
	 * the output of a stub with two inputs ( {@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction})
	 * All other fields are assumed to be constant.
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a PACT program can not be guaranteed.
	 * So if in doubt, do not add a field to this set.
	 * </b>
	 *
	 * This annotation is mutually exclusive with the {@link ConstantFieldsSecond} annotation.
	 * 
	 * If this annotation and the {@link ConstantFieldsSecond} annotation is not set, it is 
	 * assumed that <i>no</i> field is constant.
	 * 
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstantFieldsSecondExcept {
		int[] value();
	}
	
	
	/**
	 * Private constructor to prevent instantiation. This class is intended only as a container.
	 */
	private FunctionAnnotation() {}
	
	// --------------------------------------------------------------------------------------------
	//                                   Function Annotation Handling
	// --------------------------------------------------------------------------------------------
	
	public static SingleInputSemanticProperties readSingleConstantAnnotations(UserCodeWrapper<?> udf) {		
		SingleInputSemanticProperties semanticProperties = new SingleInputSemanticProperties();
		
		// get constantSet annotation from stub
		ConstantFields constantSet = udf.getUserCodeAnnotation(ConstantFields.class);
		
		// extract constantSet from annotation
		if(constantSet != null) {
			for(int value: constantSet.value()) {
				semanticProperties.addForwardedField(value,value);
			}
		}
		
		ConstantFieldsExcept notConstantSet = udf.getUserCodeAnnotation(ConstantFieldsExcept.class);
		
		// extract notConstantSet from annotation
		if(notConstantSet != null) {
			semanticProperties.addWrittenFields(new FieldSet(notConstantSet.value()));
		}
			
		if (notConstantSet != null && constantSet != null) {
			throw new RuntimeException("Either ConstantFields or ConstantFieldsExcept can be specified, not both.");
		}
		
		return semanticProperties;
	}
	
	// --------------------------------------------------------------------------------------------
	
	public static DualInputSemanticProperties readDualConstantAnnotations(UserCodeWrapper<?> udf) {
		DualInputSemanticProperties semanticProperties = new DualInputSemanticProperties();

		// get readSet annotation from stub
		ConstantFieldsFirst constantSet1Annotation = udf.getUserCodeAnnotation(ConstantFieldsFirst.class);
		ConstantFieldsSecond constantSet2Annotation = udf.getUserCodeAnnotation(ConstantFieldsSecond.class);
		
		// extract readSets from annotations
		if(constantSet1Annotation != null) {
			for(int value: constantSet1Annotation.value()) {
				semanticProperties.addForwardedField1(value, value);
			}
		}
		
		if(constantSet2Annotation != null) {
			for(int value: constantSet2Annotation.value()) {
				semanticProperties.addForwardedField2(value, value);
			}
		}
		
		// get readSet annotation from stub
		ConstantFieldsFirstExcept notConstantSet1Annotation = udf.getUserCodeAnnotation(ConstantFieldsFirstExcept.class);
		ConstantFieldsSecondExcept notConstantSet2Annotation = udf.getUserCodeAnnotation(ConstantFieldsSecondExcept.class);
		
		// extract readSets from annotations
		if(notConstantSet1Annotation != null) {
			semanticProperties.addWrittenFields(new FieldSet(notConstantSet1Annotation.value()));
		}
		
		if(notConstantSet2Annotation != null) {
			semanticProperties.addWrittenFields(new FieldSet(notConstantSet2Annotation.value()));
		}
		
		
		if (notConstantSet1Annotation != null && constantSet1Annotation != null) {
			throw new RuntimeException("Either ConstantFieldsFirst or ConstantFieldsFirstExcept can be specified, not both.");
		}
		
		if (constantSet2Annotation != null && notConstantSet2Annotation != null) {
			throw new RuntimeException("Either ConstantFieldsSecond or ConstantFieldsSecondExcept can be specified, not both.");
		}
		
		return semanticProperties;
	}
	
	
}