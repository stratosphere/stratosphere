---
layout: inner_text
title: Downloads 
---

### Development Versions

We keep our development version always as stable as possible, so it's a viable option if you want to start using the system.

#### Building Stratosphere from its sources

Get the source:
You can download the code here: [Download zip](https://github.com/dimalabs/ozone/archive/master.zip) or use Git, if you want to track our changes:

    git clone https://github.com/dimalabs/ozone.git

Go to the Stratosphere directory:

    cd ozone

And build the project using Maven

    mvn -DskipTests clean package # this will take up to 5 minutes

Stratosphere is now installed in `stratosphere-dist/target`
If you’re a Debian/Ubuntu user, you’ll find a .deb package. We will continue with the generic case.

    cd stratosphere-dist/target/stratosphere-dist-0.2-ozone-bin/stratosphere-0.2-ozone/

The directory structure here looks like the contents of an official release distribution.

Please refer to our [README](https://github.com/dimalabs/ozone/blob/master/README.md) for further instructions on running your first code.

### Releases

There are currently no official releases of Stratosphere. Our most recent development version is the best you can get.


