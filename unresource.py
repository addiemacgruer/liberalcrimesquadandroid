#!/usr/bin/python
import sys

java = sys.argv[1]
xml = sys.argv[2]

def splitxml(string):
	try:
		tag,value = string.split(">",1)
		pre,tag,post = tag.split('"')
		value,post = value.rsplit("<",1)
		return (tag,value)
	except:
		print string
		return ("","")

with open(xml) as ih:
	inpunfiltered = [m.strip() for m in ih.readlines()]

inp = []
array = False
for line in inpunfiltered:
	if array:
		if line.startswith("</string-array>"):
			array=False
		continue
	if line.startswith("<string-array"):
		array=True
		continue
	inp.append(line)

print "Filtered ",len(inpunfiltered)," into ",len(inp)

data = [splitxml(l) for l in inp[2:-1]]

oldlen = len(data)
print len(data)

def substitute(ref):
	global data
	rval = ref
	for tup in data:
		key,value = tup
		if key==ref:
			rval = value
			data.remove(tup)
			break
	else:
		print "Unmatched!",ref
		return None
	return rval

def remrestext(line):
	if "restext" in line:
		offset = line.find("restext")
		offset2 = line.find("R.string.")
		if offset2 < offset:
			return line
		end = line.find(")",offset2)
		assert end > offset2
		replacedtext = substitute(line[offset2+9:end])
		if replacedtext:
			line = line[:offset]+"text(\""+replacedtext+"\")"+line[end+1:]
	return line

def remstring(line):
	offset = line.find("R.string.")
	o2 = offset+9
	while (line[o2] >= "A" and line[o2] <= "Z") or (line[o2] >= "a" and line[o2] <= "z") or (line[o2] >= "0" and line[o2] <= "9"):
		o2+=1
		if (o2==len(line)):
			break
	replacedtext = substitute(line[offset+9:o2])
	if replacedtext:
		line = line[:offset]+'"'+replacedtext+'"'+line[o2:]
	return line

with open(java) as ih:
	javaih = ih.readlines()

with open(java,"w") as oh:
	for line in javaih:
		while "restext" in line:
			oldline = line
			line = remrestext(line)
			if line==oldline:
				break
		while "R.string." in line:
			oldline = line
			line = remstring(line)
			if line==oldline:
				break
		oh.write(line)

print oldlen,"=>",len(data)

with open(xml,"w") as oh:
	oh.write(inp[0]+"\n")
	oh.write(inp[1]+"\n")
	for tup in data:
		oh.write("\t<string name=\"%s\">%s</string>\n" % tup)
	oh.write(inp[-1])
