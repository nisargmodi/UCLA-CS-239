#!/bin/bash

outD="$(pwd)/results/error-prone"

mkdir -p $outD

export experiment_root=$(pwd)

export CLASSPATH=$experiment_root/error-prone/core/target/classes/instrumented:$experiment_root/error-prone/core/target/test-classes:$experiment_root/resources/junit.jar:.

function go {
  local cls=$1
  local n=$2
  #java -cp $CLASSPATH org.junit.runner.JUnitCore $cls >> $outD/out/"output.txt"
  java -cp $CLASSPATH org.junit.runner.JUnitCore $cls 2> >(grep '^CALL\|^RETURN' >> $outD/"output.txt")
}

cd error-prone/core/target/test-classes/
testNumber=1
for f in $(find com -name '*class')
do
  testName=$(echo $f | sed 's/\//./g')
  testName=$(echo $testName | sed 's/.class//g')
  echo ">>>running test $testNumber"
  go $testName $testNumber
  testNumber=$(expr $testNumber + 1)
done