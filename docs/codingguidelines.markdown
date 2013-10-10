---
layout: documentation
---
Coding Guidelines
=================

The guidelines cover code conventions, documentation requirements, and
utilities for easing the development of Stratosphere in Java.

We strongly recommend using
[Eclipse](http://www.eclipse.org "http://www.eclipse.org") and primarily
specify the guidelines through eclipse specific configurations and
settings. If Eclipse is not the IDE of your choice, you might have to
infer the coding guidelines by yourself. Please share the inferred
configuration of your IDE with us.

Code Conventions
----------------

We fully use the [standard code
conventions](http://java.sun.com/docs/codeconv/CodeConventions.pdf "http://java.sun.com/docs/codeconv/CodeConventions.pdf").
Following these conventions is straight-forward with Eclipse and our
predefined settings for formatter, warnings, and templates.

Be aware of the name conventions:

    class ClassNameInCamelCase implements InterfaceAlsoInCamelCase {
        private final static int CONSTANT_IN_ANSI_STYLE = 1;
     
        private int fieldStartsWithLowerCase;
     
        public void methodStartsWithLowerCase(Object parametersAsWell) {
            int soDoLocalVariables;
        }
    }

Package Conventions
-------------------

All package names of the stratosphere project start with
`eu.stratosphere.` and follow the [java naming
convention](http://www.oracle.com/technetwork/java/codeconventions-135099.html#367 "http://www.oracle.com/technetwork/java/codeconventions-135099.html#367")
for packages.

Logging Conventions
-------------------

The following section defines when which log level should be used.

-   **Debug:** This is the most verbose logging level (maximum volume
    setting). Here everything may be logged, but note that you may
    respect privacy issues. Don't log sensitive information (this of
    course applies to all log levels). This log level is not intended to
    be switched on in a productive system.

-   **Trace:** This is a special debug level. With this level turned on
    a programmer may trace a specific error in a test or productive
    system. This level is not as verbose as debug, but gives enough
    information for a programmer, to identify the source of a problem.
    For example entering/exiting log messages for methods can be logged
    here.

-   **Info:** The information level is typically used to output
    information that is useful to the running and management of your
    system. This level should be the normal log level of a productive
    system. Info messages should be 1-time or very occasional messages.
    For example the start and stop of a specific component can be logged
    here (e.g. Jobmanager, Taskmanager etc.)

-   **Warn:** Warning is often used for handled 'exceptions' or other
    important log events. Warnings are exceptions from which the system
    can recover and where the part of the system, which produced the
    exception, can nevertheless give a reasonable result.
-   **Example:** Suppose you are iterating over a list of integer
    objects, which a functions wants to sum up. One entry in this list
    is a null pointer. While summing up, the function sooner or later
    reaches the null pointer and produces a null pointer exception. This
    could be logged as a warning and “interpreted” as summing up a 0.

-   **Error:** Errors are exceptions, from which the system can recover,
    but which prevents the system from fulfilling a service/task.

-   **Fatal:** Fatal is reserved for special exceptions/conditions where
    it is imperative that you can quickly pick out these events.
    Exceptions are fatal, if the system can not recover fully from this
    exception (e.g. a component is down because of this exception). This
    level should be used rarely.

Code Style Conventions
----------------------

We have defined a code style for the Stratosphere project to which all
the source code files should comply to.   
 See the [Eclipse
Import](eclipseimport.html "eclipseimport")
page for style configuration files and Eclipse configuration
instructions.
