#!/bin/bash

JJTsourceFile="CcalParser.jjt"
JJsourceFile="CcalParser.jj"

jjtree "${JJTsourceFile}" && javacc "${JJsourceFile}" && javac *.java -Xlint:unchecked