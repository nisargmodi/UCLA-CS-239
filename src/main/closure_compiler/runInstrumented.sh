#!/bin/bash

outD="$(pwd)/results/closure-compiler"

mkdir -p $outD

export experiment_root=$(pwd)

export CLASSPATH=$experiment_root/closure-compiler/build/instrumented:$experiment_root/closure-compiler/build/test:$experiment_root/resources/junit.jar:$experiment_root/closure-compiler/lib/:.

function go {
  local cls=$1
  local n=$2
  #java -cp $CLASSPATH org.junit.runner.JUnitCore $cls >> $outD/out/"output.txt"
  java -cp $CLASSPATH org.junit.runner.JUnitCore $cls 2> >(grep '^CALL\|^RETURN' >> $outD/"output.txt")
}

cd closure-compiler/build/test
testNumber=1
for f in $(find com -name '*class')
do
  testName=$(echo $f | sed 's/\//./g')
  testName=$(echo $testName | sed 's/.class//g')
  echo ">>>running test $testNumber"
  go $testName $testNumber
  testNumber=$(expr $testNumber + 1)
done