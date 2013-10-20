---
layout: inner_simple
title: Code 
---


## Building Stratosphere from scratch

We keep our development version always as stable as possible, so it's a viable option if you want to start using the system.

Get the source:
You can download the code here: [Download zip](https://github.com/stratosphere/stratosphere/archive/master.zip) or use Git, if you want to track our changes:

    git clone https://github.com/stratosphere/stratosphere.git

Go to the project directory:

    cd stratosphere

And build the project using Maven

    mvn -DskipTests clean package # this will take up to 5 minutes

The distribution binaries are assembled under

    stratosphere-dist/target/stratosphere-dist-0.2-ozone-bin/stratosphere-0.2-ozone/

Please refer to our [README](https://github.com/stratosphere/stratosphere/blob/master/README.md) for further instructions on running your first code.

<div class="notice-important">
	Under <code>stratosphere-dist/target/stratosphere-dist-0.2-ozone.deb</code> you will find a Debian package that can be used with your OS package manager if you are a Debian or Ubuntu user.
</div> 

### What is the difference between Stratosphere and Ozone?</h3>
<dl class="dl-horizontal">
	<dt>Stratosphere</dt> <dd>is the official name of the DFG-funded research project.</dd>
	<dt>Ozone</dt> <dd>is the stable codebase of the <em>Stratosphere</em> system, developed and maintained transparently as an open-source project on GitHub.</dd>
</dl>

<div class="notice-important">
	We encourage interested parties to fork <em>Ozone</em> as a starting point for research prototypes, or use it as a base for the development of stable applications on top of the <em>Stratosphere</em> stack.
</div>


## Releases

There are currently no official releases in the <em>Ozone</em> project. The most recent development version is the best you can get.

## Support

Don't hesitate to ask if you have any questions. The best way to get fast and good help is to [open an issue on GitHub](https://github.com/stratosphere/stratosphere/issues/new).

<!--

<br>
 <div class="text-center">
<a href="https://github.com/stratosphere/stratosphere/issues/new" class="btn btn-primary">Open an Issue on Github</a>
</div>
<hr>

If you don't want to register on github, we also have a users mailing list:<br>
	<a href="mailto:users@stratosphere.tu-berlin.de">users@stratosphere.tu-berlin.de</a>
<p>
	<strong>Prior to usage, you need to register at the mailing list:</strong><br>
	<a href="https://lists.tu-berlin.de/mailman/listinfo/stratosphere-users">users@stratosphere.tu-berlin.de registration</a>
</p> -->


