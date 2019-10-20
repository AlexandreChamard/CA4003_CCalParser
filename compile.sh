#!/bin/bash

sourceFile="CcalParser.jj"

javacc "${sourceFile}" && javac *.java -Xlint:unchecked