---
layout: documentation
---
Helpful Scripts
===============

Execute commands on each cluster/cloud node
-------------------------------------------

Sometimes it is required to run one command on all nodes of the
cluster/cloud. If the ssh access is configured properly as described in
the [cluster
setup](clustersetup#sshaccess "clustersetup")
you are able to set up a small shell script to make this task easier.
You need a file with the IP/host name of those nodes the command should
be executed on. Each entry must be separated by a line break. (As that
kind of list is needed to configure both HDFS and stratosphere you may
have one already)

    #!/bin/bash

    for SLAVE in ''cat <ip-list>''; 
    do
            echo "---- '$1' on $SLAVE ----"
            export var=''ssh -i <ssh-key> nephele@$SLAVE $1''
            echo $var
    done

Where \<ip-list\> must be replaced with the absolute path to the file
mentioned above and \<ssh-key\> and with the absolute path to the
private ssh-key. 'nephele' is the user existing on all nodes as
described in the [cluster
setup](clustersetup.html "clustersetup").
Save the script (e.g. as “forall.sh”) and add the the permission to
execute it.

    chmod +x forall.sh

Now you can call it with the command as parameter.

    ./forall.sh "cat .bashrc"

**Note:**Changes of the environment (e.g. exporting variables) does not
work with this script, because the changes will only exists in the bash
instance the `ssh` command starts on connection and stops after
executing the given command. Hence **./forall.sh 'export foo=bar'**
would not have any effect. Maybe **./forall.sh 'echo “export foo=bar”
\>\> .profile'** will do the job. Irrespective of the `ssh` command,
executing a bash-script forks the current bash, the bash executing the
commands of the script is a child-process. Hence changes of the
environment will not have any effect on the parent bash.
