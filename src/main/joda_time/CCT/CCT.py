import os
import sys
import pickle

counts = {}
context=[]

for ln in sys.stdin:
  if ln[0:4] == 'CALL':
    # push
    context.append(ln[5:-1]) 

    # get key
    s = ':'.join(context)

    # make sure key exists in both counter dicts
    if not counts.has_key(s):
      counts[s] = 0

    # increment counter
    counts[s] += 1
  else:
    # pop
    context = context[:-1] 

pickle.dump(counts,open(sys.argv[1],'w'))

#for (k,n) in counts.iteritems():
#  print "%s\t%d" % (k,n)


