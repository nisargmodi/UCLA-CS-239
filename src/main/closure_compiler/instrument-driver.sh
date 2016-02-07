#!/bin/bash

main=$(pwd)
resources="$main/resources"

export CLASSPATH=$resources:$resources/asm-5.0.4.jar:.
echo $CLASSPATH

cd closure-compiler/build
mkdir -p instrumented

for f in $(find classes -name '*class')
do
  mkdir -p ../instrumented/$(dirname $f)
  java -cp $CLASSPATH Instrument $f ../instrumented/$f
done	
