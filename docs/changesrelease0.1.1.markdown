---
layout: documentation
---
Changelog and Updates of Release 0.1.1
======================================

This page shows the important changes of the Stratosphere system
introduced by release 0.1.1. It also logs all important updates and bug
fixes made to release 0.1.1.   

Changelog Release 0.1.1
-----------------------

### New Features

-   Introduced hash-based local strategy for Match input contracts. Now
    the optimizer can choose between a hash- and a sort-based local
    strategy.
-   Support for user-defined generation of input split
-   Input splits are lazily assigned to data source tasks for improved
    load balancing.
-   Improved support for low-latency programs; some programs with few
    data got a significant speed-up.
-   Support for Amazon S3 as data sources/sinks
-   Support for Snappy compression
-   MacOS support

### Included Bug Fixes

-   Serialization of PactString data types
-   Generation of small input splits
-   Several bugfixes in Nephele EC2 cloud manager
-   For a complete list of bug fixes, see the [bug
    tracker](http://dev.stratosphere.eu/query?status=closed&order=priority&col=id&col=summary&col=status&col=type&col=priority&col=milestone&col=component&milestone=Release+0.1.1 "http://dev.stratosphere.eu/query?status=closed&order=priority&col=id&col=summary&col=status&col=type&col=priority&col=milestone&col=component&milestone=Release+0.1.1").

Updates and Bug Fixes on Release 0.1.1
--------------------------------------

Updates and Bug Fixes are only available in the source distribution
available in our public Git repository ([build
instructions](buildthesystem.html "buildthesystem")).
  
 Of course, they will be included in the binary distributions of future
releases.
