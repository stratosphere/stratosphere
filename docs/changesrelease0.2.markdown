---
layout: documentation
---
Nephele
-------

-   Major performance optimizations to runtime engine:
    -   Unification of channel model
    -   Support for multi-hop data routing
    -   Dynamic redistribution of memory resources
    -   Lazy task deployment to avoid allocation of resources by waiting
        tasks
    -   Several scalability improvements

-   Fault tolerance:
    -   Support for local logs for intermediate results
    -   Replay from completed/incomplete logs to recover from task
        failures
    -   Logs can be configured on a task level

-   Support for plugins:
    -   External libraries can now hook into the schedule to
        monitor/influence the job execution
    -   Custom wrappers for user code to collect statistical information
        on the task execution

-   Support for application-layer multicast network transmissions:
    -   Identical data fragements (for example in case of a broadcast
        transmission) are now distributed through a topology-aware
        application-layer multicast tree

Pact
----

-   Data-Model changed to PactRecord, ageneric tuple model (see
    [http://www.stratosphere.eu/wiki/doku.php/wiki:PactRecord](http://www.stratosphere.eu/wiki/doku.php/wiki:PactRecord "http://www.stratosphere.eu/wiki/doku.php/wiki:PactRecord"))
    -   Schema free tuples
    -   Lazy serialization / deserialization of fields
    -   Sparse representation of null fields

-   Support for composite keys for Reduce / Match / CoGroup
-   Efficient Union of data streams
-   Generic User-Code annotations replace Output Contracts
    -   Constant fields and cardinality bounds

-   Added range partitioning and global sorting for results
-   Added support for sorted input groups for Reduce and CoGroup (a.k.a.
    Secondary Sort)
-   Removed limitation in record length for sort/hash strategies
-   Various runtime performance improvements
    -   Runtime object model changed to Mutable Objects
    -   Memory management changed to memory page pools
    -   Unified handling of data in serialized form via memory views
    -   Reworked all basic runtime strategies (sorting / hashing /
        damming)
    -   Some performance improvements for parsers / serializers

-   Extended library of input/output formats
    -   Generic parameterizable tuple parsers / serializers
    -   InputFormat that start and read from external processes

-   Introduction of NormalizedKeys for efficient sorting of custom data
    types
-   Support for multiple disks/directories for parallel I/O operations
    on temporary files (temp storage, sort / hash overflows)
-   Chaining support for Mappers/Combiners (elimination of intermediate
    serializing)
-   Changed Runtime to work on generic data types (currently only with
    manual plan composition).

Sopremo
-------

-   Initial Version of Sopremo added: Extensible operator model
    organized in packages
-   Data model based on Json
-   Includes base package of operators for structured and
    semi-structured data
-   Server/Client interface to remotely execute plans on a cluster
    -   Automatically ships Sopremo packages when package on server is
        outdated

Meteor
------

-   Initial Version of Meteor added: Extensible query language to create
    Sopremo plans
-   Command line client, to execute scripte remotely

