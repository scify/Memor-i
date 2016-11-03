#!/usr/bin/env bash

mv target/memori-1.0-SNAPSHOT-jar-with-dependencies.jar target/memori-testing.jar

scp target/memori-testing.jar  scifyuser@192.168.1.17:/home/scifyuser/