
call="java SyntaxChecker"
error="error"
testsdir="tests"

javacc sample.jj && javac *.java || exit 1

file=tmpfile

i=0

function test() {
    local filename="${testsdir}/test${n}/input.txt"
    local testfile="${testsdir}/test${n}/output.txt"

    echo 
    echo "============================"
    echo 
    echo "test${n}: " ${filename}
    ${call} ${filename} > ${file} && diff -w ${file} ${testfile}
    if [[ ${?} != 0 && "$(< ${testfile})" != "error" ]];then
        i=$((i + 1))
        echo -e "\e[31mERROR\e[0m"
    fi
    cat ${file}

}

for n in $(seq 1 $(find tests/ -maxdepth 1 -mindepth 1 -type d | wc -l)); do
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