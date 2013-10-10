---
layout: documentation
---
Stratosphere Development with Eclipse
=====================================

Eclipse Plugins
---------------

Developing Stratosphere with the [Eclipse
IDE](http://eclipse.org/ "http://eclipse.org/") becomes more comfortable
if a set of plugins is installed. The following plugins are helpful:

-   Maven Eclipse Integration m2eclipse
    [http://eclipse.org/m2e/](http://eclipse.org/m2e/ "http://eclipse.org/m2e/")
    - Maven takes care of the project setup, dependency resolution, and
    code building.
-   Eclipse EGit Plugin
    [http://www.eclipse.org/egit/](http://www.eclipse.org/egit/ "http://www.eclipse.org/egit/")
    - Integrates the Git version control system into Eclipse
-   Eclipse JGit Plugin
    [http://www.eclipse.org/jgit/](http://www.eclipse.org/jgit/ "http://www.eclipse.org/jgit/")
    - A pure Java-based implementation of the Git version control
    system. Required by EGit.

Eclipse Code Style Configuration
--------------------------------

Please configure your Eclipse to ensure your code is compliant to the
projects code style.   
 The Eclipse code style settings of the Stratosphere project are
collected in the file
[stratosphere\_codestyle.pref](media/wiki/stratosphere_codestyle.zip "wiki:stratosphere_codestyle.zip")
that can be imported with File→Import→General/Preferences.

Import Source Code into Eclipse
-------------------------------

The Eclipse source code import is done in two stages:

1.  Clone the public Stratosphere repository.
2.  Import the code into Eclipse

### Clone Public Stratosphere Git Repository

Clone the latest revision from our public github repository:

    git clone https://github.com/stratosphere-eu/stratosphere.git

The source code will be placed in a directory called `stratosphere`. See
[here](sourcecodestructure.html "sourcecodestructure")
for information on the structure of the source code.

### Import Source Code into Eclipse

Import the Stratosphere source code using Maven's Import tool:

-   Select “Import” from the “File”-menu.
-   Expand “Maven” node, select “Existing Maven Projects”, and click
    “next” button
-   Select the root directory by clicking on the “Browse” button and
    navigate to the top folder of the cloned Stratosphere Git
    repository.
-   Ensure that all projects are selected and click the “Finish” button.

Next, you need to connect imported sources with the EGit plugin:

-   Select all (sub-)projects.
-   Right-click on projects and select “Team” → “Share Project…” (NOT
    “Share Project*s*…”)
-   Select Git as repository type and click on “Next” button.
-   Ensure all projects are selected and click on “Finish” button.

Now, your finished with the source code import.

Eclipse and JGit
----------------

You can use JGIT to pull from and push to your repository.

Alternatively, you can issue pull and push commands using Git's default
command line client. Sometimes, that irritates Eclipse so that you must
refresh and manually clean your Eclipse projects. This is done as
follow:

1.  **Refresh:** Select all Stratosphere projects in your Package
    Explorer and hit the “F5” button.
2.  **Clean:** Select all Stratosphere projects in your Package Explorer
    and select “Project” → “Clean…” from the menu. Select “Clean all
    projects” and click the “Ok” button.

Run and Debug Stratosphere via Eclipse
--------------------------------------

Running and debugging the Stratosphere system in local mode is quite
easy. Follow these steps to run / debug Stratosphere directly from
Eclipse:

-   Use the *Run* menu and select *Run Configurations* to open the run
    configurations dialog. Create a new launch configuration. For
    debugging, create a corresponding *Debug Configuration*.
-   On the *Main* tab, select `nephele-server` as the project.
-   As the *Main-Class*, search and select *JobManager* from the package
    `eu.stratoshere.nephele.jobmanager`.
-   After having selected the JobManager as the main class, change the
    *Project* to `pact-tests`. That step is important, because the
    selected project determines how the class path will be constructed
    for the launch configuration, and we need to make sure that the pact
    classes are in the class path as well. Directly choosing
    `pact-tests` as the project in the earlier step prevents the main
    class search from finding the JobManager class (seems to be an
    Eclipse Bug, may possibly be fixed in current/later Eclipse
    versions).
-   Switch to the *Arguments* tab.
-   Under *Program Arguments*, enter
    `-configDir ”<path/to/config/dir/>” -executionMode “local”`. The
    configuration directory must include a valid configuration. The
    default configuration is good for local mode operation, so you can
    use the configuration in the
    'stratosphere-dist/src/main/stratosphere-bin/conf', which resides in
    the *stratosphere-dist* project.
-   Under *VM Arguments*, enter `-Djava.net.preferIPv4Stack=true`. You
    may also configure your heap size here, by adding for example
    `-Xmx768m -Xms768m` to dedicate 768 Megabytes of memory to the
    system.
-   Hit *Run* / *Debug* to launch the JVM running a job-manager in local
    mode.

When the JobManager is up and running, you can issue any Nephele job
against the system, either as original Nephele job or resulting from
compilation of a PACT program. The run / debug configuration needs only
to be configured once.
