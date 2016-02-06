#!/bin/bash

outD="$(pwd)/reports/joda-time"

mkdir -p $outD
mkdir -p $outD/out

export experiment_root=$(pwd)

cp -R $experiment_root/joda-time/target/classes/META-INF $experiment_root/joda-time/target/classes/instrumented/META-INF

export CLASSPATH=$experiment_root/joda-time/target/classes/instrumented:$experiment_root/joda-time/target/test-classes:$experiment_root/CCT/junit.jar:.

function go {
  local cls=$1
  local n=$2
  #java -cp $CLASSPATH org.junit.runner.JUnitCore $cls >> $outD/out/"output.txt"
  java -cp $CLASSPATH org.junit.runner.JUnitCore $cls 2> >(grep '^CALL\|^RETURN' >> $outD/out/"output.txt")
}

cd joda-time/target/test-classes/
testNumber=1
count=0
for f in $(find org -name '*class')
do
  $count+=1
  testName=$(echo $f | sed 's/\//./g')
  testName=$(echo $testName | sed 's/.class//g')
  echo ">>>running test $testNumber"
  go $testName $testNumber
  testNumber=$(expr $testNumber + 1)
  if $count>=30
  then
     break
  fi
done