---
layout: documentation
---
Unit Tests
----------

Unit tests have to meet the following criteria:

-   **high code coverage:** tests should execute different scenarios for
    the class under test; especially loops and branches in the code
    under test should be tested exhaustivly
-   **exception cases:** tests should also simulate faulty behaviour
    (e.g. wrong or missing parameters, exceptions thrown during
    execution …)
-   **one test per class:** normally there should exist one test class
    for one production class; the test class has several test cases
-   **Mocking:** dependencies to other classes or components must be
    mocked (see PowerMock/Mockito), with these mocks the tester can
    drive the test case
-   **test of functionality:** the test cases should be created on a
    functional perspective (expected behaviour of a method);
    getter-/setter-methods have not to be tested

Unit tests should be executed continuously during the coding, e.g.
before a commit. All unit tests should be run. Therefore it is necessary
to develop unit tests as limited as possible (concerning the code under
test) to ensure, that the unit test phase can be executed fast
(milliseconds!).

Bug Fixing
----------

Each reported (and accepted) bug has to be transformed into a unit test,
that tests the faulty behaviour. In the comment of the test class should
be a reference, to which bug this test is related to.
