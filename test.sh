
sourceFile="ccalParser.jj"
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
    echo "${f}: " ${filename}
    ${call} ${filename} > ${file} && diff -w ${file} ${testfile}
    if [[ ${?} != 0 && "$(< ${testfile})" != "error" ]];then
        i=$((i + 1))
        echo -e "\e[31mERROR\e[0m"
    fi
    cat ${file}

}

for f in $(find ${testsdir}/ -maxdepth 1 -mindepth 1 -type d | sort);do
    test
done

# test "true || true
# // blabla"            # 12 inline comment
# test "~true || true"  # 13 muidentifier
# test "true ||" error  # identifier

# test '1 +   2'      # identifier
# test '1 - 2'        # identifier
# test '1 -2'         # identifier





rm ${file}

echo 
echo "============================"
echo 
echo ${n}" tests: "${i}" errors found."

exit ${i}