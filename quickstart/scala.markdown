--- 
layout: inner_simple
title: Quick Start - Scala
---

# Quickstart: Scala
    

## Preparation

You need to install [Maven](http://maven.apache.org/) and Java 7 first.

## Create Project files

Use this command to create the project structure. The script internally uses Maven archetypes to generate the files.

```
curl https://raw.github.com/stratosphere/stratosphere-quickstart/master/quickstart-scala.sh | bash
```

If you want to name your newly created project, use the following command. It will interactively ask you for the groupId, artifactId and package name.

```
mvn archetype:generate                              \
   -DarchetypeGroupId=eu.stratosphere               \
   -DarchetypeArtifactId=quickstart-scala           \
   -DarchetypeVersion=0.4-SNAPSHOT                  \
   -DarchetypeCatalog=https://oss.sonatype.org/content/repositories/snapshots/
```

## Inspect Project

Switch into the directory of your newly created project. If you've used the `curl` approach, the directory is called `quickstart`. Otherwise, it has the name of your artifactId.

```
    cd quickstart
```

The `quickstart` project is a Maven project. We strongly recommend to import this project into your IDE. If you use eclipse you will need the following plugins:

Eclipse 4.x:

  * scala-ide: http://download.scala-ide.org/sdk/e38/scala210/stable/site
  * m2eclipse-scala: http://alchim31.free.fr/m2e-scala/update-site
  * build-helper-maven-plugin: https://repository.sonatype.org/content/repositories/forge-sites/m2e-extras/0.15.0/N/0.15.0.201206251206/

Eclipse 3.7:

  * scala-ide: http://download.scala-ide.org/sdk/e37/scala210/stable/site
  * m2eclipse-scala: http://alchim31.free.fr/m2e-scala/update-site
  * build-helper-maven-plugin: https://repository.sonatype.org/content/repositories/forge-sites/m2e-extras/0.14.0/N/0.14.0.201109282148/

With these you can import the maven project using the import dialog of eclipse.

Browse to start writing your job.

```
./src/main/scala/eu/stratosphere/quickstart/Job.scala
```

Please note the `main()` method in the `RunJob` object, this can be used to run the job on a local cluster for debugging/testing.

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

