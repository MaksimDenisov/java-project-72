.DEFAULT_GOAL := build-run

build:
	./gradlew clean build
install:
	./gradlew install
run-dist:
	./build/install/app/bin/app
report:
	./gradlew jacocoTestReport

build-run: build install run-dist

.PHONY: build
