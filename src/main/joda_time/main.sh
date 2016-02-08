#!/bin/bash

./resources/instrument-compile.sh
# cd joda-time
# mvn clean 
# mvn compile
# mvn test
# cd ..
./instrument-driver.sh
./runInstrumented.sh

resultsJT=reports/joda-time
./run-for-pass-fail.sh
./statsPassFail.sh $resultsJT > $resultsJT/statsPassFail.csv