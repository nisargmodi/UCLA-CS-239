#!/bin/bash

find . -name '*.java' | xargs wc -l | grep "total" | sed 's/total//g'