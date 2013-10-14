---
layout: documentation
---
Input and Output Formats
========================

Input- and Output Formats are the sources and sinks of the records in
your Pact Program dataflow and are hence the user defined functionality
of the DataSources and DataSinks in your Pact Program.

Input Formats
=============

Input formats are a generic description of how your records enter the
dataflow in a parallel fashion. All input formats implement the
interface
*[eu.stratosphere.pact.common.generic.io.InputFormat](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/generic/io/InputFormat.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/generic/io/InputFormat.java")*.
The input format describes two aspects:

1.  How to partition an input into *splits* that are processed in
    parallel.
2.  How to generate records from a split.

The input formats base classes are very generic to enable a generic type
runtime. However, all subclasses that programmers typically extend are
typed to PactRecord, so we will use the typed signatures in the
following.

### Creating Splits

The logic that generates splits is invoked on the Nephele JobManager.
The InputFormat is instantiated (with its null-ary constructor) and
configured via the **configure(Configuration)** method. Inside the
configure method, parameters that define the behavior of the input
format are read from the passed configuration object. For example, the
file input format reads the path to the file from the configuration.

The actual split generation happens in the method *InputSplit[]
createInputSplits(int minNumSplits)*. The method creates an array of
*[eu.stratosphere.nephele.template.InputSplit](https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/template/InputSplit.java "https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/template/InputSplit.java")*s
(or a subclass thereof) that describe the partitions of the data. The
input split interface itself does not provide more information than the
number of the split, and input formats with sophisticated partitioning
typically implement more complex subclasses of InputSplit. The file
input format uses for example the more specialized
*[eu.stratosphere.nephele.fs.FileInputSplit](https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/fs/FileInputSplit.java "https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/fs/FileInputSplit.java")*,
which holds information about the file path, the start position in the
file's byte stream, the partition length, and the number of hosts where
the split is locally accessible. The internal semantics of the split are
not interpreted by the JobManager, but only by the input format itself,
when it is assigned an input split.

The *InputSplit[] createInputSplits(int minNumSplits)* method is given a
hint how many splits to create at least. It may create fewer, but that
might result in some parallel instances not getting a split assigned.

### Creating Records

In the parallel deployment, the input format class is instantiated once
for each parallel instance. As before, it is configured via the
**configure(Configuration)** method after instantiation. Splits are
assigned to the parallel instances in a lazy fashion - The first one to
be available gets the next split.

Once an input format is assigned a split, its **open(InputSplit)**
method is invoked. This method must set up the internal state such that
the record generation can start. As an example, the file input format
reads the path of the file, opens a file input stream and seeks that
stream to the position that marks the beginning of the split. The input
format for delimited records
*[eu.stratosphere.pact.common.io.DelimitedInputFormat](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/DelimitedInputFormat.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/DelimitedInputFormat.java")*
searches the file stream for the occurrence of the next delimiter.

During the actual record generation process, the method **boolean
nextRecord(PactRecord record)** is invoked as long as a preceding call
to **boolean reachedEnd()** returned false. The method is always given
an instance of the PactRecord, into which the contents of the record is
to be put. The return value indicates whether the record is valid. The
system skips records with a return value of *false* - that way,
individual erroneous records can be skipped.

Because Stratosphere uses mutable objects (for performance reasons), the
given PactRecord object is typically the same each time. The input
format must not use the object to hold whatever state across function
invocations.

Finally, when no more records are read from the split, the **close()**
method is called.

Note that the same instance of the input format is used for multiple
splits, if a parallel instance is assigned more than one input split. In
that case, a closed input format is opened again with another split, so
it must make sure that closing opening cleans and resets internal state
accordingly.

* * * * *

### Input Formats Examples

**Whole File Split Input Format**

A simple change to the file input format is to create only one split per
file. That is useful, if for example the data is encoded in a fashion
that does not permit a read to start in the middle of the file. To
enforce such split generation, simply override the split creation method
as shown below:

    public FileInputSplit[] createInputSplits(int minNumSplits) throws IOException {
        final Path path = this.filePath;
        final FileSystem fs = path.getFileSystem();
     
        final FileStatus pathFile = fs.getFileStatus(path);
     
        if (pathFile.isDir()) {
            // input is directory. list all contained files
            List<FileInputSplit> splits = new ArrayList<FileInputSplit>();
            final FileStatus[] dir = fs.listStatus(path);
            for (int i = 0; i < dir.length; i++) {
                final BlockLocation[] blocks = fs.getFileBlockLocations(dir[i], 0, dir[i].getLen());
                splits.add(new FileInputSplit(i, dir[i].getPath(), 0, dir[i].getLen(), blocks[0].getHosts()));
            }
            return (FileInputSplit[]) splits.toArray(new FileInputSplit[splits.size()]);
        } else { 
            // analogous for one file
        }
    }

* * * * *

**A Data Generating Input Format**

You can easily create an input format that runs a data generator rather
than reading the data with the help of the
*[eu.stratosphere.pact.common.io.GenericInputFormat](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/GenericInputFormat.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/io/GenericInputFormat.java")*.
It uses a generic input split type that holds no information except its
partition number. The example below shows a minimal example:

    public class GeneratorFormat extends GenericInputFormat
    {
        private Random random;
        private int numRecordsRemaining;
     
        private final PactInteger theInteger = new PactInteger();
        private final PactString theInteger = new PactString();
     
        @Override
        public void configure(Configuration parameters) {
            super.configure(parameters);
            this.numRecordsRemaining = parameters.getInteger("generator.numRecords", 100000000);
        }
     
        @Override
        public void open(GenericInputSplit split) throws IOException {
            this.random = new Random(split.getSplitNumber());
        }
     
        @Override
        public boolean reachedEnd() throws IOException {
            return this.numRecordsRemaining > 0;
        }
     
        @Override
        public boolean nextRecord(PactRecord record) throws IOException {
            this.theInteger.setValue(this.random.nextInt());
            generateRandomString(this.theString, this.random);
     
            record.setField(0, theInteger);
            record.setField(1, theString);
            this.numRecordsRemaining--;
            return true;
        }
     
        private static void generateRandomString(PactString target, Random rnd) {
            ...
        }
    }

Note that the above code example works with mutable objects and reuses
object instances across function calls. In general, user code should
always try to do this, as reference tracing and object garbage
collection can become a serious computational bottleneck. See [Advanced
Pact
Programming](advancedpactprogramming.html "advancedpactprogramming")
