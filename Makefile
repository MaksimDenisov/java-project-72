.DEFAULT_GOAL := build-run

build:
	make -C app clean build
install:
	make -C app  install
run-dist:
	make -C app run-dist
report:
	make -C app  report

build-run: build install run-dist

.PHONY: build