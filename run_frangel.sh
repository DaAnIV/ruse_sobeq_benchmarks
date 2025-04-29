#!/usr/bin/env bash

# MIN_HEAP=$(grep MemTotal /proc/meminfo | awk '{printf "%.f\n", $2/1024/1024*50/100 }')G
MAX_HEAP=$(grep MemTotal /proc/meminfo | awk '{printf "%.f\n", $2/1024/1024*90/100 }')G

cd frangel-comparison
java -cp "./out/production/frangel_benchmarks:lib/*:lib/FrAngel/frangel.jar:lib/FrAngel/lib/*" Main ../benchmarks
