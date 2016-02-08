#!/bin/bash

mkdir -p instrumented
java -cp .:asm-5.0.3.jar Instrument $1 instrumented/$1

