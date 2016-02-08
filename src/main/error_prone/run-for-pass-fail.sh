#!/bin/bash

outD="$(pwd)/reports/error-prone"
cct1="python $(pwd)/CCT/CCT.py"

mkdir -p $outD

export experiment_root=$(pwd)
export CLASSPATH=$experiment_root/error-prone/core/target/classes/instrumented:$experiment_root/error-prone/core/target/test-classes:$experiment_root/resources/junit.jar:.

function go {
  local cls=$1
  local n=$2
  java -cp $CLASSPATH junit.textui.TestRunner $cls 2> >(grep '^CALL\|^RETURN' | $cct1 $outD/$cls.cct) > $outD/$cls.out
}

cd error-prone/core/target/test-classes
testNumber=1

for f in $(find com -name '*class')
do
  testName=$(echo $f | sed 's/\//./g')
  testName=$(echo $testName | sed 's/.class//g')

  echo ">>>running test $testNumber"
  go $testName $testNumber
  testNumber=$(expr $testNumber + 1)
done