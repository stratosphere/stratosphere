---
layout: documentation
---
PACT Architecture
=================

PACT is a parallel [programming
model](pactpm.html "pactpm")
and extends MapReduce. PACT provides a user API to write parallel data
processing tasks. Programs written in the PACT programming model are
translated by the [PACT
Compiler](pactcompiler.html "pactcompiler")
into a Nephele job and and executed on
[Nephele](nephele.html "nephele").
In fact, Nephele considers PACT programs as regular Nephele jobs; to
Nephele, PACT programs are arbitrary user programs.

* * * * *

For details on the architectural components, refer to the following
sections:

-   [PACT Programming
    Model](pactpm.html "pactpm")
-   [PACT
    Compiler](pactcompiler.html "pactcompiler")
-   [Internal PACT
    Strategies](pactstrategies.html "pactstrategies")
-   [PACT
    Clients](executepactprogram.html "executepactprogram")

