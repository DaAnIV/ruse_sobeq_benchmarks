clean:
	sbt clean

test: clean
	sbt test

build:
	sbt assembly

rq1: build
	./scripts/rq1_test_orderings.sh

rq3: build
	./scripts/rq3_test_sideeffects.sh

rq4: build
	./scripts/rq4_test_seen_set.sh

zip:
	zip -r9 benchmark_results.zip *-rq1 *-rq3 *-rq3 *-rq4
