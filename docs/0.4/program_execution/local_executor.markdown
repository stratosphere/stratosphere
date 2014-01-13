---
layout: inner_docs_v04
title:  "Local Executor"
---

## Local Executor

Stratosphere can run on a single machine, even in a single Java Virtual Machine. This allows users to test and debug Stratosphere programs (plans) locally.

### Maven Dependency

If you are developing your program in a Maven project, you have to add the `stratosphere-clients` module using this dependency:

```xml
<dependency>
  <groupId>eu.stratosphere</groupId>
  <artifactId>stratosphere-clients</artifactId>
  <version>{{site.current_stable}}</version>
</dependency>
```

### Code Example:

The following code shows how you would use the `LocalExecutor` with the Wordcount example:

```java
public static void main(String[] args) throws Exception {
	WordCount wc = new WordCount();
	Plan plan = wc.getPlan(args);
	LocalExecutor.execute(plan);
}
```

If you have added this code to your program class, you can use the "Run" button of your IDE to execute the plan locally using Stratosphere.

If you are running Stratosphere programs with the `LocalExecutor` from your IDE, you can also use it to debug your plan!
You can either use `System.out.println()` to write out some internal variables or you can use the debugger. So it is possible to set breakpoints within `map()`, `reduce()` and all the other methods.

The `execute()` method returns a `JobExecutionResult` object which contains the program runtime and the accumulator results.

Limitations of the `LocalExecutor`:

 * The LocalExecutor does not start the Web interface
 * We have not tested the LocalExecutor with `hdfs://` files. (We suggest to use `file://` for local files.)



### LocalDistributedExecutor

Stratosphere also offers a `LocalDistributedExecutor` which starts multiple TaskManagers within one JVM. The standard `LocalExecutor` starts one JobManager and one TaskManager in one JVM.
With the `LocalDistributedExecutor` you can define the number of TaskManagers to start.

```java
public static void main(String[] args) throws Exception {
	WordCount wc = new WordCount();
	Plan plan = wc.getPlan(args);
	LocalDistributedExecutor lde = new LocalDistributedExecutor();
	lde.startNephele(2); // start two TaskManagers
	lde.run(plan);
}
```

Pass the number of TaskManagers to the method `LocalDistributedExecutor.startNephele(int)`


