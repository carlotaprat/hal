#!/usr/bin/env bash

EXECUTABLE=bin/hal

color() {
    color=$1
    text=$2
    echo -e "\e[38;05;${color}m${text}\e[0m";
}

# Create executable
echo -n "Making executable..."
make > /dev/null 2>&1
[ $? -eq 0 ] && color 2 "done" || { color 1 "error" && exit 1; }


unittest () {
    infile=$1
    outfile=${infile%.in}.out

    echo -n "Testing ${infile%.in}..."
    $EXECUTABLE < $infile > /tmp/out 2> /dev/null
    diff -q /tmp/out $outfile > /dev/null 2>&1

    [ $? -eq 0 ] && color 2 "ok" || color 1 "failed"
}

for file in $(find tests/ -name "*.in")
do
   unittest $file
done
