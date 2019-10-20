#!/bin/bash
sourceFile="CcalParser.jj"
programName="SyntaxChecker"

call="java ${programName}"
error="error"
testsdir="tests"

javacc "${sourceFile}" && javac *.java || exit 1

file=tmpfile

i=0
n=0

function test() {
    local filename="${f}/input.txt"
    local testfile="${f}/output.txt"

    n=$((n + 1))

    echo 
    echo "============================"
    echo 
    echo "${f}: ${call} ${filename}"
    ${call} "${filename}" > "${file}"
    if [[ ${?} -ne 0 ]];then
        if [[ "$(< ${testfile})" != "error" ]]; then
            i=$((i + 1))
            echo -e "\e[31mERROR\e[0m"
        fi
    else
        diff -w "${file}" "${testfile}"
        if [[ ${?} -ne 0 ]];then
            i=$((i + 1))
            echo -e "\e[31mERROR\e[0m"
        fi
    fi
    cat "${file}"
}

for f in $(find ${testsdir}/ -maxdepth 1 -mindepth 1 -type d | sort);do
    test
done

rm "${file}"

echo 
echo "============================"
echo 
echo "${n} tests: ${i} errors found."

exit ${i}