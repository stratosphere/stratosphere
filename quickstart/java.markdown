--- 
layout: inner_simple
title: Quick Start - Java
---

# Quickstart: Java
    

## Preparation

You need to install [Maven](http://maven.apache.org/) and Java 7 first.

## Create Project files

Use this command to create the project structure. The script internally uses Maven archetypes to generate the files.

```
curl https://raw.github.com/stratosphere/stratosphere-quickstart/master/quickstart.sh | bash
```

If you want to name your newly created project, use the following command. It will interactively ask you for the groupId, artifactId and package name.

```
mvn archetype:generate                              \
   -DarchetypeGroupId=eu.stratosphere               \
   -DarchetypeArtifactId=quickstart-java            \
   -DarchetypeVersion=0.4-SNAPSHOT                  \
   -DarchetypeCatalog=https://oss.sonatype.org/content/repositories/snapshots/
```

## Inspect Project

Switch into the directory of your newly created project. If you've used the `curl` approach, the directory is called `quickstart`. Otherwise, it has the name of your artifactId.

```
    cd quickstart
```

The `quickstart` project is a Maven project. We strongly recommend to import this project into your IDE. If you use Eclipse, the [m2e](http://www.eclipse.org/m2e/) allows to [import Maven projects](http://books.sonatype.com/m2eclipse-book/reference/creating-sect-importing-projects.html#fig-creating-import). Other IDEs such as IntelliJ also support this.


Browse to start writing your job.

```
./src/main/java/eu/stratosphere/quickstart/Job.java
```

Please note the `main()` method in this class, which allows you to start Stratosphere in a development/testing mode.

## Build Project

If you want to build your project, go into the `quickstart` directory in issue the following command.

```
mvn clean package
```

You will find a jar that runs on every Stratosphere cluster right here:

```
./target/stratosphere-project-0.1-SNAPSHOT.jar
```


## Next Steps

Write your application!

If you have any troubles, ask on our GitHub page (using an Issue) or on our [Mailing list](https://groups.google.com/forum/#!forum/stratosphere-dev).