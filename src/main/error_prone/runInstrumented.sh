#!/bin/bash

outD="$(pwd)/results/joda-time"

mkdir -p $outD

export experiment_root=$(pwd)

cp -R $experiment_root/joda-time/target/classes/META-INF $experiment_root/joda-time/target/classes/instrumented/META-INF

export CLASSPATH=$experiment_root/joda-time/target/classes/instrumented:$experiment_root/joda-time/target/test-classes:$experiment_root/resources/junit.jar:.

function go {
  local cls=$1
  local n=$2
  #java -cp $CLASSPATH org.junit.runner.JUnitCore $cls >> $outD/out/"output.txt"
  java -cp $CLASSPATH org.junit.runner.JUnitCore $cls 2> >(grep '^CALL\|^RETURN' >> $outD/"output.txt")
}

cd joda-time/target/test-classes/
testNumber=1
for f in $(find org -name '*class')
do
  testName=$(echo $f | sed 's/\//./g')
  testName=$(echo $testName | sed 's/.class//g')
  echo ">>>running test $testNumber"
  go $testName $testNumber
  testNumber=$(expr $testNumber + 1)
done