--- 
layout: inner_simple
title: Quick Start - Overview
---

<p style="font-size: 50px;margin-bottom:50px" class="text-center">What do you want to do?</p>


<div class="row">
  <div class="col-md-2">
  </div>
  <div class="col-md-8">
    <p>There are plenty of ways to start using Stratosphere. Install it, if you want to get to know the infrastructure. Application developers should start immediately with their favorite programming language.</p>
  </div>
  <div class="col-md-2">
  </div>
</div>

<div class="row" style="margin-top:20px">
  <div class="col-md-4">
    <button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','setup',this.href]); location.href='{{ site.baseurl }}/quickstart/build.html'">
      <i class="icon-cloud icon-4x"></i><br> <br>Set up Stratosphere
    </button>
    <br>Install on your computer or on a cluster to run jobs.
  </div>
  <div class="col-md-4">
  	<button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','scala',this.href]); location.href='{{ site.baseurl }}/quickstart/scala.html'">
  		<i class="icon-code icon-4x"></i><br> <br>Write job in Scala
    </button>
    <br>Develop Stratosphere jobs with Scala. Run and debug them locally.
  </div>
  <div class="col-md-4">
    <button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','java',this.href]); location.href='{{ site.baseurl }}/quickstart/java.html'">
      <i class="icon-coffee icon-4x"></i><br> <br>Write job in Java
    </button>
    <br>You can alsow write jobs in Java, if you are not familar with Scala.
  </div>
</div>




