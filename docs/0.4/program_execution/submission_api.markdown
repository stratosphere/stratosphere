---
layout: inner_docs_v04
title:  "Submission API"
---

## Submission API

The PACT Client API (Embedded Client)
=====================================

For executing Pact programs from Java source code, a simplified Client
API is provided. The API makes it possible to submit PACT programs from
within any Java application. Both the command line client and the web
client internally use that API. It consists of the following two
classes:

**eu.stratosphere.pact.client.nephele.api.PactProgram**: This class
wraps all PACT program related code. The class has to be instantiated
with the path to the jar file and the arguments for the program.

**eu.stratosphere.pact.client.nephele.api.Client**: The client class
wraps all code related to sending a program to the Nephele job manager.
The relevant method is *Client\#run(PactProgram)* method which accepts a
pact program as argument and sends that to the job manager. More methods
are provided, which allow for example only compilation without
submission, or submission of a pre-compiled plan.

A minimal example for sending a readily packaged pact program to the job
manager is:

    File jar = new File("./path/to/jar");
    // load configuration
    GlobalConfiguration.loadConfiguration(location);
    Configuration config = GlobalConfiguration.getConfiguration();

    try {
      PactProgram prog = new PactProgram(jar);
      Client client = new Client(configuration);
      client.run(prog);
    } catch (ProgramInvocationException e) {
      handleError(e);
    } catch (ErrorInPlanAssemblerException e) {
      handleError(e);
    }

As mentioned before, the embedded client requires a configuration to be
passed, describing how to reach the JobManager. As a minimum, the values
*jobmanager.rpc.address* and *jobmanager.rpc.port* must be set. The
parameters that define the behavior of the PactCompiler (such as the
default parallelism, intra-node parallelism, and the limit to the number
of instances to use) are also drawn from this configuration. Please
refer to the [Configuration
Reference](configreference.html "configreference")
for details.

As an alternative, the client can be started with a Java
[InetSocketAddress](http://download.oracle.com/javase/6/docs/api/java/net/InetSocketAddress.html "http://download.oracle.com/javase/6/docs/api/java/net/InetSocketAddress.html"),
that describes the connection to the JobManager. The configuration for
the compiler is assumed empty in that case, causing the default
parameters to be used for compilation.