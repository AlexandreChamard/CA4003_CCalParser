# CCalParser
CCal parser with JavaCC

USAGE:
	java CcalAstGenerator [-h | [inputfile.ccal] [--tac | --debug [Program | Condition | Expression | Identifier]]]

  -h --help:	show help.
  --tac:	show the Three Adress Code representation of the program.
  --debug:	parse only the given type.

## What it does

Program generates AST on success and an error if not.  
If no file is given, the prompt are set as the current input.  
So "java CcalAstGenerator < test.txt" equals "java CcalAstGenerator test.txt"  
Performs an lexical, syntax and semantic analysis.  
The program checks:  
- every identifier is declared within scope before its is used.  
- no identifier is declared more than once in the same scope.  
- no identifier is declared as the same as function declaration.  
- the left-hand side of an assignment have the correct type.  
- the arguments of an arithmetic operator are integer variables or integer constants.  
- the arguments of a boolean operator are boolean variables or boolean constants.  
- every function call have the correct number of arguments.  

## Script files:

compile.sh:  
  compile source files  
test.sh:  
  run all tests (37)  
clear.sh:  
  clear the repository (remove all .class and .java except 3 java sources files)  
  based on git and gitignore files (does not success if not find)  