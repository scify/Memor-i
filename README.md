
# Memor-i

<p align="center">
<img src="https://raw.githubusercontent.com/scify/Memor-i/master/src/main/resources/img/memori.png" width="400" alt="Gameplay">
</p>
<br>

A Memory card game especially tailored to meet the needs of blind people.
Check out our [YouTube video](https://www.youtube.com/watch?v=M2DqT5e975w)

![Memor-i gameplay](https://raw.githubusercontent.com/scify/Memor-i/master/src/main/resources/img/memori_gameplay.gif)

## Getting Started

Please take a look at the [project roadmap](http://jira.scify.org/secure/RapidBoard.jspa?rapidView=99&projectKey=MEM&view=detail&selectedIssue=MEM-35")
To see currently open tasks, discussed ideas and submitted bugs

### Prerequisites

After cloning the code repository, Java and Apache Maven are required to be able to compile it.
Ensure that Java and Maven version installed on your machine by running:

```bash
mvn -v
```

And the result will output the Maven-related information:

```text
Apache Maven 3.0.5

Maven home: /usr/share/maven

Java version: 1.8.0_101, vendor: Oracle Corporation

Java home: /usr/lib/jvm/java-8-oracle/jre
```

#### Choosing the correct Java version

This project is built using Java `1.8`.

If you have multiple versions of Java installed, you need to select the correct one by running:

```bash
sudo update-alternatives --config java
```

and then (if applicable):

```bash
sudo update-alternatives --config javac
```

Many programs, such as Java servers, use the JAVA_HOME environment variable to determine the Java installation location.

So, you need to get the correct Java path, and add it to `~/.bashrc`:

```text
# ~/.bashrc contents:

# export JAVA_HOME=/usr/lib/jvm/jdk-11/

export JAVA_HOME=/usr/lib/jvm/jdk1.8.0_333/

export PATH=$JAVA_HOME/bin:$PATH
```

After updating the `~/.bashrc` file, run the `source` command to activate the changes:

```bash
source ~/.bashrc
```

### Installing

By looking at pom.xml you will see some plugins this project uses. 
Supposing that Maven runs correctly on your machine and you have configured this project as a Maven project, 
these plugins will be automativcally downloaded and installed upon compilation.

After compiling, in order for the standalone .jar file to be built, run these commands:

```
mvn clean

mvn package

mvn assembly:single

mvn dependency:copy-dependencies
```

In order to run the produced `.jar` file, run:

```bash
java -jar -Djdk.gtk.version=2 -Dexec.mainClass=org.scify.memori.ApplicationLauncher target/memori-1.0-SNAPSHOT-jar-with-dependencies.jar
```

These tasks can also be accomplished more easily just by running 

```bash
./build_project.sh
```

which is a UNIX executable file located at the root of the project.

Note: if not executable, you need to make the file executable, by running:

```bash
chmod +x build_project.sh
```

## Built With

* Java - 1.8
* Maven - 3.0.5
* IntelliJ - 16

## Authors

* **SciFY Development team** - *Initial work* - [SciFY](https://github.com/scify)

See also the list of [contributors](https://github.com/scify/Memor-i/graphs/contributors) who participated in this project.

## License

All images and sounds are licenced under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)

Copyright 2016

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Sponsors

* [Crowdfunding campaign](http://www.giveandfund.com/giveandfund/project/games-for-the-blind)

### Memor-i Player VS Player Sponsor
<br>
Η έκδοση Memor-i PvP του έργου Memor-i υλοποιείται με χρηματοδότηση από το <a href="http://www.latsis-foundation.org/">Κοινωφελές Ίδρυμα Ιωάννη Σ. Λάτση.</a>
<br><br><br>
<table>
<tr>
<td>
<a href="https://www.scify.gr/site/en/"><img src="https://www.scify.gr/site/images/scify/scify_logo_108.png" alt="SciFY logo"></a>
</td>
<td>
<a href="https://www.latsis-foundation.org/" title="Ίδρυμα Λάτση" rel="home"><img height="100px" src="https://praksis.gr/cms/files/2019/12/LATSIS_FOUNDATION_LOGO_GR.jpg" alt="Ίδρυμα Λάτση" title="Ίδρυμα Λάτση"></a>
</td>
</tr>
</table>
<br><br>

## Acknowledgments

* [Maven plugin for parsing JSON files](https://mvnrepository.com/artifact/org.json/json)
* <div>Icons made by <a href="https://www.flaticon.com/authors/pixel-buddha" title="Pixel Buddha">Pixel Buddha</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
