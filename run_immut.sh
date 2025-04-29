#!/usr/bin/env bash

# MIN_HEAP=$(grep MemTotal /proc/meminfo | awk '{printf "%.f\n", $2/1024/1024*50/100 }')G
MAX_HEAP=$(grep MemTotal /proc/meminfo | awk '{printf "%.f\n", $2/1024/1024*90/100 }')G

CONFIG='{
    "benchmark": "../benchmarks/",
    "timeout": 3600,
    "outputDirName": "immut"
}';

cd sobeq-main/
echo -n "Compiling... ";
if BUILD_RESULT=$(sbt assembly); then
    echo "OK";
    java -Xmx$MAX_HEAP -Dfile.encoding=UTF-8 \
      -cp target/scala-2.13/sobeq-assembly-0.1.jar \
      ImmutBenchmarkRunner \
      "$CONFIG";
else
    echo "FAILED";
    echo "$BUILD_RESULT";
fi;
