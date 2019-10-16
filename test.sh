
call="java SyntaxChecker"

error="error"

n=0
i=0

function test() {
    n=$((n + 1))
    echo 
    echo "============================"
    echo 
    echo $2'test'${n}' "'$1'":'
    ${call} "$1" > ${file}
    if [[ $? == 0 ]];then
        if [[ $2 == "error" ]];then
            i=$((i + 1))
        fi
    else
        if [[ $2 != "error" ]];then
            i=$((i + 1))
        fi
    fi
    cat ${file}
}

javacc sample.jj && javac *.java || exit 1

file=tmpfile

##
## TESTS
##

test "0"            # numeric 0
test "-0"           # numeric negative 0
test "42"           # numeric
test "-42"          # numeric negative
test "042" error    # numeric error
test "-042" error   # numeric error negative

test "a"            # identifier
test "a_42"         # identifier
test "A_42"         # identifier upper case
test "while" error  # identifier invalid: keyword
test "_a" error     # identifier

test "true || true
// blabla"            # identifier
test "~true || true"  # identifier
test "true ||" error  # identifier

test '1 +   2'      # identifier
test '1 - 2'        # identifier
test '1 -2'         # identifier





rm ${file}

echo 
echo "============================"
echo 
echo ${n}" tests: "${i}" errors found."

exit ${i}