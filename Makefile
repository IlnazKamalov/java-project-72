.DEFAULT_GOAL := build-run

clean:
	make -C app clean

build:
	make -C app clean build

install:
	make -C app clean install

lint:
	make -C app checkstyleMain checkstyleTest

report:
	make -C app report

.PHONY: build
