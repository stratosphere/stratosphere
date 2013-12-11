package eu.stratosphere.nephele.services.accumulators;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import eu.stratosphere.nephele.io.IOReadableWritable;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.util.SerializableHashMap;

/**
 * 
 * 
 * This class doesn't use the existing {@link SerializableHashMap} since this
 * requires to transform the string to a StringRecord. It also sends the class
 * of the key, which is constant in our case.
 */
public class AccumulatorEvent implements IOReadableWritable {
  
  private JobID jobID;
  
  private Map<String, Accumulator<?, ?>> accumulators = new HashMap<String, Accumulator<?,?>>();
  
//  public AccumulatorEvent() {
//    
//  }

  public AccumulatorEvent(JobID jobID, Map<String, Accumulator<?,?>> accumulators) {
    this.accumulators = accumulators;
    this.jobID = jobID;
  }
  
  public JobID getJobID() {
    return this.jobID;
  }
  
  public Map<String, Accumulator<?, ?>> getAccumulators() {
    return this.accumulators;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    System.out.println("WRITE");
    jobID.write(out);
//    out.write(accumulators.size());
//    for (Map.Entry<String, Accumulator<?,?>> entry : this.accumulators.entrySet()) {
//      StringRecord.writeString(out, entry.getKey());
//      StringRecord.writeString(out, entry.getValue().getClass().getName());
//      entry.getValue().write(out);
//    }
  }

  @Override
  public void read(DataInput in) throws IOException {
    System.out.println("READ");
    jobID = new JobID();
    jobID.read(in);
//    int numberOfMapEntries = in.readInt();
//    this.accumulators = new HashMap<String, Accumulator<?,?>>(numberOfMapEntries);
//    
//    for (int i = 0; i < numberOfMapEntries; i++) {
//
//      String key = StringRecord.readString(in);
//
//      final String valueType = StringRecord.readString(in);
//      Class<Accumulator<?,?>> valueClass = null;
//      try {
////        valueClass = (Accumulator<?,?>) Class.forName(valueType);
//        Accumulator<?,?> test = instantiate((Class<Accumulator<?,?>>)Class.forName(valueType), (Class<Accumulator<?,?>>)Class.forName("Accumulator<?,?>"));
////        valueClass = Class.forName(valueType).asSubclass(Accumulator.class);
//        
//      } catch (ClassNotFoundException e) {
//        throw new IOException(StringUtils.stringifyException(e));
//      }
//
//      Accumulator<?,?> value = null;
//      try {
//        value = valueClass.newInstance();
//      } catch (Exception e) {
//        throw new IOException(StringUtils.stringifyException(e));
//      }
//      value.read(in);
//
//      this.accumulators.put(key, value);
//    }
  }
  
  public static <T> T instantiate(Class<T> clazz, Class<? super T> castTo) {
    if (clazz == null) {
      throw new NullPointerException();
    }
    
    // check if the class is a subclass, if the check is required
    if (castTo != null && !castTo.isAssignableFrom(clazz)) {
      throw new RuntimeException("The class '" + clazz.getName() + "' is not a subclass of '" + 
        castTo.getName() + "' as is required.");
    }
    
    // try to instantiate the class
    try {
      return clazz.newInstance();
    }
    catch (InstantiationException iex) {
      // check for the common problem causes
      checkForInstantiation(clazz);
      
      // here we are, if non of the common causes was the problem. then the error was
      // most likely an exception in the constructor or field initialization
      throw new RuntimeException("Could not instantiate type '" + clazz.getName() + 
          "' due to an unspecified exception: " + iex.getMessage(), iex);
    }
    catch (IllegalAccessException iaex) {
      // check for the common problem causes
      checkForInstantiation(clazz);
      
      // here we are, if non of the common causes was the problem. then the error was
      // most likely an exception in the constructor or field initialization
      throw new RuntimeException("Could not instantiate type '" + clazz.getName() + 
          "' due to an unspecified exception: " + iaex.getMessage(), iaex);
    }
    catch (Throwable t) {
      String message = t.getMessage();
      throw new RuntimeException("Could not instantiate type '" + clazz.getName() + 
        "' Most likely the constructor (or a member variable initialization) threw an exception" + 
        (message == null ? "." : ": " + message), t);
    }
  }
  
  /**
   * Performs a standard check whether the class can be instantiated by {@code Class#newInstance()}.
   * 
   * @param clazz The class to check.
   * @throws RuntimeException Thrown, if the class cannot be instantiated by {@code Class#newInstance()}.
   */
  public static void checkForInstantiation(Class<?> clazz) {
    final String errorMessage;
    
    if (!isPublic(clazz)) {
      errorMessage = "The class is not public.";
    } else if (!isProperClass(clazz)) {
      errorMessage = "The class is no proper class, it is either abstract, an interface, or a primitive type.";
    } else if (isNonStaticInnerClass(clazz)) {
      errorMessage = "The class is an inner class, but not statically accessible.";
    } else if (!hasPublicNullaryConstructor(clazz)) {
      errorMessage = "The class has no (implicit) public nullary constructor, i.e. a constructor without arguments.";
    } else {
      errorMessage = null; 
    }
    
    if (errorMessage != null) {
      throw new RuntimeException("The class '" + clazz.getName() + "' is not instantiable: " + errorMessage);
    }
  }
  public static boolean isPublic(Class<?> clazz) {
    return Modifier.isPublic(clazz.getModifiers());
  }
  public static boolean isProperClass(Class<?> clazz) {
    int mods = clazz.getModifiers();
    return !(Modifier.isAbstract(mods) || Modifier.isInterface(mods) || Modifier.isNative(mods));
  }

  /**
   * Checks, whether the class is an inner class that is not statically accessible. That is especially true for
   * anonymous inner classes.
   * 
   * @param clazz The class to check.
   * @return True, if the class is a non-statically accessible inner class.
   */
  public static boolean isNonStaticInnerClass(Class<?> clazz) {
    if (clazz.getEnclosingClass() == null) {
      // no inner class
      return false;
    } else {
      // inner class
      if (clazz.getDeclaringClass() != null) {
        // named inner class
        return !Modifier.isStatic(clazz.getModifiers());
      } else {
        // anonymous inner class
        return true;
      }
    }
  }
  public static boolean hasPublicNullaryConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getConstructors();
    for (int i = 0; i < constructors.length; i++) {
      if (constructors[i].getParameterTypes().length == 0 && 
          Modifier.isPublic(constructors[i].getModifiers())) {
        return true;
      }
    }
    return false;
  }
  
  
//  private void <V,R> assignSingle(Class<Accumulator<?,?>> valueClass) {
//     
//  }
  
}
