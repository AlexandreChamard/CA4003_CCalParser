#!/bin/bash

sourceFile="CcalAstGenerator.jj"

javacc "${sourceFile}" && javac *.java -Xlint:unchecked