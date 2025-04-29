#!/usr/bin/env bash

# MIN_HEAP=$(grep MemTotal /proc/meminfo | awk '{printf "%.f\n", $2/1024/1024*50/100 }')G
MAX_HEAP=$(grep MemTotal /proc/meminfo | awk '{printf "%.f\n", $2/1024/1024*90/100 }')G

CONFIG='{
    "benchmark": "'$1'",
    "timeout": 3600,
    "outputDirName": "results"
}';

cd sobeq-main
echo -n "Compiling... ";
if BUILD_RESULT=$(sbt assembly); then
    echo "OK";
    java -jar -Xmx$MAX_HEAP -Dfile.encoding=UTF-8 \
      target/scala-2.13/sobeq-assembly-0.1.jar \
      "$CONFIG";
else
    echo "FAILED";
    echo "$BUILD_RESULT";
fi;
