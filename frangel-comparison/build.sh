#!/usr/bin/env bash

SCRIPT_DIR=$(dirname "$0")
cd $SCRIPT_DIR

if [ -d "lib/FrAngel/" ]; then
    git submodule update --init --recursive;
    cd lib/FrAngel/;
    ant frangel-jar;
    cd ../../
fi

rm -rf out;
mkdir -p "./out/production/frangel_benchmarks";
javac -cp "lib/*:lib/FrAngel/lib/*:lib/FrAngel/frangel.jar" -d "out/production/frangel_benchmarks" $(find src/main/java/ -name *.java)

cd -
