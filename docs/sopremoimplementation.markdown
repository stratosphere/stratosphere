---
layout: documentation
---
Implementation Details
======================

In the following, we describe the implementation details of Sopremo
operators and the underlying support system. The information should
provide a solid base to implement own Sopremo operators.

First, we examine the implementation of the *Selection* operator,
explain how the data flows in a query with the embedded operator, and
transfer the strategies to more complex examples.

Implementation of Selection
---------------------------

Each *Operator* must provide a definition of *asPactModule* that
converts the operator with all its parameter to a *PactModule*. The
following code snippets shows the implementation for the *Selection*.

    #!java
    @Override
    public PactModule asPactModule(EvaluationContext context) {
        PactModule module = new PactModule(1, 1);
        MapContract<PactNull, PactJsonObject, Key, PactJsonObject> selectionMap = new MapContract<PactNull, PactJsonObject, Key, PactJsonObject>(
            SelectionStub.class);
        module.getOutput(0).setInput(selectionMap);
        selectionMap.setInput(module.getInput(0));
        SopremoUtil.serialize(selectionMap.getStubParameters(), "condition", this.getCondition());
        SopremoUtil.setTransformationAndContext(selectionMap.getStubParameters(), null, context);
        return module;
    }

The *Selection* operator may only process one input stream and produces
exactly one output stream. Hence, the *PactModule* is created with 1 as
the number of inputs and outputs. The next lines show how the only
implementation contract is connected to the input and the output of the
module. Finally, the parameters are serialized with the *SopremoUtil*
and added to the stub parameters. Note that the second function is a
convenience function that sets the transformation (here null) and
context in one call.

The next code snippet illustrates the implementation of the
*SelectionStub*. Please note that it extends the *SopremoMap* that
already provides some convenience functions, such as deserialization of
the transformation and context.

    #!java

    public static class SelectionStub extends SopremoMap<PactNull, PactJsonObject, Key, PactJsonObject> {
            private Condition condition;

            @Override
            public void configure(Configuration parameters) {
                    super.configure(parameters);
                    this.condition = SopremoUtil.deserialize(parameters, "condition", Condition.class);
            }

            @Override
            public void map(PactNull key, PactJsonObject value, Collector<Key, PactJsonObject> out) {
                    if (this.condition.evaluate(value.getValue(), this.getContext()) == BooleanNode.TRUE)
                            out.collect(key, value);
            }
    }

As seen in the snipper, the *configure* method is used to deserialize
the condition. Please note the call to the implementation of the base
class that is responsible for proper deserialization of the
transformation and *EvaluationContext*.

Finally, the actual implementation of the stub is quite short as
displayed in the *map* method. The condition is *evaluate*d using the
raw Json value of the *PactJsonObject* and the transparently
deserialized context. If the result of the condition is true, the key
value pair is emitted.

Using the Selection
-------------------

To use the *Selection*, we examine one of the test cases that covers the
selection. The test selections all employees that are either manager
(mgr flag is set) or have an income above 30000.

    #!java
    SopremoTestPlan sopremoPlan = new SopremoTestPlan(1, 1);

    ComparativeExpression incomeComparison = new ComparativeExpression(new FieldAccess("income"), BinaryOperator.GREATER, new ConstantExpression(30000));
    UnaryExpression mgrFlag = new UnaryExpression(new FieldAccess("mgr"));
    ConditionalExpression condition = new ConditionalExpression(Combination.OR, mgrFlag, incomeComparison);
    sopremoPlan.getOutputOperator(0).setInputs(new Selection(condition, sopremoPlan.getInputOperator(0)));

    sopremoPlan.getInput(0).
        add(createPactJsonObject("name", "Jon Doe", "income", 20000, "mgr", false)).
        add(createPactJsonObject("name", "Vince Wayne", "income", 32500, "mgr", false)).
        add(createPactJsonObject("name", "Jane Dean", "income", 72000, "mgr", true)).
        add(createPactJsonObject("name", "Alex Smith", "income", 25000, "mgr", false));
    sopremoPlan.getExpectedOutput(0).
        add(createPactJsonObject("name", "Vince Wayne", "income", 32500, "mgr", false)).
        add(createPactJsonObject("name", "Jane Dean", "income", 72000, "mgr", true));

    sopremoPlan.run();

In the next section, the test case is used to explain the data flow in
this operator.

Data Flow
---------

All contracts in Sopremo have a *PactJsonObject* as their value types.
Instances of this type wrap a Json node that is either a primitive Json
value, a Json array, or a Json object.

During the creation of the unit test, the condition and the Json objects
are statically initialized. The input Json objects are written in a
temporary file and the location is bound to a *DataSourceContract*.
Similarly, the expected output Json objects are also bound to a
*DataSourceContract*. Additionally, the condition and the
*EvaluationContext* of the *SopremoPlan* are serialized in the
parameters of the *SelectionStub*

The *run* method invokes the Stratopshere stack and the Sopremo plan is
executed. During the execution, the input *DataSourceContract* is
initialized and produces the four Json objects wrapped in
*PactJsonObject*s. The *SelectionStub* deserializes the condition and
the context in the *configure* method. In the end, the
*SelectionStub\#map* is subsequently invoked with the four Json objects.

In the *map* the condition is evaluated with the value. As the condition
consists of two parts connected with an *OR*, the parts are evaluated
subsequently to extract the field values *mgr* and *income* and
calculate the boolean value. For the respective matching Json objects,
the object is emitted.

Finally, the expected values are extracted from the temporary file and
compared to the actual values.

Operators with more inputs and keys
-----------------------------------

Operators with more than two inputs are subsequently mapped to Pacts
with two inputs. The *SetOperator* illustrates how a n-way set operation
can be broken down to (n-1) operators.

    #!java
    int numInputs = this.getInputOperators().size();
    PactModule module = new PactModule(numInputs, 1);

    if (numInputs == 1) {
        module.getOutput(0).setInput(module.getInput(0));
        return module;
    }

    Contract leftInput = SopremoUtil.addKeyExtraction(module, this.getSetKeyExtractor(0), context);
    for (int index = 1; index < numInputs; index++) {

        Contract rightInput = SopremoUtil.addKeyExtraction(module, this.getSetKeyExtractor(index), context);
        Contract union = this.createSetContractForInputs(leftInput, rightInput);

        SopremoUtil.setTransformationAndContext(union.getStubParameters(), null, context);
        leftInput = union;
    }

    module.getOutput(0).setInput(SopremoUtil.addKeyRemover(leftInput));

    return module;

First, a pact module with the respective number of inputs is created.
Then the trivial case of one input is treated, by shortcutting the input
and the output. The shortcut effectively removes the operator from the
Pact conversion.

Next, the first input is wrapped with a key extractor. The key extractor
is a Map that applies an *EvaluableExpression* to the input tuples to
generate a key/value pair.

Then, for all remaining inputs, the next input is wrapped as well and
fed as the right input to a Pact contract created by
*createSetContractForInputs*. The result is repeatedly added as the left
input to a new Pact contract until no inputs are left.

Finally, the key has to be removed to conform with the general Pact data
types in Sopremo (no key between operators).
