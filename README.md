# Replication package for the paper "Bottom-up Synthesis of Memory Mutations with Separation Logic" (ECOOP'25)

DOI: https://doi.org/10.4230/LIPIcs.ECOOP.2025.7

This artifact contains the code for our implementation of SObEq, all benchmarks used in our evaluation, all parallel versions used in our evaluation, and the grammer for using FrAngel as a baseline.

## requirements

*System requirements*: `scala` 2.13 and `sbt` 1.6 or above. Running the comparison to FrAngel requires `ant`.

*Hardware requirements*: Running SObEq requires 128GB of RAM.

*Time requirements*: The full experiments take ~160 hours. They are broken up into six scripts that require between 10 and 60 hours.

## building SObEq:

In each of the directories run `sbt assembly`:
- sobeq-main/
- classical-enumeration/
- concrete-states/

The experiment scripts will run assemble the code before running.

To run FrAngel, follow the instructions in the inner readme in directory `frangel-comparison/` before executing `./run_frangel.sh`

## experiment scripts

Scripts `./run_*.sh` run experiments used to evaluate SObEq. Each script creates a new directory with a timestamp with individual output files for each benchmark and with a `results.csv` file to summarize the run.

## benchmarks

Benchmarks are included in the `benchmarks/` directory, divided into the _May_ and _Must_ benchmark sets, then further divided by origin. Notice that the paper's _Pure_ benchmark set is constructed on the fly during the run of the `./run_immut.sh` script.

# Docker container

This code is available as a docker container at https://zenodo.org/records/15300517.

The docker file used to build the artifact is included.

