--- 
layout: inner_with_sidebar
title: "Quick Start: Example"
description: Run an example Stratosphere Program
keywords: stratosphere, scala, program, howto, quickstart, big data, data analytics
links:
  - {anchor: "data", title: "Generate Input Data"}
  - {anchor: "run", title: "Run Clustering"}
  - {anchor: "result", title: "Analyze the Result"}
---


<p class="lead">
	This guide will demonstrate Stratosphere's features by example. You will see how you can leverage Stratosphere's Iteration-feature to find clusters in a dataset using <a href="http://en.wikipedia.org/wiki/K-means_clustering">K-Means clustering</a>.<br>
	On the way, you will see the compiler, the status interface and the result of the algorithm.
</p>


<section id="data">
  <div class="page-header">
  	<h2>Generate Input Data</h2>
  </div>
  <p>Stratosphere contains a data generator for K-Means.</p>
  {% highlight bash %}
# Download Stratosphere (Development version)
wget http://stratosphere-bin.s3-website-us-east-1.amazonaws.com/stratosphere-0.5-SNAPSHOT.tgz
tar xzf stratosphere-0.5-SNAPSHOT.tgz 
cd stratosphere
mkdir kmeans
cd kmeans
# run data generator
java -cp ../examples/stratosphere-java-examples-0.5-SNAPSHOT-KMeansIterative.jar eu.stratosphere.example.java.record.kmeans.KMeansSampleDataGenerator 500 10 0.08
  {% endhighlight %}
The generator has the following arguments:
{% highlight bash %}
KMeansDataGenerator <numberOfDataPoints> <numberOfClusterCenters> [<relative stddev>] [<centroid range>] [<seed>]
{% endhighlight %}
The *relative standard deviation* is an interesting tuning parameter: it determines the closeness of the points to the centers.
<p>The `kmeans/` directory should now contain two files: <code>centers</code> and <code>points</code>.</p>


<h2>Review Input Data</h2>
Use the `plotPoints.py` tool to review the result of the data generator. <a href="{{site.baseurl}}/quickstart/example-data/plotPoints.py">Download Python Script</a>
{% highlight bash %}
python2.7 plotPoints.py points input
{% endhighlight %}

The following overview presents the impact of the different standard deviations on the input data.
<div class="row" style="padding-top:15px">
	<div class="col-md-4">
		<div class="text-center" style="font-weight:bold;">relative stddev = 0.03</div>
		<a data-lightbox="inputs" href="{{site.baseurl}}/img/quickstart-example/kmeans003.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/kmeans003.png" /></a>
	</div>
	<div class="col-md-4">
		<div class="text-center" style="font-weight:bold;">relative stddev = 0.08</div>
		<a data-lightbox="inputs" href="{{site.baseurl}}/img/quickstart-example/kmeans008.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/kmeans008.png" /></a>
	</div>
	<div class="col-md-4">
		<div class="text-center" style="font-weight:bold;">relative stddev = 0.15</div>
		<a data-lightbox="inputs" href="{{site.baseurl}}/img/quickstart-example/kmeans015.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/kmeans015.png" /></a>
	</div>
</div>
</section>

<section id="run">
 <div class="page-header">
  	<h2>Run Clustering</h2>
  </div>
We are using the generated input data to run the clustering using a Stratosphere job.
{% highlight bash %}
# go to the Stratosphere-root directory
cd stratosphere
# start Stratosphere (use ./bin/start-cluster.sh if you're on a cluster)
./bin/start-local.sh
# Start Stratosphere web client
./bin/start-webclient.sh
{% endhighlight %}

<h2>Review Stratosphere Compiler</h2>

The Stratosphere webclient allows to submit Stratosphere programs using a graphical user interface.

<div class="row" style="padding-top:15px">
	<div class="col-md-6">
		<a data-lightbox="compiler" href="{{site.baseurl}}/img/quickstart-example/run-webclient.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/run-webclient.png" /></a>
	</div>
	<div class="col-md-6">
		1. <a href="http://localhost:8080/launch.html">Open webclient on localhost:8080</a> <br>
		2. Upload the 
{% highlight bash %}
examples/stratosphere-java-examples-0.5-SNAPSHOT-KMeansIterative.jar
{% endhighlight %} file.<br>
		3. Select it in the left box to see how the operators in the plan are connected to each other. <br>
		4. Enter the arguments in the lower left box:
{% highlight bash %}
1 file://<pathToGenerated>points file://<pathToGenerated>centers file://<pathToGenerated>result 10
{% endhighlight %}
For example:
{% highlight bash %}
1 file:///home/robert/stratosphere-0.5-SNAPSHOT/kmeans/points file:///home/robert/stratosphere-0.5-SNAPSHOT/kmeans/centers file:///home/robert/stratosphere-0.5-SNAPSHOT/kmeans/result 10
{% endhighlight %}
	</div>
</div>
<hr>
<div class="row" style="padding-top:15px">
	<div class="col-md-6">
		<a data-lightbox="compiler" href="{{site.baseurl}}/img/quickstart-example/compiler-webclient.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/compiler-webclient.png" /></a>
	</div>

	<div class="col-md-6">
		1. Press the <b>RunJob</b> to see the optimzer plan. <br>
		2. Inspect the operators and see the properties (input sizes, cost estimation) determined by the optimizer.
	</div>
</div>
<hr>
<div class="row" style="padding-top:15px">
	<div class="col-md-6">
		<a data-lightbox="compiler" href="{{site.baseurl}}/img/quickstart-example/jobmanager-running.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/jobmanager-running.png" /></a>
	</div>
	<div class="col-md-6">
		1. Press the <b>Continue</b> button to start executing the job. <br>
		2. <a href="http://localhost:8080/launch.html">Open Stratosphere's monitoring interface</a> to see the job's progress.<br>
		3. Once the job has finished, you can analyize the runtime of the individual operators.
	</div>
</div>
</section>

<section id="result">
 <div class="page-header">
  	<h2>Analyze the Result</h2>
  </div>
Use the <a href="{{site.baseurl}}/quickstart/example-data/plotPoints.py">Python Script</a> again to visualize the result

{% highlight bash %}
python2.7 plotPoints.py result/points result
{% endhighlight %}

The following three pictures show the results for the sample input above. Play around with the parameters (number of iterations, number of clusters) to see how they affect the result.

<div class="row" style="padding-top:15px">
	<div class="col-md-4">
		<div class="text-center" style="font-weight:bold;">relative stddev = 0.03</div>
		<a data-lightbox="results" href="{{site.baseurl}}/img/quickstart-example/result003.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/result003.png" /></a>
	</div>
	<div class="col-md-4">
		<div class="text-center" style="font-weight:bold;">relative stddev = 0.08</div>
		<a data-lightbox="results" href="{{site.baseurl}}/img/quickstart-example/result008.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/result008.png" /></a>
	</div>
	<div class="col-md-4">
		<div class="text-center" style="font-weight:bold;">relative stddev = 0.15</div>
		<a data-lightbox="results" href="{{site.baseurl}}/img/quickstart-example/result015.png" data-lightbox="example-1"><img class="img-responsive" src="{{site.baseurl}}/img/quickstart-example/result015.png" /></a>
	</div>
</div>



<!-- 1 file:///home/robert/Projekte/ozone/ozone/stratosphere-dist/target/stratosphere-dist-0.5-SNAPSHOT-bin/stratosphere-0.5-SNAPSHOT/kmeans/points file:///home/robert/Projekte/ozone/ozone/stratosphere-dist/target/stratosphere-dist-0.5-SNAPSHOT-bin/stratosphere-0.5-SNAPSHOT/kmeans/centers file:///home/robert/Projekte/ozone/ozone/stratosphere-dist/target/stratosphere-dist-0.5-SNAPSHOT-bin/stratosphere-0.5-SNAPSHOT/kmeans/result 10 -->


</section>