---
layout: documentation
---
Git workflow
============

Initial import into an empty repository
---------------------------------------

-   Clone the repository you want to import into your empty repository

<!-- -->

    git clone https://<user>@dev.stratosphere.eu/git/<importRep>.git
    Cloning into <importRep>...

-   Change into the repository directory

<!-- -->

    cd <importRep>

-   Push the repository content into the master branch of your empty
    remote repository

<!-- -->

    git push https://<user>@dev.stratosphere.eu/git/<user>.git HEAD:master
    Fetching remote heads...
      refs/
      refs/tags/
      refs/heads/
    updating 'refs/heads/master' using 'HEAD'
      from 0000000000000000000000000000000000000000
      to   d62951afd832fdee6f47de8cc755ce6aa3391d88
        sending 15699 objects
        done
    Updating remote server info
    To https://<user>@dev.stratosphere.eu/git/<user>.git
      * [[new|branch]]      HEAD -> master

-   Clean up and remove the cloned repository

<!-- -->

    cd ..

    rm -r <importRep>

-   Continue cloning your repository

How to integrate master branch from \<other\_user\>
---------------------------------------------------

-   Git clone of private repository

<!-- -->

      git clone https://<user>@dev.stratosphere.eu/git/<user>.git
      
      git branch
      ->  * master

      git remote
      -> origin

-   Add branch from other user

<!-- -->

      git remote add <other_user> https://<user>@dev.stratosphere.eu/git/<other_user>.git
      
      git fetch -v <other_user>
      
      git branch
      -> * master

      git checkout -b <other_user> remotes/<other_user>/master
      
      git branch
      -> * <other_user>
           master

-   Merge branches

<!-- -->

      git checkout master
      -> Switched to branch 'master'

      git merge <other_user>
      
      git branch
      ->   <other_user>
          * master

-   Push changes to private directory

<!-- -->

      git push origin master

How to merge own repository with stratosphere master branch
-----------------------------------------------------------

    git checkout -b stratosphere remotes/stratosphere/master

    git fetch -v stratosphere

    git merge master

    git push stratosphere master
