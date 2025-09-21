# FrAngel benchmarks for SObEq

This repo contains setup code for FrAngel that converts SObEq benchmarks into FrAngel ones,
and uses a custom grammar mirroring SObEq's to run FrAngel on those benchmarks.

To set up this repo, you need to initialize the FrAngel submodule and build it:
```sh
git submodule update --init --recursive;
cd lib/FrAngel/;
ant frangel-jar;
cd ../../;
```

To compile, you need to run the following code:
```sh
rm -rf out;
mkdir -p "./out/production/frangel_benchmarks";
javac -cp "lib/*:lib/FrAngel/lib/*:lib/FrAngel/frangel.jar" -d "out/production/frangel_benchmarks" $(find src/main/java/ -name *.java)
```

And to run:
```sh
java -cp "./out/production/frangel_benchmarks:lib/*:lib/FrAngel/frangel.jar:lib/FrAngel/lib/*" Main $PATH_TO_SOBEQ_BENCHMARKS
```
