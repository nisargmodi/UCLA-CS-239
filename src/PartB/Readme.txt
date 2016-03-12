Steps to follow:

1. Execute gitpymain.py file to generate the commit transactions file and file metrics csv.
2. Execute FPmining.py to generate frequently occuring patterns file and also statistics for
   frequent/nonfrequent patterns.
3. Execute frequent_patterns.py file. It will generate frequent itemsets output files required
   for precision-recall graph creation.
4. Use these files as input to run the prec_recall.py file which will generate precision-recall graphs.