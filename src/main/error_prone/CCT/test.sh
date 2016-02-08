#!/bin/bash

./build.sh

javac Test.java
./instrument.sh Test.class
java -cp instrumented Test

