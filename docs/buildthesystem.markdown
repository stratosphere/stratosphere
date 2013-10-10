---
layout: documentation
---
Build the System
================

Get the Source Code
-------------------

The latest source code is available in our Git repository.
Unfortunately, our SSL certificate does not have a valid signature chain
at the moment. You have to disable GIT's check for that. To check out
the code execute:

    git clone git://github.com/stratosphere-eu/stratosphere.git

Compile the Source Code and Build the System
--------------------------------------------

-   Change into the source code root folder:

<!-- -->

    cd stratosphere

-   Compile the code and build the system using [Maven
    3](http://maven.apache.org/download.html "http://maven.apache.org/download.html").
    The build process will take about 7 minutes. *Unfortunatelly,
    unit-tests cannot be disabled with `-DskipTests` due to a dependency
    to test code.*

<!-- -->

    mvn clean package

The build resides in

    ./stratosphere-dist/target/stratosphere-dist-0.2-stratosphere-bin/stratosphere-0.2
    ./stratosphere-dist/target/stratosphere-dist-0.2-stratosphere-bin/stratosphere-0.2.zip
    ./stratosphere-dist/target/stratosphere-dist-0.2-stratosphere-bin/stratosphere-0.2.tar.gz
