#!/bin/bash

java -jar -DttsUrl="http://localhost/action/" -Djdk.gtk.version=2 -Dexec.mainClass=org.scify.memori.ApplicationLauncher memori-1.0-SNAPSHOT-jar-with-dependencies.jar
