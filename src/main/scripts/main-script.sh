#!/bin/bash

curr_path=$(pwd)
echo $curr_path
base_path=$curr_path/..
echo $base_path

path_to_tests=$base_path/resources/jsoup/target/classes

CLASSPATH=.:$base_path/resources/asm-all-5.0.4.jar:$base_path/resources/junit.jar:$base_path/java/instrumented/jsoup/target/classes
javac -cp $CLASSPATH $base_path/java/core-utils/Instrument.java

echo "classpath is:"$CLASSPATH

cd $path_to_tests

echo $(pwd)

for f in $(find org -name '*class')
do
  mkdir -p $base_path/java/instrumented/jsoup/target/classes/$(dirname $f)
  cd $base_path/java/core-utils/
  java -cp $CLASSPATH Instrument $path_to_tests/$f $base_path/java/instrumented/jsoup/target/classes/$f
  cd $path_to_tests
done

cp -R $base_path/resources/jsoup/target/classes/META-INF $base_path/java/instrumented/jsoup/target/classes/META-INF
cp -R $base_path/resources/jsoup/target/test-classes $base_path/java/instrumented/jsoup/target/test-classes

path_to_instrumented_tests=$base_path/resources/jsoup/target/test-classes

cd $path_to_instrumented_tests
echo $(pwd)

CLASSPATH=.:$base_path/resources/asm-all-5.0.4.jar:$base_path/resources/junit.jar:$base_path/java/instrumented/jsoup/target/classes:$base_path/resources/jsoup/target/test-classes

for f in $(find org -name '*class')
do
  arrIN=(${f//./ })
  echo $arrIN
  java -cp $CLASSPATH org.junit.runner.JUnitCore $arrIN
done