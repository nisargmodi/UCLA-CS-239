#!/bin/bash

for out in $1/*.out
do
  nm="$(basename $out | sed 's/\.out$//')"

  grep error $out > /dev/null
  if [ $? -eq 0 ]
  then
    echo $nm,FAIL
  else
    echo $nm,PASS
  fi
  #echo "$nm,$(grep error $out | wc -l)"
done
