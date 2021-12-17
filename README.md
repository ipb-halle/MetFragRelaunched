[![Build Status](https://travis-ci.org/ipb-halle/MetFragRelaunched.svg?branch=master)](https://travis-ci.org/ipb-halle/MetFragRelaunched)
[![install with bioconda](https://img.shields.io/badge/install%20with-bioconda-brightgreen.svg?style=flat-square)](http://bioconda.github.io/recipes/metfrag/README.html)

MetFrag relaunched
==================

##### Requirements
- Java >= 1.6
- Apache Maven ≥ 3.0.4
##### Requirements tested
- Java = openjdk11
- Apache Maven = 3.8

##### Sources
- download sources by cloning git repository<br>
```bash
git clone https://github.com/ipb-halle/MetFragRelaunched.git
```

MetFrag Webapp container
------------------------

This container packages the MetFrag (https://github.com/ipb-halle/MetFragRelaunched) webapp and some text databases based on the latest official tomcat docker container.

##### Following options are supported:
* environment variable JAVA_OPTS="-Xmx?g -Xms?g" to adjust jvm memory
* environment variable WEBPREFIX to adjust the web app name
* automatic use of a metfrag settings file if provided at /resources/settings.properties

##### Examples:
Run MetFrag at http://localhost:8888/MetFragWeb
```
docker run -it --rm -p 8888:8080 ipb-halle/metfragweb
```
Run Metfrag at http://localhost:8888/mymetfrag with 4GB JVM size using a settings file from `./settings.properties`
```
docker run -it --rm -e  JAVA_OPTS="-Xmx4g -Xms4g" -e WEBPREFIX=mymetfrag -v $(pwd)/settings.properties:/resources/settings.properties -p 8888:8080 ipbhalle/metfragweb
```
##### Container repositories
See https://hub.docker.com/r/ipbhalle/metfragweb for automatic builds.



MetFragLib
----------

##### Build
```bash
mvn clean install -pl MetFragLib -am
```

- skipping test during build by<br>
```bash
mvn clean install -pl MetFragLib -am -DskipTests
```

MetFragCommandLine
------------------

##### Basics
- MetFrag commandline version depends on MetFragLib
- for a short tutorial visit http://ipb-halle.github.io/MetFrag/projects/metfragcl/

##### Build
```bash
mvn clean install -pl MetFragCommandLine -am
```

- skipping test during build by<br>
```bash
mvn clean install -pl MetFragCommandLine -am -DskipTests
```

MetFragR
--------

##### Basics
- MetFrag R package depends on MetFragLib<br>
- for a short tutorial visit http://ipb-halle.github.io/MetFrag/projects/metfragr/

##### Build
```bash
mvn clean install -pl MetFragR -am
```

- skipping test during build by<br>
```bash
mvn clean install -pl MetFragR -am -DskipTests
```

- after the successful build the MetFragR java library is located in MetFragR/rpackage/metfRag/inst/java/ 
- point to the directory MetFragR/rpackage/ to perform checks and the build of the R package<br>
```bash
R CMD check metfRag
R CMD build metfRag
```

- after the successful build the R package can be installed in R<br>
```R
install.packages("metfRag",repos=NULL,type="source")
library(metfRag)
```

- all earlier steps can be skipped by installing the pre-compiled R package from GitHub<br>
```R
library(devtools)
install_github("ipb-halle/MetFragR/metfRag")
```

MetFragWeb
----------

##### Basics
- MetFrag web version depends on MetFragLib. Runs with Java Server Faces 2.2 and PrimeFaces 5.3 and brings its own Tomcat web server<br>

##### Configuration
- rename file settings.properties.template in MetFragWeb/src/main/webapp/resources to settings.properties and set necessary parameters<br>
```bash
## define chemspider token to query ChemSpider database
ChemSpiderToken = ...

## if MetFragWeb host is connected via proxy to the internet provide proxy settings for 
## different web services
MoNAProxyServer = ...
MoNAProxyPort = ...

KeggProxyServer = ...
KeggProxyPort = ...

MetaCycProxyServer = ...
MetaCycProxyPort = ...

## to speed up database queries you can set up local repositories by setting up MySQL or 
## PostgreSQL databases
LocalPubChemDatabase = ...
LocalPubChemDatabaseCompoundsTable = ...
LocalPubChemDatabasePortNumber = ...
LocalPubChemDatabaseServerIp = ...
LocalPubChemDatabaseMassColumn = ...
LocalPubChemDatabaseFormulaColumn = ...
LocalPubChemDatabaseInChIColumn = ...
LocalPubChemDatabaseInChIKey1Column = ...
LocalPubChemDatabaseInChIKey2Column = ...
LocalPubChemDatabaseCidColumn = ...
LocalPubChemDatabaseSmilesColumn = ...
LocalPubChemDatabaseUser = ...
LocalPubChemDatabasePassword = ...
LocalPubChemDatabaseCompoundNameColumn = ...
```

##### Build and Run
```bash
mvn clean install -pl MetFragLib
mvn clean install org.codehaus.cargo:cargo-maven3-plugin:run -pl MetFragWeb
```

- skipping test during build by<br>
```bash
mvn clean install -pl MetFragWeb -DskipTests
mvn clean install org.codehaus.cargo:cargo-maven3-plugin:run -pl MetFragWeb
```

- after the successful build Tomcat web server runs on port 8080<br>
- MetFragWeb can be accessed via pointing to http://localhost:8080/index.xhtml in the web browser<br>

##### Build
- if you just want to build the war file to transfer it to another Tomcat instance, run:

```bash
mvn clean package -pl MetFragWeb -am
```

- after the successful build the war file is located in MetFragWeb/target/MetFragWeb.war

Additionals
-----------

##### Run main methods 
```bash
mvn exec:java -Dexec.mainClass="java_class_path" -Dexec.args="arg0 arg1 arg2 ..."  
```

References
----------

Ruttkies C., Schymanski E.L. et al, MetFrag relaunched: incorporating strategies beyond in silico fragmentation. Journal of Cheminformatics, 2016, 8:3. http://jcheminf.springeropen.com/articles/10.1186/s13321-016-0115-9
