#!/bin/bash

java -jar -Dlang="en" -DauthToken="token1234" -DinputMethod="mouse_touch" -DttsUrl="http://localhost/action/" -Djdk.gtk.version=2 -Dexec.mainClass=org.scify.memori.ApplicationLauncher memori-1.0-SNAPSHOT-jar-with-dependencies.jar
