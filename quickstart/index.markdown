--- 
layout: inner_simple
title: Quick Start - Overview
---

<div class="page-header text-center">
  <h1><strong>What would you like to do?</strong></h1>
</div>



<div class="row">
    <p class="lead"><strong>There are plenty of ways to explore Stratosphere</strong>. Install it one one or more machines, if you want to get to know the infrastructure. Application developers can also start immediately with their favorite programming language and run programs locally from within their favorite IDE.</p>
</div>

<div class="row" style="margin-top:20px">
  <div class="col-md-4">
    <button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','setup',this.href]); location.href='{{ site.baseurl }}/quickstart/setup.html'">
      <i class="icon-cloud icon-4x"></i><br> <br>Setup Stratosphere
    </button>
    
    <div class="text-center" style="font-weight: bold; font-size: 1.2em; margin-top: 1em;">Install Stratosphere on one or more computers.</div>
  </div>
  <div class="col-md-4">
  	<button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','scala',this.href]); location.href='{{ site.baseurl }}/quickstart/scala.html'">
  		<i class="icon-code icon-4x"></i><br> <br>Write job in Scala
    </button>
    <div class="text-center" style="font-weight: bold; font-size: 1.2em; margin-top: 1em;">Develop Stratosphere programs with <a href="http://scala-lang.org">Scala</a> and experience Stratosphere's new  concise and flexible programming abstraction. Run and debug your programs locally.</div>
  </div>
  <div class="col-md-4">
    <button type="button" class="btn btn-primary btn-lg btn-block gettingstarted-choices" onclick="_gaq.push(['_trackEvent','Quickstart','java',this.href]); location.href='{{ site.baseurl }}/quickstart/java.html'">
      <i class="icon-coffee icon-4x"></i><br> <br>Write job in Java
    </button>
    <div class="text-center" style="font-weight: bold; font-size: 1.2em; margin-top: 1em;">Write Stratosphere programs with the classic Java API. Run and debug your programs locally.
  </div>
</div>