
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
package eu.stratosphere.api.java.functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.common.primitives.Ints;

import eu.stratosphere.api.common.operators.DualInputSemanticProperties;
import eu.stratosphere.api.common.operators.SingleInputSemanticProperties;
import eu.stratosphere.api.common.operators.util.UserCodeWrapper;
import eu.stratosphere.api.java.typeutils.TypeInformation;


/**
 * This class defines the semantic assertions that can be added to functions.
 * The assertions are realized as java annotations, to be added to the class declaration of
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
 * Be aware that some annotations should only be used for functions with as single input 
 * ({@link MapFunction}, {@link ReduceFunction}) and some only for stubs with two inputs 
 * ({@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction}).
 */
public class FunctionAnnotationJapi {
	
	/**
	 * Specifies the fields of an input record that are unchanged in the output of 
	 * a stub with a single input ( {@link MapFunction}, {@link ReduceFunction}).
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * The annotation takes two int arrays. The first specifies the fields that are constant in the input, the second
	 * int array is optional and contains the position of the fields in the output. If the 'from' array is not given, it
	 * is expected that the position of the fields also remains constant.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a program can not be guaranteed.
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
		int[] from();
		int[] to() default {};
	}
	
	/**
	 * Specifies that all fields of an input record that are unchanged in the output of 
	 * a {@link MapFunction}, or {@link ReduceFunction}).
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a program can not be guaranteed.
	 * So if in doubt, do not add a field to this set.
	 * </b>
	 * 
	 * This annotation is mutually exclusive with the {@link ConstantFieldsExcept} annotation.
	 * 
	 * If this annotation and the {@link ConstantFieldsExcept} annotation is not set, it is 
	 * assumed that <i>no</i> field is constant.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AllFieldsConstants {}
	
	/**
	 * Specifies the fields of an input record of the first input that are unchanged in 
	 * the output of a stub with two inputs ( {@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction})
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * The annotation takes two int arrays. The first specifies the fields that are constant in the input, the second
	 * int array is optional and contains the position of the fields in the output. If the 'from' array is not given, it
	 * is expected that the position of the fields also remains constant.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a program can not be guaranteed.
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
		int[] from();
		int[] to() default {};
	}
	
	/**
	 * Specifies the fields of an input record of the second input that are unchanged in 
	 * the output of a stub with two inputs ( {@link CrossFunction}, {@link JoinFunction}, {@link CoGroupFunction})
	 * 
	 * A field is considered to be constant if its value is not changed and copied to the same position of 
	 * output record.
	 * 
	 * The annotation takes two int arrays. The first specifies the fields that are constant in the input, the second
	 * int array is optional and contains the position of the fields in the output. If the 'from' array is not given, it
	 * is expected that the position of the fields also remains constant.
	 * 
	 * <b>
	 * It is very important to follow a conservative strategy when specifying constant fields.
	 * Only fields that are always constant (regardless of value, stub call, etc.) to the output may be 
	 * inserted! Otherwise, the correct execution of a program can not be guaranteed.
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
		int[] from();
		int[] to() default {};
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
	 * inserted! Otherwise, the correct execution of a program can not be guaranteed.
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
	 * inserted! Otherwise, the correct execution of a program can not be guaranteed.
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
	 * inserted! Otherwise, the correct execution of a program can not be guaranteed.
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
	private FunctionAnnotationJapi() {}
	
	// --------------------------------------------------------------------------------------------
	//                                   Function Annotation Handling
	// --------------------------------------------------------------------------------------------
	/**
	 * Reads the annotations of a user defined function with one input and returns semantic properties according to the constant fields annotated.
	 * @param udf	The user defined function.
	 * @param input	Type information of the operator input.
	 * @param output	Type information of the operator output.
	 * @return	The DualInputSemanticProperties containing the constant fields.
	 */
	
	
	public static SingleInputSemanticProperties readSingleConstantAnnotations(UserCodeWrapper<?> udf, TypeInformation<?> input, TypeInformation<?> output) {
		if (!input.isTupleType() || !output.isTupleType()) {
			return null;
		}

		
		AllFieldsConstants allConstants = udf.getUserCodeAnnotation(AllFieldsConstants.class);
		ConstantFields constantSet = udf.getUserCodeAnnotation(ConstantFields.class);
		ConstantFieldsExcept notConstantSet = udf.getUserCodeAnnotation(ConstantFieldsExcept.class);
		
		int inputArity = input.getArity();
		int outputArity = output.getArity();
		


		if (notConstantSet != null && (constantSet != null || allConstants != null)) {
			throw new RuntimeException("Either ConstantFields or ConstantFieldsExcept can be specified, not both.");
		}
		SingleInputSemanticProperties semanticProperties = new SingleInputSemanticProperties();

		// extract notConstantSet from annotation
		if (notConstantSet != null) {
			for (int i = 0; i < inputArity && i < outputArity; i++) {
				if (!Ints.contains(notConstantSet.value(), i)) {
					semanticProperties.addForwardedField(i, i);
				};
			}
		}
		
		// extract notConstantSet from annotation
		if (allConstants != null) {
			for (int i = 0; i < inputArity && i < outputArity; i++) {
					semanticProperties.addForwardedField(i, i);
			}
		}
		
		
		// extract constantSet from annotation
		if (constantSet != null) {
			if (constantSet.to().length == 0) {
				for (int value: constantSet.from()) {
					semanticProperties.addForwardedField(value,value);
				}
			} else if (constantSet.from().length == constantSet.to().length) {
				for (int i = 0; i < constantSet.from().length; i++) {
					semanticProperties.addForwardedField(constantSet.from()[i], constantSet.to()[i]);
				}
			} else {
				throw new RuntimeException("Field 'from' and 'to' of the annotation should have the same length.");
			}
		}
		
		return semanticProperties;
		
	}
	
	// --------------------------------------------------------------------------------------------
	/**
	 * Reads the annotations of a user defined function with two inputs and returns semantic properties according to the constant fields annotated.
	 * @param udf	The user defined function.
	 * @param input1	Type information of the first operator input.
	 * @param input2	Type information of the second operator input.
	 * @param output	Type information of the operator output.
	 * @return	The DualInputSemanticProperties containing the constant fields.
	 */
	
	public static DualInputSemanticProperties readDualConstantAnnotations(UserCodeWrapper<?> udf, TypeInformation<?> input1, TypeInformation<?> input2, TypeInformation<?> output) {
		if (!input1.isTupleType() || !input2.isTupleType() || !output.isTupleType()) {
			return null;
		}
		
		int input1Arity = input1.getArity();
		int input2Arity = input2.getArity();
		int outputArity = output.getArity();
				
		// get readSet annotation from stub
		ConstantFieldsFirst constantSet1 = udf.getUserCodeAnnotation(ConstantFieldsFirst.class);
		ConstantFieldsSecond constantSet2= udf.getUserCodeAnnotation(ConstantFieldsSecond.class);
			
		// get readSet annotation from stub
		ConstantFieldsFirstExcept notConstantSet1 = udf.getUserCodeAnnotation(ConstantFieldsFirstExcept.class);
		ConstantFieldsSecondExcept notConstantSet2 = udf.getUserCodeAnnotation(ConstantFieldsSecondExcept.class);
			
		
		if (notConstantSet1 != null && constantSet1 != null) {
			throw new RuntimeException("Either ConstantFieldsFirst or ConstantFieldsFirstExcept can be specified, not both.");
		}
		
		if (constantSet2 != null && notConstantSet2 != null) {
			throw new RuntimeException("Either ConstantFieldsSecond or ConstantFieldsSecondExcept can be specified, not both.");
		}
		
		DualInputSemanticProperties semanticProperties = new DualInputSemanticProperties();
		
		// extract readSets from annotations
		if(notConstantSet1 != null) {
			for (int i = 0; i < input1Arity && i < outputArity; i++) {
				if (!Ints.contains(notConstantSet1.value(), i)) {
					semanticProperties.addForwardedField1(i, i);;
				};
			}
		}
			
		if(notConstantSet2 != null) {
			for (int i = 0; i < input2Arity && i < outputArity; i++) {
				if (!Ints.contains(notConstantSet2.value(), i)) {
					semanticProperties.addForwardedField2(i, i);;
				};
			}		
		}
				
		// extract readSets from annotations
		if (constantSet1 != null) {
			if (constantSet1.to().length == 0) {
				for (int value: constantSet1.from()) {
					semanticProperties.addForwardedField1(value,value);
				}
			} else if (constantSet1.from().length == constantSet1.to().length) {
				for (int i = 0; i < constantSet1.from().length; i++) {
					semanticProperties.addForwardedField1(constantSet1.from()[i], constantSet1.to()[i]);
				}
			} else {
				throw new RuntimeException("Field 'from' and 'to' of the annotation should have the same length.");
			}
		}
				
		if (constantSet2 != null) {
			if (constantSet2.to().length == 0) {
				for (int value: constantSet2.from()) {
					semanticProperties.addForwardedField1(value,value);
				}
			} else if (constantSet2.from().length == constantSet2.to().length) {
				for (int i = 0; i < constantSet2.from().length; i++) {
					semanticProperties.addForwardedField2(constantSet2.from()[i], constantSet2.to()[i]);
				}
			} else {
				throw new RuntimeException("Field 'from' and 'to' of the ConstantFields annotation should have the same length.");
			}
		}
				
		return semanticProperties;
	}
	
	
		
		
	}

