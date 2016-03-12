
# Code to perform Frequent Pattern Mining on version history data collected using GitPython

from pyspark.mllib.fpm import FPGrowth
from pyspark import SparkContext

sc = SparkContext(appName="FPGrowth")

#data = sc.textFile("ErrorProneCommitTransactions.txt")
data = sc.textFile("jodatime-groundtruth.txt")
transactions = data.map(lambda line: line.strip().split(' '))
model = FPGrowth.train(transactions, minSupport=0.004, numPartitions=4)
result = model.freqItemsets().collect()
sorted_data = sorted(result, key = lambda itemset : len(itemset.items))

f = open('temp','w')
prev_len = 1

for fi in sorted_data:
    cur_len = len(fi.items)

    if cur_len != prev_len:
        f.close()
        fname = "sup0.004-set-" + str(cur_len) +".txt"
        f = open(fname,'a')
        print "file created"
    prev_len = cur_len
    if len(fi.items) > 1:
        itemlist = ' '.join(str(x) for x in fi.items)
        f.write(itemlist)
        f.write('\n')
        #w.writerow(fi.items)

f.close()
