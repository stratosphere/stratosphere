# a little script for the developers to check if illegal terms are used

grep --color=always --include *.markdown --include *.html -r -E -e "0\\.4|contract|pact|stratosphere-eu" docs/0.4 quickstart/
