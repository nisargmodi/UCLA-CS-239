import csv
import pandas as pd
import numpy as np
from pyspark.mllib.fpm import FPGrowth
from pyspark import SparkContext
import warnings

warnings.simplefilter("ignore")

class Stats:
     def __init__(self):
        self.linesAdded = []
        self.linesDeleted = []
        self.linesChanged = []
        self.noOfAuthors = []
        self.noOfCommits = []
        self.noOfCommitsForBugFixOnly = []

     def display(self):
        print("AvgLinesAdded %.2f" % np.nanmean(np.asarray(self.linesAdded)))
        print("AvgLinesDeleted %.2f" % np.nanmean(np.asarray(self.linesDeleted)))
        print("AvgLinesChanged %.2f" % np.nanmean(np.asarray(self.linesChanged)))
        print("AvgNoOfAuthors %.2f" % np.nanmean(np.asarray(self.noOfAuthors)))
        print("AvgNoOfCommits %.2f" % np.nanmean(np.asarray(self.noOfCommits)))
        print("AvgNoOfCommitsForBugFixOnly %.2f" % np.nanmean(np.asarray(self.noOfCommitsForBugFixOnly)))

     def addAll(self,LA,LD,LC,NA,NC,NCBF):
        self.linesAdded.append(LA)
        self.linesDeleted.append(LD)
        self.linesChanged.append(LC)
        self.noOfAuthors.append(NA)
        self.noOfCommits.append(NC)
        self.noOfCommitsForBugFixOnly.append(NCBF)

     def createStatsCSV(self,name):
        data = pd.DataFrame()
        data['NumberOfLinesAdded'] = pd.Series(self.linesAdded)
        data['NumberOLinesDeleted'] = pd.Series(self.linesDeleted)
        data['NumberOfLinesChangedTotal'] = pd.Series(self.linesChanged)
        data['NumberOfAuthors'] = pd.Series(self.noOfAuthors)
        data['NumberOfCommits'] = pd.Series(self.noOfCommits)
        data['NumberOfCommitsOnlyForBugFix'] = pd.Series(self.noOfCommitsForBugFixOnly)
        data = data.fillna(0)
        data.to_csv('JodaTimeFileStats'+name+'.csv',sep=',',index=False)
        print('JodaTimeFileStats.csv generated')

obj1 = Stats()
obj2 = Stats()
obj3 = Stats()

sc = SparkContext(appName="FPGrowth")
indata = sc.textFile("JodaTimeCommitTransactions.txt")
#indata = sc.textFile("JodaTimeCommitTransactions.txt")
transactions = indata.map(lambda line: line.strip().split(' '))
model = FPGrowth.train(transactions, minSupport=0.005, numPartitions=5)
result = model.freqItemsets().collect()
data = pd.read_csv('JodaTimeFileStats.csv',sep=',')
all_freq = []
lessChangingFiles = 14
moreChangingFiles = 35
for fi in result:
    linesAdded = []
    linesDeleted = []
    linesChanged = []
    noOfAuthors = []
    noOfCommits = []
    noOfCommitsForBugFixOnly = []
    if len(fi.items) >=2:
        for file in fi.items:
                idx = data[data['FileName'] == file].index.tolist()
                if idx:
                    linesAdded.append(data.loc[idx,'AvgLinesAddedPerCommit'].item())
                    linesDeleted.append(data.loc[idx,'AvgLinesDeletedPerCommit'].item())
                    linesChanged.append(data.loc[idx,'AvgLinesChangedPerCommit'].item())
                    noOfAuthors.append(data.loc[idx,'NumberOfAuthors'].item())
                    noOfCommits.append(data.loc[idx,'NumberOfCommits'].item())
                    noOfCommitsForBugFixOnly.append(data.loc[idx,'NumberOfCommitsOnlyForBugFix'].item())
        LA = np.nanmean(np.asarray(linesAdded))
        LD = np.nanmean(np.asarray(linesDeleted))
        LC = np.nanmean(np.asarray(linesChanged))
        NA = np.nanmean(np.asarray(noOfAuthors))
        NC = np.nanmean(np.asarray(noOfCommits))
        NCBF = np.nanmean(np.asarray(noOfCommitsForBugFixOnly))

        all_freq.append(fi.freq)

        if fi.freq < lessChangingFiles:
            obj1.addAll(LA,LD,LC,NA,NC,NCBF)
        else:
            if fi.freq < moreChangingFiles:
                obj2.addAll(LA,LD,LC,NA,NC,NCBF)
            else:
                obj3.addAll(LA,LD,LC,NA,NC,NCBF)

all_freq = np.asarray(all_freq)

print("Non-frequent changing pattern statistics")
obj1.display()
print("Frequent changing pattern statistics")
obj2.display()
print("Very Frequent changing pattern statistics")
obj3.display()

with open('JodaTimeFPMining.txt', 'w',newline='') as f: #newline=''
    writer = csv.writer(f, delimiter=" ")
    writer.writerows(result)
print('JodaTimeFPMining.txt generated')