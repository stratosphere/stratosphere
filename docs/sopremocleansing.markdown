---
layout: documentation
---
Sopremo Cleansing
-----------------

Operators in this package improvde the quality of data sets. In most
cases, they require structured or at least semi-structured data.

The current set of operators listed with their Sopremo and Meteor names
as well as their properties.

Category

Sopremo Name

Meteor Name

\#Inputs

Meteor Properties

Comments

Basic

Scrub

scrub

1

rules

EntityExtraction

split records

1

projections

DuplicateDetection

detect duplicates

1

similarity measure, threshold, sorting or partition key

Fusion

fuse

1

strategies, weights

Complex

DuplicateRemoval

remove duplicates

1

similarity measure, threshold, sorting or partition key, strategies

ClusterRecords

link/cluster records

n

similarity measure, threshold, sorting or partition key, projection
