/***********************************************************************************************************************
 *
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
 *
 **********************************************************************************************************************/
package eu.stratosphere.api.java.typeutils;


import java.io.ObjectOutputStream;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;


import eu.stratosphere.api.common.InvalidProgramException;
import eu.stratosphere.api.common.io.InputFormat;
import eu.stratosphere.api.java.functions.*;
import eu.stratosphere.api.java.tuple.Tuple;
import eu.stratosphere.types.Value;
import org.apache.commons.lang3.Validate;


public class TypeExtractor {

	
	public static <X> TypeInformation<X> getMapReturnTypes(MapFunction<?, X> mapFunction) {
		Type returnType = getTemplateTypes (MapFunction.class, mapFunction.getClass(), 1);
		return createTypeInfo(returnType);
	}
	
	public static <X> TypeInformation<X> getFlatMapReturnTypes(FlatMapFunction<?, X> flatMapFunction) {
		Type returnType = getTemplateTypes (FlatMapFunction.class, flatMapFunction.getClass(), 1);
		return createTypeInfo(returnType);
	}
	
	public static <X> TypeInformation<X> getGroupReduceReturnTypes(GroupReduceFunction<?, X> groupReduceFunction) {
		Type returnType = getTemplateTypes (GroupReduceFunction.class, groupReduceFunction.getClass(), 1);
		return createTypeInfo(returnType);
	}
	
	public static <X> TypeInformation<X> getJoinReturnTypes(JoinFunction<?, ?, X> joinFunction) {
		Type returnType = getTemplateTypes (JoinFunction.class, joinFunction.getClass(), 2);
		return createTypeInfo(returnType);
	}

	public static <X> TypeInformation<X> getCoGroupReturnTypes(CoGroupFunction<?, ?, X> coGroupFunction) {
		Type returnType = getTemplateTypes (CoGroupFunction.class, coGroupFunction.getClass(), 2);
		return createTypeInfo(returnType);
	}

	public static <X> TypeInformation<X> getCrossReturnTypes(CrossFunction<?, ?, X> crossFunction) {
		Type returnType = getTemplateTypes (CrossFunction.class, crossFunction.getClass(), 2);
		return createTypeInfo(returnType);
	}

	public static <X> TypeInformation<X> getKeyExtractorType(KeySelector<?, X> extractor) {
		Type returnType = getTemplateTypes (KeySelector.class, extractor.getClass(), 1);
		return createTypeInfo(returnType);
	}
	
	public static <X> TypeInformation<X> extractInputFormatTypes(InputFormat<X, ?> format) {
		@SuppressWarnings("unchecked")
		Class<InputFormat<X, ?>> formatClass = (Class<InputFormat<X, ?>>) format.getClass();
		Type type = findGenericParameter(formatClass, InputFormat.class, 0);
		return getTypeInformation(type);
	}
	
	// --------------------------------------------------------------------------------------------
	//  Generic utility methods
	// --------------------------------------------------------------------------------------------
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <X> TypeInformation<X> createTypeInfo(Type t) {
		
		Type curT = t;
		if ((curT instanceof Class<?> && Tuple.class.isAssignableFrom((Class<?>) curT)) ||
				(curT instanceof ParameterizedType && Tuple.class.isAssignableFrom((Class<?>)((ParameterizedType) curT).getRawType()))) {
			
			// move up the class hierarchy until we have a ParameterizedType or a Tuple
			while(!(curT instanceof ParameterizedType) && !((Class)curT).equals(Tuple.class))  {
				// move up to super class
				curT = ((Class<?>)curT).getGenericSuperclass();
			}
			
			if(curT instanceof ParameterizedType) {
			
				ParameterizedType pt = (ParameterizedType) curT;
				
				Type raw = pt.getRawType();
				if (raw instanceof Class) {
					
					Type[] subtypes = pt.getActualTypeArguments();
					
					TypeInformation<?>[] tupleSubTypes = new TypeInformation<?>[subtypes.length];
					for (int i = 0; i < subtypes.length; i++) {
						tupleSubTypes[i] = createTypeInfo(subtypes[i]);
					}
					
					// TODO: Check that type that extends Tuple does not have additional fields.
					// Right now, these fields are not be serialized by the TupleSerializer. 
					// We might want to add an ExtendedTupleSerializer for that. 

					if (t instanceof Class<?>) {
						return new TupleTypeInfo(((Class<? extends Tuple>)t), tupleSubTypes);
					} else if (t instanceof ParameterizedType) {
						return new TupleTypeInfo(((Class<? extends Tuple>)((ParameterizedType)t).getRawType()), tupleSubTypes);
					}
				}
			}
			
		} else if (t instanceof Class) {
			// non tuple
			return getForClass((Class<X>) t);
		}
		
		return null;
	}


	@SuppressWarnings("unchecked")
	public static <X> TypeInformation<X> getForClass(Class<X> clazz) {
		Validate.notNull(clazz);

		// check for basic types
		{
			TypeInformation<X> basicTypeInfo = BasicTypeInfo.getInfoFor(clazz);
			if (basicTypeInfo != null) {
				return basicTypeInfo;
			}
		}

		// check for subclasses of Value
		if (Value.class.isAssignableFrom(clazz)) {
			Class<? extends Value> valueClass = clazz.asSubclass(Value.class);
			return (TypeInformation<X>) ValueTypeInfo.getValueTypeInfo(valueClass);
		}

		// check for subclasses of Tuple
		if (Tuple.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Type information extraction for tuples cannot be done based on the class.");
		}

		List<Field> fields = getAllDeclaredFields(clazz);
		List<PojoField> pojoFields = new ArrayList<PojoField>();
		for (Field field : fields) {
			if (!Modifier.isTransient(field.getModifiers())) {
				pojoFields.add(new PojoField(field, createTypeInfo(field.getType())));
			}
		}

		List<Method> methods = getAllDeclaredMethods(clazz);
		boolean containsReadObjectOrWriteObject = false;
		for (Method method : methods) {
			if (method.getName().equals("readObject") || method.getName().equals("writeObject")) {
				containsReadObjectOrWriteObject = true;
				break;
			}
		}

		if (!containsReadObjectOrWriteObject) {
			return new PojoTypeInfo<X>(clazz, pojoFields);
		}

		// return a generic type
		return new GenericTypeInfo<X>(clazz);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <X> TypeInformation<X> getForObject(X value) {
		Validate.notNull(value);

		// check if we can extract the types from tuples, otherwise work with the class
		if (value instanceof Tuple) {
			Tuple t = (Tuple) value;
			int numFields = t.getArity();

			TypeInformation<?>[] infos = new TypeInformation[numFields];
			for (int i = 0; i < numFields; i++) {
				Object field = t.getField(i);

				if (field == null) {
					throw new InvalidProgramException("Automatic type extraction is not possible on canidates with null values. " +
							"Please specify the types directly.");
				}

				infos[i] = getForObject(field);
			}


			return (TypeInformation<X>) new TupleTypeInfo(value.getClass(), infos);
		}
		else {
			return getForClass((Class<X>) value.getClass());
		}
	}

	// recursively determine all declared fields
	private static List<Field> getAllDeclaredFields(Class<?> clazz) {
		List<Field> result = new ArrayList<Field>();
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				result.add(field);
			}
			clazz = clazz.getSuperclass();
		}
		return result;
	}

	// recursively determine all declared methods
	private static List<Method> getAllDeclaredMethods(Class<?> clazz) {
		List<Method> result = new ArrayList<Method>();
		while (clazz != null) {
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				result.add(method);
			}
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	
	public static ParameterizedType getTemplateTypesChecked(Class<?> baseClass, Class<?> clazz, int pos) {
		Type t = getTemplateTypes(baseClass, clazz, pos);
		if (t instanceof ParameterizedType) {
			return (ParameterizedType) t;
		} else {
			throw new InvalidTypesException("The generic function type is no Tuple.");
		}
	}
	
	
	public static Type getTemplateTypes(Class<?> baseClass, Class<?> clazz, int pos) {
		return getTemplateTypes(getSuperParameterizedType(baseClass, clazz))[pos];
	}
	
	public static Type[] getTemplateTypes(ParameterizedType paramterizedType) {
		Type[] types = new Type[paramterizedType.getActualTypeArguments().length];
		
		int i = 0;
		for (Type templateArgument : paramterizedType.getActualTypeArguments()) {
			types[i++] = templateArgument;
		}
		return types;
	}
	
	public static ParameterizedType getSuperParameterizedType(Class<?> baseClass, Class<?> clazz) {
		Type type = clazz.getGenericSuperclass();
//		while (true) {
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				if (parameterizedType.getRawType().equals(baseClass)) {
				  return parameterizedType;
				}
			}

			if (clazz.getGenericSuperclass() == null) {
				throw new IllegalArgumentException();
			}

			type = clazz.getGenericSuperclass();
			clazz = clazz.getSuperclass();
//		}
		throw new IllegalArgumentException("Generic function base class must be immediate super class.");
	}
	
	public static Class<?>[] getTemplateClassTypes(ParameterizedType paramterizedType) {
		Class<?>[] types = new Class<?>[paramterizedType.getActualTypeArguments().length];
		int i = 0;
		for (Type templateArgument : paramterizedType.getActualTypeArguments()) {
			types[i++] = (Class<?>) templateArgument;
		}
		return types;
	}
	
	
	public static <X> TypeInformation<X> getTypeInformation(Type type) {
		return null;
	}
	
	public static Type findGenericParameter(Class<?> clazz, Class<?> genericSuperClass, int genericArgumentNum) {
		return null;
	}
	
	
	
	
	private TypeExtractor() {}
}
