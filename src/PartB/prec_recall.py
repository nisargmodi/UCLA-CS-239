from itertools import chain, combinations
from itertools import izip
import matplotlib.pyplot as plt

def powerset(iterable):
    s = list(iterable)
    return chain.from_iterable(combinations(s, r) for r in range(len(s)+1))

input_file = open('sup0.006-set-6.txt', 'r')
#input_file = open('set-5', 'r')
names = []
conseq = []
denom = 2
j=0
file_precision_list=[]
file_recall_list = []
precision_list = []
recall_list = []

# for each rule
for line in input_file:

    names = line.split(' ')
    A = names[0]
    conseq = names[1:]
    gt_names = []
    subsets = []
    #precision_list = list()
    #recall_list = list()
    subsets = list(powerset(conseq))
    list_len = len(subsets)
    subsets_copy = list(subsets)
    # for multiple scans of the transactions to check cur subset
    precision = 0
    recall = 0


    for i in range(list_len):
        max_len_itemset = max(subsets_copy, key = len)
        subsets_copy.remove(max_len_itemset)

        flag = 0

        gt = open('old_ground_truth.txt', 'r')
        for gt_line in gt:
            gt_names = gt_line.split(' ')
            gt_A = gt_names[0]
            gt_conseq = gt_names[1:]

            if A == gt_A:   # If A of rule matches, check for other elements

                cur_itemset_len = len(max_len_itemset)
                match_len = len(set(max_len_itemset).intersection(set(gt_conseq)))
                conseq_len = len(gt_conseq)

                if conseq_len == 0:
                    recall = 0.0
                else:
                    recall = float(float(match_len) / float(conseq_len))
                if cur_itemset_len == 0:
                    precision = 0.0
                else:
                    precision = float(float(match_len) / float(cur_itemset_len))

                recall_list.append(recall)
                precision_list.append(precision)
        gt.close()
    # Sort the lists
    sorted_precision, sorted_recall = (list(x) for x in zip(*sorted(zip(precision_list, recall_list), key=lambda pair: pair[0])))

    file_precision_list.append(sorted_precision[-1])
    file_recall_list.append(sorted_recall[-1])
    del precision_list[:]
    del recall_list[:]



plt.plot( file_recall_list, file_precision_list)
plt.xlabel('Recall')
plt.ylabel('Precision')
plt.title('Precision recall graph for min support 0.006 itemset size 6')
plt.show()


input_file.close()
gt.close()
