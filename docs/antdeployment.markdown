---
layout: documentation
---
Deployment with Ant
-------------------

This page briefly explains how the deployment of Pact Programs can be
automated with Ant. The solution consists of an ant file that builds the
jar file and deploys it automatically on a predefined server with scp.

The ant file contains four targets that depend on the respective
previous target

-   Prepare
-   Compile
-   Make-jar
-   Deploy-on-stratosphere

The referenced build.properties contains important configuration data
such as server user name and password. The login data as well as the
base directory to stratopshere need to be adjusted.

The attached ant file also adds the MANIFEST.MF in the META-INF folder
to the jar file. The manifest points to the assembler class.

To execute the ant file, select run as → ant build from the context menu
or menu toolbar.

TODO: port to maven? certificate based authentication?
