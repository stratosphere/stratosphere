---
layout: documentation
---
Unit Tests with Mockito and Powermock
-------------------------------------

Readings:

-   [http://mockito.org](http://mockito.org "http://mockito.org")
-   [http://code.google.com/p/powermock/](http://code.google.com/p/powermock/ "http://code.google.com/p/powermock/")
-   [http://code.google.com/p/hamcrest/](http://code.google.com/p/hamcrest/ "http://code.google.com/p/hamcrest/")

Mockito is a Test Framework extending JUnit. It provides easy mechanism
to mock objects in a unit test. Powermock extends Mockito with some
features to test “untestable” code (e.g. static methods, local variables
…).

Example: test the PactCompiler.compile(.) method
------------------------------------------------

signature of the method:

    public OptimizedPlan compile(Plan pactPlan) throws CompilerException

first we have to mock the Plan object pactPlan:

    @Mock Plan myTestPlan;

To give the mocked object a desired behaviour we use the API of Mockito.
E.g. we want, that the call to Plan.getMaxNumberMachines() returns 1:

    //return a valid maximum number of machines
    when(myTestPlan.getMaxNumberMachines()).thenReturn(1);

Finally, if all dependent objects are mocked, we want to test the
method:

    PactCompiler toTest = new PactCompiler();
     
    // now the compile schould succeed
    toTest.compile(myTestPlan);
    verify(myTestPlan, times(1)).getMaxNumberMachines();

the verify-call checks, whether the method getMaxNumberMachines() was
called exactly once. The times(1) is called a VerificationMode in
Mockito. There are more VerificationModes available. See the Mockito
Documentation for more modes and details.

Note: To run the Testcase you should use the
@RunWith(MockitoJUnitRunner.class) Annotation for the test class.

local variables in PactCompiler.compile(.)
------------------------------------------

A closer look to the compile()-method shows some “untestable” code.
There are local variables instantiated, which can not be mocked with
Mockito, since the instantiation is done in the method. To mock this
local variables, we use Powermock:

first we have to use the PowermockRunner as Testrunner:

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(PactCompiler.class)
    public class TestPactCompiler {

The @PrepareForTest annotation tells Powermock, that the class to test
(PactCompiler) has some local variables that we want to mock (same
applies to static methods).

Inside the test method we tell Powermock, that the instantiation of a
class of type OptimizedPlan shall return a mocked object:

    whenNew(OptimizedPlan.class).withArguments(Matchers.any(Collection.class), Matchers.any(Collection.class),
                    Matchers.any(Collection.class), Matchers.any(String.class)).thenReturn(
                    this.mockedOP);

The code means, that a call to the constructor of OptimizedPlan with
argument types (Collection,Collection,Collection,String) with any
arguments (Matchers.any(.)) shall return our mock object mockedOP. For a
more comprehensive view on the test class, see the attached file.

declarative Matchers with Hamcrest
----------------------------------

Since JUnit 4.x declarative matchers may be used to define assertions on
the outcome of tests. The core matchers of hamcrest are already part of
the JUnit library and can be extended by using the hamcrest lib. The
advantage of those matchers are the assertions being readable in a more
intuitive format and the ability to implement own matchers. The
following assertion almost reads like an english sentence (and thus
reduces the time to understand/the need to document the test):

*assertThat(matchCount, is(lessThan(2)));*
