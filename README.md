MetFrag relaunched
==================

##### Requirements
- Java >= 1.6
- Apache Maven ≥ 3.0.4

##### Sources
- download sources by cloning git repository<br>
```bash
git clone https://github.com/c-ruttkies/MetFragRelaunched.git
```

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
- for a short tutorial visit http://c-ruttkies.github.io/MetFrag/projects/metfrag22cl/

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
- for a short tutorial visit http://c-ruttkies.github.io/MetFrag/projects/metfragr/

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
install_github("c-ruttkies/MetFragR/metfRag")
```

MetFragWeb
----------

##### Basics
- MetFrag web version depends on MetFragLib. Runs with Java Server Faces 2.2 and PrimeFaces 5.3 and brings its own Tomcat web server<br>

##### Configuration
 - rename file settings.properties.template in MetFragWeb/src/main/webapp/resources to settings.properties and set necessary parameters<br>

 \#\# define chemspider token to query ChemSpider database<br>
 ChemSpiderToken = ...<br>

 \#\# if MetFragWeb host is connected via proxy to the internet provide proxy settings for different web services<br>
 MoNAProxyServer = ...<br>
 MoNAProxyPort = ...<br>

 KeggProxyServer = ...<br>
 KeggProxyPort = ...<br>

 MetaCycProxyServer = ...<br>
 MetaCycProxyPort = ...<br>

 \#\# to speed up database queries you can set up local repositories by setting up MySQL or PostgreSQL databases<br>
 LocalPubChemDatabase = ...<br>
 LocalPubChemDatabaseCompoundsTable = ...<br>
 LocalPubChemDatabasePortNumber = ...<br>
 LocalPubChemDatabaseServerIp = ...<br>
 LocalPubChemDatabaseMassColumn = ...<br>
 LocalPubChemDatabaseFormulaColumn = ...<br>
 LocalPubChemDatabaseInChIColumn = ...<br>
 LocalPubChemDatabaseInChIKey1Column = ...<br>
 LocalPubChemDatabaseInChIKey2Column = ...<br>
 LocalPubChemDatabaseCidColumn = ...<br>
 LocalPubChemDatabaseSmilesColumn = ...<br>
 LocalPubChemDatabaseUser = ...<br>
 LocalPubChemDatabasePassword = ...<br>
 LocalPubChemDatabaseCompoundNameColumn = ...<br>

##### Build and Run
```bash
mvn clean -Dcontainer=tomcat7 tomcat:run-war -pl MetFragWeb -am
```

- skipping test during build by<br>
```bash
mvn clean -Dcontainer=tomcat7 tomcat:run-war -pl MetFragWeb -am -DskipTests
```

- after the successful build Tomcat web server runs on port 8080<br>
- MetFragWeb can be accessed via pointing to http://localhost:8080/MetFragWeb/index.xhtml in the web browser<br>

Additionals
-----------

##### Run main methods 
```bash
mvn exec:java -Dexec.mainClass="java_class_path" -Dexec.args="arg0 arg1 arg2 ..."  
```

References
----------

Ruttkies C., Schymanski E.L. et al, MetFrag relaunched: incorporating strategies beyond in silico fragmentation. Journal of Cheminformatics, 2016, 8:3. http://jcheminf.springeropen.com/articles/10.1186/s13321-016-0115-9
