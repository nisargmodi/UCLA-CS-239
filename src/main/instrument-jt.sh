#!/bin/bash

CCT=$(pwd)
CCT="$CCT/CCT"

export CLASSPATH=$CCT:$CCT/asm-5.0.3.jar:.
echo $CLASSPATH

cd joda-time/target/classes
mkdir -p instrumented

for f in $(find org -name '*class')
do
  mkdir -p instrumented/$(dirname $f)
  java -cp $CLASSPATH Instrument $f instrumented/$f
done	
