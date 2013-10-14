--- 
layout: inner_simple
title: Quick Start
---

<p class="lead">Three easy steps to Stratosphere: Build, Start &amp; Run.</p>

<h3><a id="build"></a>1. Build</h3>

Stratosphere runs on all *UNIX-like* environments: **Linux**, **Mac OS X**, **Cygwin** on **Windows**. The only requirements are Java (6 or 7), maven, and git.

We check out the latest version from GitHub and build it with maven.

<pre class="prettyprint" style="padding-left:1em">
git clone https://github.com/stratosphere/stratosphere.git
cd stratosphere
mvn clean package -DskipTests
</pre>

Stratosphere is now installed in `stratosphere-dist/target`.
If you’re a Debian/Ubuntu user, you’ll also find a `.deb` package here.

<h3><a id="start"></a>2. Start</h3>

The newly created directory is identical to the contents of the official release distribution.

We change to it with:

<pre class="prettyprint" style="padding-left:1em">
cd stratosphere-dist/target/stratosphere-dist-0.2-ozone-bin/stratosphere-0.2-ozone/
</pre>

Now, we can start Stratosphere in local mode:

<pre class="prettyprint" style="padding-left:1em">
./bin/start-local.sh
</pre>

<h3><a id="run"></a>3. Run</h3>

We will run a simple “Word Count” example. Get some test data:

<pre class="prettyprint" style="padding-left:1em">
wget -O hamlet.txt http://www.gutenberg.org/cache/epub/1787/pg1787.txt
</pre>

Start the job:

<pre class="prettyprint" style="padding-left:1em">
./bin/pact-client.sh run \
    --jarfile ./examples/pact/pact-examples-0.2-ozone-WordCount.jar \
    --arguments 1 file://`pwd`/hamlet.txt file://`pwd`/wordcount-result.txt
</pre>

You will find a file called `wordcount-result.txt` in your current directory.