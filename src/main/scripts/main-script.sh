#!/bin/bash

curr_path=$(pwd)

export CLASSPATH=.:$curr_path/../resources/asm-all-5.0.4.jar:.
javac ../java/core-utils/Instrument.java

echo "classpath is:"$CLASSPATH

cd ../

for f in $(find resources -name '*class')
do
  mkdir -p java/instrumented/$(dirname $f)
  cd java/core-utils/
  java Instrument $curr_path/../$f $curr_path/../java/instrumented/$f
  cd ../../
done