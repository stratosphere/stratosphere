---
layout: documentation
---
WordCount
=========

Counting words is the classic example to introduce parallelized data
processing with MapReduce. It demonstrates how the frequencies of words
in a document collection are computed.   
 The source code of this example can be found
[here](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/wordcount/WordCount.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/wordcount/WordCount.java").

Algorithm description
---------------------

The WordCount algorithm works as follows:

1.  The text input data is read line by line
2.  Each line is cleansed (removal of non-word characters, conversion to
    lower case, etc.) and tokenized by words.
3.  Each word is paired with a partial count of “1”.
4.  All pairs are grouped by the word and the total group count is
    computed by summing up partial counts.

A detailed description of the implementation of the WordCount algorithm
for Hadoop MapReduce can be found at [Hadoop's Map/Reduce
Tutorial](http://hadoop.apache.org/common/docs/r0.20.2/mapred_tutorial.html#Example%3A+WordCount+v1.0 "http://hadoop.apache.org/common/docs/r0.20.2/mapred_tutorial.html#Example%3A+WordCount+v1.0").

PACT Program
------------

The [example PACT
program](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/wordcount/WordCount.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-examples/src/main/java/eu/stratosphere/pact/example/wordcount/WordCount.java")
implements the above described algorithm.

1.  The PACT program starts with an input data source. The
    DataSourceContract produces a list of key-value pairs. Each line of
    the input file becomes a value. The key is not used here and hence
    set to `NULL`.
2.  The Map PACT is used to split each line into single words. The user
    code tokenizes the line by whitespaces (`' `', `'\t`', …) into
    single words. Each word is emitted in an independent key-value pair,
    where the key is the word itself and the value is an Integer “1”.
3.  The Reduce PACT implements the reduce and combine methods. The
    ReduceStub is annotated with `@Combinable` to use the combine
    method. The combine method computes partial sums over the “1”-values
    emitted by the Map contract, the reduce method sums these partial
    sums and obtains the final count for each word.
4.  The final DataSink contract writes the results back.

[![](media/wiki/wordcount_pactprogram.png)](media/wiki/wordcount_pactprogram.png "wiki:wordcount_pactprogram.png")

Program Arguments
-----------------

Three arguments must be provided to the `getPlan()` method of the
example job:

1.  `int noSubStasks`: Degree of parallelism of all tasks.
2.  `String inputPath`: Path to the input data.
3.  `String outputPath`: Destination path for the result.

See
[here](executepactprogram.html "executepactprogram")
for details on how to specify paths for Nephele.

Test Data
---------

Any plain text file can be used as input for the the WordCount example.
If you do not have a text file at hand, you can download some literature
from the [Gutenberg
Project](http://www.gutenberg.org "http://www.gutenberg.org"). For
example

    wget -O ~/hamlet.txt http://www.gutenberg.org/cache/epub/1787/pg1787.txt

will download Hamlet and put the text into a file called `hamlet.txt` in
your home directory.
