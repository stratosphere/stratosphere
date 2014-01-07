# a little script for the developers to check if illegal terms are used

grep --color=always --include *.markdown --include *.html -r -P "contract|pact|stratosphere-eu|match" docs/0.4 quickstart/