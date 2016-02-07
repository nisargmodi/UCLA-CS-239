#!/bin/bash

./resources/instrument-compile.sh
# cd error-prone
# mvn clean 
# mvn compile
# mvn test
# cd ..
./instrument-driver.sh
./runInstrumented.sh
#resultsJT=results/error-prone
#./statsPassFail.sh $resultsJT > $resultsJT/statsPassFail.csv