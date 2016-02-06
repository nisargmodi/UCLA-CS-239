#!/bin/bash

./CCT/build.sh

cd joda-time
mvn clean 
mvn compile
mvn test
cd ..

reportJT=reports/joda-time

./instrument-jt.sh

./run-jt-tests.sh

./passFailReport.sh $reportJT > $reportJT/passFail.csv