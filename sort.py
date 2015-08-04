#!/usr/bin/python

import os
from xml.etree import ElementTree as ET

def namesort(x,y):
	if y.tag=="idname":
		return 1
	if x.tag < y.tag:
		return -1
	if x.tag > y.tag:
		return 1
	return 0

for f in sorted(os.listdir(".")):
	if not f.endswith(".xml"):
		continue
	if f.startswith("sitemaps"): # this is naughty, an xml file that depends on order
		continue
	print f
	xp = ET.parse(f)
	for y in xp.iter():
		y.getchildren().sort(namesort)
	xp.write(f)
