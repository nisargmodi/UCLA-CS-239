#!/bin/bash

main=$(pwd)
resources="$main/resources"

export CLASSPATH=$resources:$resources/asm-5.0.4.jar:.
echo $CLASSPATH

cd joda-time/target/classes
mkdir -p instrumented

for f in $(find org -name '*class')
do
  mkdir -p instrumented/$(dirname $f)
  java -cp $CLASSPATH Instrument $f instrumented/$f
done	
