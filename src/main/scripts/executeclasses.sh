#!/bin/bash

alldirs="org/jsoup"
#echo "root dir is"
#echo "$alldirs"

for dir in "$alldirs"/*
do
#	echo $dir
	for file in "$dir"/*
	do
		echo $file
		temp=$(echo "$file" | rev |  cut -c 7- | rev)
		temp=$(echo $temp | tr "/" .)
		echo "temp is"
		echo $temp
		java -cp /home/ashish/Desktop/CS239/junit-4.12.jar:. org.junit.runner.JUnitCore $temp >> op
	done
done

