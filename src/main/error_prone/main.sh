#!/bin/bash

./resources/instrument-compile.sh
# cd joda-time
# mvn clean 
# mvn compile
# mvn test
# cd ..
./instrument-driver.sh
./runInstrumented.sh
#resultsJT=results/joda-time
#./statsPassFail.sh $resultsJT > $resultsJT/statsPassFail.csv