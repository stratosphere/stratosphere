---
layout: documentation
---
Best Practices of Pact Programming
==================================

This page provides a brief overview of insights and strategies that we
learned during programming with PACTs to achieve better performance.

Consider the Number of Stub Calls
---------------------------------

-   You should consider that your stub methods usually are called
    extremely often. A *map(…)* function is called once per processed
    record.
-   As a (very) general guideline, implement your methods with the least
    possible overhead you can think of. Move any invariant code out of
    the function. The *open(Configuration)* and *close()* methods are
    places for initialization and tear-down logic.

Object Instantiations and Modifications
---------------------------------------

-   Avoid instantiation of objects as much as possible. Recall that many
    method calls happen in the course of a program. Even though the
    object instantiation itself is pretty cheap in Java, it will
    eventually cause many short-lived objects to be created. The tracing
    of a plethora of dead objects as well as the eventual reclamation of
    their memory space by the garbage collector has a cost that is often
    underestimated. Careless object instantiation is a major cause for
    bad performance.
-   Reuse object instances. Once an data type object and a record are
    emitted to the Collector, their instances are safe to be reused,
    because the data will have been serialized at this point. The
    following code illustrates that at the example of the line
    tokenizing mapper in the WordCount example. The map function reuses
    all object instances. The PactString containing the next token is
    reused after it has been emitted to the Collector.

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

Implement your own efficient Data Types
---------------------------------------

-   Use Java's primitive types (int, double, boolean, char, …) as much
    as possible when implementing your own data types and try to avoid
    using objects.
-   The provided data types are fine for rapid prototyping, but you
    should avoid composing them to more complex data types and rather
    use primitives, if you aim for performance.
-   The previous statement holds especially for the provided collection
    types, such as *PactList* and *PactMap*.
-   Pay attention to efficient serialization and deserialization
    methods. Serialization and deserialization occur very often,
    whenever network is involved, data is sorted or hashed, or
    checkpoints are created.
-   If your data type acts as a key, consider also implementing the
    [NormalizableKey](https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/NormalizableKey.java "https://github.com/stratosphere/stratosphere/blob/master/pact/pact-common/src/main/java/eu/stratosphere/pact/common/type/NormalizableKey.java")
    interface. That allows the system to operate more efficiently during
    sorting.

Implement State-Free Stub Methods
---------------------------------

-   Stub methods are executed in parallel. On the same physical node (or
    Virtual Machine), all stubs run in the same JVM in concurrent
    threads.
-   Ideally, stub methods should be implemented state-free, following
    the principle of functional programming. All invocations of the
    method stub should be completely independent from one another and no
    shared variables are used. If state needs to be held, use instance
    variables. The same single stub object is used throughout the life
    of a single parallel thread, so the state within a thread can be
    maintained through instance variables.
-   Avoid class (*static*) variables. Their scope is within the JVM of
    the physical instance (or VM). Even when specifying a default
    intra-node parallelism of one, more parallel instances of your stub
    may be scheduled to the same JVM, because TaskManagers on large
    machines may be assigned multiple smaller virtual instances. Even if
    the statically referenced objects are thread-safe, the performed
    synchronization may cause significant overhead through lock
    congestion.
-   Using *static* objects, can cause unpredictable behavior, if the
    statically referenced objects (or any methods of those objects) are
    not thread-safe. In the worst case, that can lead to exceptions and
    cause the job to fail repeatedly.
-   Using state-free or thread-safe processing objects (such as
    Formatter, Parser, and so on) in your stub methods is fine. These
    objects should be created during instantiation (within the
    constructor) or using *configure()* method of the stub.

Use User Code Annotations and Compiler Hints
--------------------------------------------

-   *User Code Annotations* give the optimizer information about the
    behavior of your stub methods. The optimizer can frequently exploit
    that information to generate execution plans that avoid expensive
    operations such as data shipping and sorting. Be sure to attach only
    valid annotations to your function. An incorrect annotation that the
    function violates may cause incorrect results. Refer to the [list of
    annotations](pactpm#user_code_annotations "pactpm")
    for an overview of available annotations, and to the [programming
    tutorial](writepactprogram.html "writepactprogram")
    to learn how to attach them. Our
    [examples](pactexamples.html "pactexamples")
    also show how to correctly use them. We are currenlty prototyping
    static code analysis methods to infer those annotations
    automatically.
-   Give the optimizer as much information as possible.
    Average-records-per-stub-call, average-record-width,
    key-cardinality, and values-per-key are important numbers for the
    optimizer that help generating better estimates consequently saving
    costs. See the [Compiler
    Hints](writepactprogram#compiler_hints "writepactprogram")
    section for the supported compiler hints and how to set them.

