---
layout: documentation
---
Pact Record
===========

The
[PactRecord](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/PactRecord.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/PactRecord.java")
is the basic data type that flows in the PACT data flow programs. It
replaces the Key/Value pair in MapReduce and is more generic and
flexible.

The Pact Record can be thought of as a schema free tuple that may
contain arbitrary fields. The fields may be any data type that
implements the
[Value](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/Value.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/Value.java")
interface, which describes how the type is serialized and deserialized.

The interpretation of a field is up to the user code. Whenever a field
in the record is accessed, the user code must specify the position of
the field to access and the data type as which to read it. A simple
example of a filtering map that accesses a single integer field is given
below:

    @Override
    public void map(PactRecord record, Collector<PactRecord> out) throws Exception {
     
        PactInteger theInt = record.getField(1, PactInteger.class);
        if (theInt.getValue() > RANKFILTER) {
            out.collect(record);
        }
    }

Lazy DeSerialization
--------------------

When the record is read from a stream, all fields are typically in a
binary representation. Fields are lazily deserialized when accessed and
then cached. Any modifications to the record are also cached and lazily
serialized when needed (for example for network communication). It may
happen that the record never serializes data, for example when it is
piped to a successive task which filters it.

In the above example of the filtering map, the functions parameter
record is re-emitted. Because the record is unmodified, it can use its
original binary signature and will not invoke any data-types
serialization code. This is rather efficient. For records where only a
subset of the fields are changed, the unchanged ones are kept in their
original binary representation, avoiding serialization and
deserialization effort.

Mutable Objects
---------------

**Warning**: It is important to treat PactRecords as mutable objects.
They were specifically designed for reusing objects and perform quite a
bit of caching and lazy work to increase performance. That makes them
rather heavy-weight objects which put a lot of pressure on the garbage
collector, if they are not reused.

PactRecords reuse internal data structures for performance reasons. You
should refrain from creating instances in the user function. If the
record is unchanged (or only modified at some positions) it is best to
emit the instance that was supplied in the function call, as in the
above example of the filtering map function. If you emit records whose
contents is very much unrelated to the received records, create a single
instance outside the function and reuse that one. It is save to reuse
any PactRecord object once it has been handed to the Collector. The code
below (from the WordCount example) illustrates that:

    public static class TokenizeLine extends MapStub
    {
        // initialize reusable mutable objects
        private final PactRecord outputRecord = new PactRecord();
        private final PactString word = new PactString();
        private final PactInteger one = new PactInteger(1);
     
        private final AsciiUtils.WhitespaceTokenizer tokenizer = new AsciiUtils.WhitespaceTokenizer();
     
        @Override
        public void map(PactRecord record, Collector<PactRecord> collector){
            // get the first field (as type PactString) from the record
            PactString line = record.getField(0, PactString.class);
     
            // normalize the line
            AsciiUtils.replaceNonWordChars(line, ' ');
            AsciiUtils.toLowerCase(line);
     
            // tokenize the line
            this.tokenizer.setStringToTokenize(line);
            while (tokenizer.next(this.word)) {
                // we emit a (word, 1) pair 
                this.outputRecord.setField(0, this.word);
                this.outputRecord.setField(1, this.one);
                collector.collect(this.outputRecord);
            }
        }
    }

Pact Records are also reused by the runtime. Whenever a function is
invoked multiple times, it is typically supplied with the same
PactRecord Object, with different contents each time. This means that
you cannot keep references to the object across function invocations,
because the object's contents will be changed. If you need to keep a
copy of the entire record (which is typically an indication for bad
code), create a copy of the record via *PactRecord\#createCopy()*. In
general, it is better to keep a copy of some individual fields,
preferably as primitive types.

The following code will cause the list to contain multiple times the
same reference:

    public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) {
        List<PactRecord> list = new ArrayList<PactRecord>();
        while (records.hasNext()) {
            list.add(records.next());
        }
    }

A correct and also more efficient version would be the version below.
The code saves any unnecessary copying and, depending on a clever
implementation of the *IntList*, code might run without ever
instantiating an object inside the reduce function.

    private final IntList list = new IntList();
     
    @Override
    public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) {
        list.clear();
        while (records.hasNext()) {
            PactInteger i = records.next().getField(1, PactInteger.class);
            list.add(i.getValue);
        }
     
        // do something with the list
    }

Cached Fields
-------------

To save de-serialization effort, the Pact Record caches deserialized
fields. That means a successive access to the fields (possibly in a
succeeding function) may be a lot cheaper. The implication of that
caching mechanism is that you must be careful modifying instances that
you obtained from the record. In any case, any object may be reused
across different function calls or records obtained from an iterator.
The following example shows a possible error source:

    public void map(PactRecord record, Collector<PactRecord> out) {
        PactInteger i = record.getField(0, PactInteger.class); // deserialized the value and cached the object instance
        System.out.println(i); // prints the original value
        i.setValue(42); // sets the value to the object that is also cached

        PactInteger k = record.getField(1, PactInteger.class); // returns the cached object
        System.out.println(k); // prints the 42
    }

Sparsity
--------

The record may have null fields between fields that are actually set. If
a certain field is set, all yet unset fields before that field are
considered *null*. IN the following example, fields 2 and 5 are set,
while fields 1, 3, 4 are *null*.

    PactRecord rec = new PactRecord();
    rec.setField(2, new PactInteger(43290));
    rec.setField(5, new PactString("some text...");

Null fields are sparsely encoded with bit masks and occupy rather little
space. They allow to write code that moves little data between the
fields.
