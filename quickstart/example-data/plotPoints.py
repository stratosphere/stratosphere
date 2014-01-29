#!/usr/bin/python
import sys
import matplotlib.pyplot as plt
import csv
import os

if len(sys.argv) < 3:
  print "Usage: plotPoints.py <file> <outfileprefix>"
  sys.exit(1)

dataFile = sys.argv[1]
# outDir = sys.argv[2]
outFilePx = sys.argv[2]

plotFileName = os.path.splitext(os.path.basename(dataFile))[0]
plotFile = os.path.join(".", outFilePx+"-plot")

########### READ DATA

ps = []
xs = []
ys = []

minP = None
maxP = None
minX = None
maxX = None
minY = None
maxY = None

with open(dataFile, 'rb') as file:
  for line in file:
    # parse data
    csvData = line.strip().split('|')

    p = int(csvData[0])
    x = float(csvData[1])
    y = float(csvData[2])
    
    if not minP or minP > p:
      minP = p
    if not maxP or maxP < p:
      maxP = p
    if not minX or minX > x:
      minX = x
    if not maxX or maxX < x:
      maxX = x
    if not minY or minY > y:
      minY = y
    if not maxY or maxY < y:
      maxY = y

    ps.append(p)
    xs.append(x)
    ys.append(y)

######## PLOT DATA

plt.clf()
plt.scatter(xs, ys, c=ps, s=25, cmap='jet', edgecolors='None', alpha=1.0)
plt.ylim([minY,maxY])
plt.xlim([minX,maxX])

cb = plt.colorbar()

plt.savefig(plotFile+'.pdf', dpi=600)
print "\nPlotted file: "+plotFile+'.pdf'

sys.exit(0)