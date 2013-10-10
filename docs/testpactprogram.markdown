---
layout: documentation
---
Testing PACTs
=============

Stratosphere incorporates a test harness for unit tests of PACT stub
implementations and PACT programs. Similar to JUnit test cases, one or a
pipeline of stub implementations can be tested individually to guarantee
certain behavior for some use and edge cases. The tests do not require
any specific installation and do not perform any network communication.
Thus, a test usually completes within seconds.

The test harness is located in the `pact-client` Maven module in the
package
[eu.stratosphere.pact.testing](https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-clients/src/main/java/eu/stratosphere/pact/testing "https://github.com/stratosphere-eu/stratosphere/tree/master/pact/pact-clients/src/main/java/eu/stratosphere/pact/testing").

### Simple Case

Consider the following minimalistic map that simply echos all input
values.

    @SameKey
    public class IdentityMap extends MapStub {
        @Override
        public void map(final PactRecord record, final Collector<PactRecord> out) {
            out.collect(record);
        }
    }

In order to test that the map performs its expected behavior (i.e.
forwarding all input records), the following test case verifies the
correct behavior for one input/output combination.

    public class IdentityMapTest {
     
        @Test
        public void shouldEchoOutput() {
            final MapContract map = MapContract.builder(IdentityMap.class).name("Map").build();
            TestPlan testPlan = new TestPlan(map);
            testPlan.getInput().
                add(new PactInteger(1), new PactString("test1")).
                add(new PactInteger(2), new PactString("test2"));
            testPlan.getExpectedOutput(PactInteger.class, PactString.class).
                add(new PactInteger(1), new PactString("test1")).
                add(new PactInteger(2), new PactString("test2"));
            testPlan.run();
        }
    }

The test initializes a contract with the identity map stub and creates a
TestPlan with it. The following lines initialize the input tuples of the
test and the expected output tuples. Please note, that you have to
specify the expected types.

Finally, the plan is executed and the result is compared with the
expected output. In case of unequal output, the JUnit test will fail due
to an AssertionError. Additionally, if the PACT had an runtime error,
the test would fail with an AssertionError, too.

### Multiple PACTs

Testing a single PACT stub implementation is not sufficient in many
cases. If the collective behavior of a whole PACT pipeline needs to be
tested, the PACTs need to be chained similarly to a normal PACT program.

    final MapContract map1 = MapContract.builder(IdentityMap.class).name("Map").build();
    final MapContract map2 = MapContract.builder(IdentityMap.class).name("Map").input(map1).build();
    TestPlan testPlan = new TestPlan(map2);
    testPlan.getInput().
        add(new PactInteger(1), new PactString("test1")).
        add(new PactInteger(2), new PactString("test2"));
    testPlan.getExpectedOutput(PactInteger.class, PactString.class).
        add(new PactInteger(1), new PactString("test1")).
        add(new PactInteger(2), new PactString("test2"));
    testPlan.run();

In this simple example, the concatenated identity maps should still echo
the input.

### Explicit Data Sources/Sinks

The framework automatically adds data sources and data sinks for each
unconnected input and output respectively. However, there are many cases
where the user needs to exert more control. For example, if he
specifically wants to test a new file format.

    final FileDataSource source = new FileDataSource(IntegerInFormat.class, "TestPlan/test.txt");

    final MapContract map = MapContract.builder(IdentityMap.class).name("Map").input(read).build();

    FileDataSink output = new FileDataSink(IntegerOutFormat.class, "output");
    output.setInput(map);

    TestPlan testPlan = new TestPlan(output);
    testPlan.run();

### Multiple Inputs

So far, we have only seen programs with one implicit data source and
sink. If multiple data sources are involved, there are two ways to
specify the input.

    CrossContract crossContract = CrossContract.builder(CartesianProduct.class).build();
     
    TestPlan testPlan = new TestPlan(crossContract);
    testPlan.getInput(0).
        add(new PactInteger(1), new PactString("test1")).
        add(new PactInteger(2), new PactString("test2"));
    testPlan.getInput(1).
        add(new PactInteger(3), new PactString("test3")).
        add(new PactInteger(4), new PactString("test4"));
     
    testPlan.getExpectedOutput(PactInteger.class, PactInteger.class, PactString.class, PactString.class).
        add(new PactInteger(1), new PactInteger(3), new PactString("test1"), new PactString("test3")).
        add(new PactInteger(1), new PactInteger(4), new PactString("test1"), new PactString("test4")).
        add(new PactInteger(2), new PactInteger(3), new PactString("test2"), new PactString("test3")).
        add(new PactInteger(2), new PactInteger(4), new PactString("test2"), new PactString("test4"));
    testPlan.run();

Additionally, when the sources have been explicitly defined, it is
possible to identify the input with the source.

    CrossContract crossContract = CrossContract.builder(CartesianProduct.class).build();
     
    TestPlan testPlan = new TestPlan(crossContract);
    // first and second input are added in TestPlan
    testPlan.getInput((GenericDataSource<?>) crossContract.getFirstInputs().get(0)).
        add(new PactInteger(1), new PactString("test1")).
        add(new PactInteger(2), new PactString("test2"));
    testPlan.getInput((GenericDataSource<?>) crossContract.getSecondInputs().get(0)).
        add(new PactInteger(3), new PactString("test3")).
        add(new PactInteger(4), new PactString("test4"));
     
    testPlan.getExpectedOutput(PactInteger.class, PactInteger.class, PactString.class, PactString.class).
        add(new PactInteger(1), new PactInteger(3), new PactString("test1"), new PactString("test3")).
        add(new PactInteger(1), new PactInteger(4), new PactString("test1"), new PactString("test4")).
        add(new PactInteger(2), new PactInteger(3), new PactString("test2"), new PactString("test3")).
        add(new PactInteger(2), new PactInteger(4), new PactString("test2"), new PactString("test4"));
    testPlan.run();

Use Test Data from Files
------------------------

It is also possible to either specify data sources/sinks explicitly or
load the input and/or output tuples from a file.

    final MapContract map = MapContract.builder(IdentityMap.class).name("Map").build();
    TestPlan testPlan = new TestPlan(map);
    testPlan.getInput().fromFile(IntegerInFormat.class, "test.txt");
    testPlan.getExpectedOutput(output, PactInteger.class, PactString.class).fromFile(IntegerInFormat.class, "test.txt");
    testPlan.run();

How it works
------------

The TestPlan transparently adds data sources and sinks to the PACT
program if they are not specified. Expected and actual output tuples are
sorted before comparison since the actual order is usually not of
interest.

All network communications are mocked and the test cases are executed
with low memory settings for the MemoryManager. All channels are
replaced with InMemoryChannels to enable fast execution.

However, all PACTs are still executed in their own threads to maintain
the consumer-producer notions. It is also possible to set the degree of
parallelism for all PACTs or the complete test.

Since only little code of the PACT execution is mocked, passed PACT
tests are likely to work in the same way on a complete cluster. However,
the partitioning of the data is not emulated. Therefore, the PACT tests
are not a replacement for integration tests on a real cluster.
