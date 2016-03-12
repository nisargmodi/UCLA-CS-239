import csv
import pandas as pd
import numpy as np
import os
from git import *
join = os.path.join
repo = Repo('joda-time')
# IF THIS GIVES ERROR, PLEASE DOWNLOAD THE FOLDER FROM GIT AND PLACE IT IN YOUR LOCAL
#repo = Repo ("PATH/TO/LOCAL/JODA-TIME/FOLDER")
#repo = Repo ("PATH/TO/LOCAL/ERROR-PRONE/FOLDER")
all_commits = list(repo.iter_commits('master'))
all_commits.reverse()

all_files = [] 
updated_files = []
file_count = {}
file_insertion_count = {}
file_deletion_count = {}
file_totalLinesChanged_count = {} #modified lines
file_authors = {}
all_authors = []
file_count_bugfix = {}
all_bug_only_files = []
changes = ['FIX','BUG','CHANGE','UPDATE','REMOVE']
#######################################################################3
# USING COMMIT OBJ AND UPDATING EVERY FILE COUNT
for item in all_commits:
    changed_files = []
    changed_files_nameonly = []
    sta = item.stats
    com_msg = item.message
    committer = item.committer.name

    #all commiters names
    if committer not in all_authors:
        all_authors.append(committer)

    #files which are only related to bug fixes/changes
    bug_only_files = []
    for x in changes:
        if x in str.upper(com_msg):
            for key,value in sta.files.items():
               bug_only_files.append(key)
            all_bug_only_files.append(bug_only_files)
            break

    for key,value in sta.files.items():
                if key not in updated_files:
                    updated_files.append(key)
                changed_files.append(key)

                # List of committers that have worked on this file
                if key in file_authors:
                    a_list = file_authors[key]
                else:
                    a_list = []
                if committer not in a_list:
                    a_list.append(committer)
                file_authors[key] = a_list

                # Number of lines added/deleted/modified per file
                if key in file_insertion_count:
                    for k,v in value.items():
                        if k == 'insertions':
                            file_insertion_count[key] += v
                        if k == 'deletions':
                            file_deletion_count[key] += v
                        else:
                            file_totalLinesChanged_count[key] += v
                else:
                    for k,v in value.items():
                        if k == 'insertions':
                            file_insertion_count[key] = v
                        if k == 'deletions':
                            file_deletion_count[key] = v
                        else:
                            file_totalLinesChanged_count[key] = v

                # Number of commits per file
                if key in file_count:
                    file_count[key] += 1
                else:
                    file_count[key] = 1

                # Number of commits per file w.r.t to any bug/fix/change only
                for x in changes:
                    if x in str.upper(com_msg):
                        if key in file_count_bugfix:
                            file_count_bugfix[key] += 1
                        else:
                            file_count_bugfix[key] = 1
                        break
    all_files.append(changed_files)

with open('JodaTimeCommitTransactions.txt', 'w',newline='') as f: #newline=''
    writer = csv.writer(f, delimiter=" ")
    writer.writerows(all_files)
print('JodaTimeCommitTransactions.txt generated')

###################################################################
# GENERATE STATS
data = pd.DataFrame(columns=['FileName'])
data['FileName'] = pd.Series(updated_files)
sLength = len(data['FileName'])
for key in updated_files:
    idx = data[data['FileName'] == key].index.tolist()
    if idx:
        if key in file_insertion_count:
            data.loc[idx,'NumberOfLinesAdded'] = int(file_insertion_count[key])
            data.loc[idx,'NumberOLinesDeleted'] = int(file_deletion_count[key])
            data.loc[idx,'NumberOfLinesChangedTotal'] = int(file_totalLinesChanged_count[key])
            data.loc[idx,'NumberOfAuthors'] = len(file_authors[key])
            data.loc[idx,'NumberOfCommits'] = int(file_count[key])
            if key in file_count_bugfix:
                data.loc[idx,'NumberOfCommitsOnlyForBugFix'] = int(file_count_bugfix[key])
data = data.fillna(0)
data['AvgLinesAddedPerCommit'] = np.round(data['NumberOfLinesAdded']/data['NumberOfCommits'],2)
data['AvgLinesDeletedPerCommit'] = np.round(data['NumberOLinesDeleted']/data['NumberOfCommits'],2)
data['AvgLinesChangedPerCommit'] = np.round(data['NumberOfLinesChangedTotal']/data['NumberOfCommits'],2)
#print(data.describe())
cols = ['FileName','NumberOfLinesAdded','NumberOLinesDeleted','NumberOfLinesChangedTotal',
        'NumberOfAuthors','NumberOfCommits','NumberOfCommitsOnlyForBugFix',
        'AvgLinesAddedPerCommit','AvgLinesDeletedPerCommit','AvgLinesChangedPerCommit']
data.to_csv('JodaTimeFileStats.csv',sep=',',columns=cols,index=False)
print('JodaTimeFileStats.csv generated')
# # ####################################################################
