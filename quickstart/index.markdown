--- 
layout: inner_simple
title: Quick Start - Overview
---

<p style="font-size: 50px;margin-bottom:50px" class="text-center">What would you like to do?</p>


<div class="row">
  <div class="col-md-2">
  </div>
  <div class="col-md-8">
    <p>There are plenty of ways to explore Stratosphere. Install it one one or more machines, if you want to get to know the infrastructure. Application developers can also start immediately with their favorite programming language and run programs locally from within their favorite IDE.</p>
  </div>
  <div class="col-md-2">
  </div>
</div>

<div class="row" style="margin-top:20px">
  <div class="col-md-4">
    <button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','setup',this.href]); location.href='{{ site.baseurl }}/quickstart/build.html'">
      <i class="icon-cloud icon-4x"></i><br> <br>Set up Stratosphere
    </button>
    <br>Install Stratosphere on one or more computers.
  </div>
  <div class="col-md-4">
  	<button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','scala',this.href]); location.href='{{ site.baseurl }}/quickstart/scala.html'">
  		<i class="icon-code icon-4x"></i><br> <br>Write job in Scala
    </button>
    <br>Develop Stratosphere programs with <a href="http://scala-lang.org">Scala</a> and experience Stratosphere's new  concise and flexible programming abstraction. Run and debug your programs locally.
  </div>
  <div class="col-md-4">
    <button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','java',this.href]); location.href='{{ site.baseurl }}/quickstart/java.html'">
      <i class="icon-coffee icon-4x"></i><br> <br>Write job in Java
    </button>
    <br>Write Stratosphere programs with the classic Java API. Run and debug your programs locally.
  </div>
</div>




