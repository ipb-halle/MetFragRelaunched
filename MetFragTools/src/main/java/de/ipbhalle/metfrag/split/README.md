splitPFAS tool
==============

##### Sources
- download sources by cloning git repository<br>
```bash
git clone https://github.com/ipb-halle/MetFragRelaunched.git
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

MetFragTools
------------

- build MetFragTools jar containing the executable JAR running the splitPFAS tool

##### Build
```bash
mvn clean install -pl MetFragTools -am
```

##### Run

- you can execute the splitPFAS tool via running the created JAR file

```bash
java -jar MetFragTools/target/MetFragTools-2.4.5-jar-with-dependencies.jar
```


