---
layout: post
title:  "Accessing Data Stored in MongoDB with Stratosphere"
date:   2014-01-28 9:00:00
categories: news blog tutorial
---

We recently merged a [pull request](https://github.com/stratosphere/stratosphere/pull/437) that allows you to use any existing Hadoop [InputFormat](http://developer.yahoo.com/hadoop/tutorial/module5.html#inputformat) with Stratosphere. So you can now (in the `0.5-SNAPSHOT` and upwards versions) define a Hadoop-based data source:

```java
HadoopDataSource source = new HadoopDataSource(new TextInputFormat(), new JobConf(), "Input Lines");
TextInputFormat.addInputPath(source.getJobConf(), new Path(dataInput));
```

We describe in the following article how to access data stored in [MongoDB](http://www.mongodb.org/) with Stratosphere. This allows users to join data from multiple sources (e.g. MonogDB and HDFS) or perform machine learning with the documents stored in MongoDB.

The approach here is to use the `MongoInputFormat` that was developed for Apache Hadoop but now also runs with Stratosphere.

```java
JobConf conf = new JobConf();
conf.set("mongo.input.uri","mongodb://localhost:27017/enron_mail.messages");
HadoopDataSource src = new HadoopDataSource(new MongoInputFormat(), conf, "Read from Mongodb", new WritableWrapperConverter());
```

### Example Program
The example program reads data from the [enron dataset](http://www.cs.cmu.edu/~enron/) that contains about 500k internal e-mails. The data is stored in MongoDB and the Stratosphere program counts the number of e-mails per day.

The complete code of this sample program is available on [GitHub](https://github.com/stratosphere/stratosphere-mongodb-example).

#### Prepare MongoDB and the Data

 - Install MongoDB
 - Download the enron dataset from [their website](http://mongodb-enron-email.s3-website-us-east-1.amazonaws.com/).
 - Unpack and load it

 ```bash
 bunzip2 enron_mongo.tar.bz2
 tar xvf enron_mongo.tar
 mongorestore dump/enron_mail/messages.bson
 ```

We used [Robomongo](http://robomongo.org/) to visually examine the dataset stored in MongoDB.

<img src="{{ site.baseurl }}/img/blog/robomongo.png" style="width:90%;margin:15px">


#### Build `MongoInputFormat`

MongoDB offers an InputFormat for Hadoop on their [GitHub page](https://github.com/mongodb/mongo-hadoop). The code is not available in any Maven repository, so we have to build the jar file on our own.

- Check out the repository

```
git clone https://github.com/mongodb/mongo-hadoop.git
cd mongo-hadoop
```

- Set the appropriate Hadoop version in the `build.sbt`, we used `1.1`.

```bash
hadoopRelease in ThisBuild := "1.1"
```
- Build the input format

```bash
./sbt package
```

The jar-file is now located in `core/target`.

#### The Stratosphere Program

Now we have everything prepared to run the Stratosphere program. I only ran it on my local computer, out of Eclipse. To do that, check out the code ...

```bash
git clone https://github.com/stratosphere/stratosphere-mongodb-example.git
```

... and import it as a Maven project into your Eclipse. You have to manually add the previously built mongo-hadoop jar-file as a dependency.
You can now press the "Run" button and see how Stratosphere executes the little program. It was running for about 8 seconds on the 1.5 GB dataset.


The result (located in `/tmp/enronCountByDay`) now looks like this.

```
11,Fri Sep 26 10:00:00 CEST 1997
154,Tue Jun 29 10:56:00 CEST 1999
292,Tue Aug 10 12:11:00 CEST 1999
185,Thu Aug 12 18:35:00 CEST 1999
26,Fri Mar 19 12:33:00 CET 1999
```

There is one thing left I want to point out here. MongoDB represents objects stored in the database as JSON-documents. Since Stratosphere's standard types do not support JSON documents, I was using the `WritableWrapper` here. This wrapper allows to use any Hadoop datatype with Stratosphere.

The following code example shows how the JSON-documents are accessed in Stratosphere.

```java
public void map(Record record, Collector<Record> out) throws Exception {
	Writable valWr = record.getField(1, WritableWrapper.class).value();
	BSONWritable value = (BSONWritable) valWr;
	Object headers = value.getDoc().get("headers");
	BasicDBObject headerOb = (BasicDBObject) headers;
	String date = (String) headerOb.get("Date");
	// further date processing
}
```

Please use the comments if you have questions or if you want to showcase your own MongoDB-Stratosphere integration.
<br><br>
<small>Written by Robert Metzger ([@rmetzger_](https://twitter.com/rmetzger_)).</small>
