import os
import subprocess

import git
from git import *
join = os.path.join
# rorepo is a Repo instance pointing to the git-python repository.
# For all you know, the first argument to Repo is a path to the repository
# you want to work with
#repo = Repo(self.rorepo.working_tree_dir)
#assert not repo.bare

#repo = Repo ("E:\\Punit\\D\\UCLA\\Winter15\\CS239\\Code\\ProjectA")
repo = Repo ("E:\\Punit\\D\\UCLA\\Winter15\\CS239\\Project2\\joda-time")
#repo = git.Repo("https://github.com/jhy/jsoup")
#o = repo.remotes.origin
#o.pull()

# xyz = repo.commit('master')
fifty_first_commits = list(repo.iter_commits('master', max_count=500)) #
fifty_first_commits.reverse()
#assert len(fifty_first_commits) == 50
# this will return commits 21-30 from the commit list as traversed backards master
#ten_commits_past_twenty = list(repo.iter_commits('master', max_count=10, skip=20))
#assert len(ten_commits_past_twenty) == 10
#assert fifty_first_commits[20:30] == ten_commits_past_twenty
#print(len(fifty_first_commits))

# for item in fifty_first_commits:
#     headcommit = item #repo.head.commit
#     tree = headcommit.tree
#     print(len(tree.blobs) + len(tree.trees)) # == len(tree)
#     print(len(list(tree.traverse())))
#     #currentCommit = repo.commit(currentBranch)
#     #compCommit = repo.commit(compBranch)
#     #diffed = repo.diff(item, item.parents[0])
#     #subprocess.check_output(['git', 'diff', '--name-only', item + '..' + item.parents[0]])
#
#     for parent in item.parents:
#         changed_files = []
#
#         for x in item.diff(parent):
#             if x.a_blob.path not in changed_files:
#                 changed_files.append(x.a_blob.path)
#
#             if x.b_blob is not None and x.b_blob.path not in changed_files:
#                 changed_files.append(x.b_blob.path)
#
#         print(changed_files)

all_files = []
for item in fifty_first_commits:
    # item = fifty_first_commits[i]
    # parent = fifty_first_commits[i-1]
    tree = item.tree
    # print(len(tree.blobs) + len(tree.trees)) # == len(tree)
    #print(len(list(tree.traverse())))
    #currentCommit = repo.commit(currentBranch)
    #compCommit = repo.commit(compBranch)
    #diffed = repo.diff(item, item.parents[0])
    #subprocess.check_output(['git', 'diff', '--name-only', item + '..' + item.parents[0]])


    changed_files = []
    for parent in item.parents:
        for x in item.diff(parent):
            if x.a_blob is not None and x.a_blob.path not in changed_files:
                changed_files.append(x.a_blob.path)

            if x.b_blob is not None and x.b_blob.path not in changed_files:
                changed_files.append(x.b_blob.path)

    #print(changed_files)
    new = ['ADD','SETUP','MORE','MOVE','INITIAL','FIRST','SUPPORT']
    changes = ['UPDATE','FIX','CHANGE','FINISH','DROP','REMOVE','INTEGRATE']
    found = False
    if changed_files:
        com_msg = item.message # == 40
        for x in new:
            if x in str.upper(com_msg):
                changed_files.append('New')
                #print(com_msg)
                found = True
                break
        if found == True:
            all_files.append(changed_files)
        else:
            for x in changes:
                if x in str.upper(com_msg):
                    changed_files.append('Change')
                    #print(com_msg)
                    found = True
                    break
            if found == True:
                all_files.append(changed_files)
            else:
                changed_files.append('Other')
                all_files.append(changed_files)

    #print(len(headcommit.message))


    # # assert len(headcommit.parents) > 0
    # # assert headcommit.tree.type == 'tree'
    # print(headcommit.author.name) # == 'Sebastian Thiel'
    # # assert isinstance(headcommit.authored_date, int)
    #print(item.committer.name) # == 'Sebastian Thiel'
    # # assert isinstance(headcommit.committed_date, int)
import csv
with open('outputwithlabel.txt', 'w',newline='') as f: #newline=''
    writer = csv.writer(f, delimiter=" ")
    writer.writerows(all_files)

    # for entry in tree.traverse():
    #     #for entry in tree.trees: # intuitive iteration of tree members
    #     # for blob in entry:
    #         # for x in blob:
    #         #     print(x.path)
    #     #count = count + 1
    #     #print(len(entry))
    #     print(entry.name)
    #print(count)


    #blob = tree.trees[0].blobs[0]
    #print(blob.name)
    #
    # #print(repo.tree(headcommit))
    # for j in tree:
    #     print(j)
    #     # for k in repo.git.diff(j):
    #     #     print(k)
    #     print(j.abspath)
    #     #print(j.__doc__)
    # #chk = repo.git.diff(tree)


    # if not item.parents:
    #     # First commit, don't know what to do
    #     continue
    # else:
    #     # Has a parent
    #     diff = item.diff(item.parents[0], create_patch=True)
    #
    # for k in diff:
    #     try:
    #         # Get the patch message
    #         msg = k.diff.decode(defenc)
    #         print(msg)
    #     except UnicodeDecodeError:
    #         continue
    # #prin(headcommit._get_intermediate_items(xyz))
    # print(len(headcommit.message))
    # print(headcommit.message) # == 40
    # # assert len(headcommit.parents) > 0
    # # assert headcommit.tree.type == 'tree'
    # print(headcommit.author.name) # == 'Sebastian Thiel'
    # # assert isinstance(headcommit.authored_date, int)
    # print(headcommit.committer.name) # == 'Sebastian Thiel'
    # # assert isinstance(headcommit.committed_date, int)
    # # assert headcommit.message != ''