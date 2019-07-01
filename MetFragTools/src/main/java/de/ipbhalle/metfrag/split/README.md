splitPFAS tool
==============

#### Sources
- download sources by cloning git repository<br>
```bash
git clone https://github.com/ipb-halle/MetFragRelaunched.git
```

#### MetFragLib

##### Build
```bash
mvn clean install -pl MetFragLib -am
```

- skipping test during build by<br>
```bash
mvn clean install -pl MetFragLib -am -DskipTests
```

#### MetFragTools

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

#### Parameters

smiles    	
 - SMILES of input PFAS

smartspath	
 - file containing SMARTS (one per line)
 - for empty SMARTS just include empty line
 - order marks priotity
 - if not given, default "" is used

image     	
 - create image of bonds broken (default 'no')

eccs		
 - SMARTS to find carbon of the end of the PFAS chain
 - (eccs: end chain carbon SMARTS)
 - (default: FC(F)([C,F])[!$(C(F)(F));!$(F)])

df     		
 - debug folder where structure images are written
 - used for debugging (doesn't effect 'image' parameter)

#### How it works
##### 01 - Find carbon(s) at the end of PFAS chain
- find atoms in the given PFAS ('smiles') matching with end chain carbon SMARTS ('eccs')
* filter matches that contain a carbon which 
** has at least two fluorine atoms connected
** is connected to only one other carbon which is connected to fluorines
* this carbon is considered as a PFAS end chain carbon

##### 02 - Find bonds to split by given SMARTS ('smartspath')
- if 'smartspath' is given each smarts is used to find matches in the given PFAS ('smiles')
- get those end chain carbon atoms from Step-01 which are connected to a carbon of a match found in Step-02
- the bond between this end chain carbon atom and the connected atom is marked for splitting

#### 03 - Splitting marked bonds
-  marked bonds are split 
