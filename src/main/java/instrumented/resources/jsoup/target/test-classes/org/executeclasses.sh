#!/bin/bash

for dir in /jsoup/*
do
echo $dir
	echo $dir
	for file in "$dir"/*
	do
	echo $file
	echo temp = $(echo "$file" | rev |  cut -c 7- | rev)
	echo $temp
		java $temp >> output.txt
	done
done

