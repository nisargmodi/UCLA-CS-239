#!/bin/bash

./resources/instrument-compile.sh
# cd closure-compiler
# mvn clean 
# mvn compile
# mvn test
# cd ..
./instrument-driver.sh
./runInstrumented.sh
#resultsJT=results/closure-compiler
#./statsPassFail.sh $resultsJT > $resultsJT/statsPassFail.csv