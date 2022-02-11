FROM maven:3-jdk-11 AS builder

COPY MetFragLib/ /MetFragRelaunched/MetFragLib/
COPY MetFragCommandLine/ /MetFragRelaunched/MetFragCommandLine/
COPY MetFragR/ /MetFragRelaunched/MetFragR/
COPY MetFragTools/ /MetFragRelaunched/MetFragTools/
COPY MetFragRest/ /MetFragRelaunched/MetFragRest/
COPY MetFragWeb/ /MetFragRelaunched/MetFragWeb/
COPY pom.xml /MetFragRelaunched/

RUN printf '# local database file folder \n\
LocalDatabasesFolderForWeb = /vol/file_databases' > /MetFragRelaunched/MetFragWeb/src/main/webapp/resources/settings.properties

RUN mvn -f MetFragRelaunched clean package -pl MetFragLib -pl MetFragWeb -am -DskipTests


FROM tomee:8

RUN wget -q -O- https://msbi.ipb-halle.de/~sneumann/file_databases.tgz | tar -C / -xzf -
RUN cd /vol/file_databases; \
        wget -q https://zenodo.org/record/3375500/files/HMDB4_23Aug19.csv; \
        wget -q https://zenodo.org/record/3403530/files/WormJam_10Sept19.csv; \
        wget -q https://zenodo.org/record/3434579/files/YMDB2_17Sept2019.csv; \
        wget -q https://zenodo.org/record/3735703/files/EColiMDB_11Nov2019.csv; \
        wget -q https://zenodo.org/record/3541624/files/Zebrafish_13Nov2019_Beta.csv; \
        wget -q https://zenodo.org/record/3472781/files/CompTox_07March19_WWMetaData.csv; \
        wget -q https://zenodo.org/record/3520106/files/NPAtlas_Aug2019.csv; \
        wget -q https://zenodo.org/record/3548461/files/NORMANSusDat_20Nov2019.csv
RUN cd /vol/file_databases; \
        wget -q https://zenodo.org/record/3547718/files/COCONUT4MetFrag.csv; \
        wget -q https://zenodo.org/record/4562688/files/CyanoMetDB_MetFrag_Feb2021.csv; \
        wget -q https://zenodo.org/record/4432124/files/PubChemLite_01Jan2021_exposomics.csv; \
        wget -q https://zenodo.org/record/4456208/files/PubChemLite_01Jan2021_exposomics_CCSbase.csv; \
        wget -q https://zenodo.org/record/3957497/files/HBM4EU_CECscreen_MF_1Jul2020.csv; \
        wget -q https://zenodo.org/record/3957497/files/HBM4EU_CECscreen_MF_1Jul2020_plusTPs.csv; \
        wget -q https://zenodo.org/record/3564602/files/BloodExposomeDB_03Dec2019.csv

COPY --from=builder /MetFragRelaunched/MetFragWeb/target/MetFragWeb.war /usr/local/tomee/webapps/
RUN printf '#!/bin/sh \n\
if [ -f "/resources/settings.properties" ] \n\
then \n\
	jar uvf /usr/local/tomee/webapps/MetFragWeb.war /resources/settings.properties \n\
fi \n\
if ! [ -z ${WEBPREFIX} ] \n\
then \n\
	mv /usr/local/tomee/webapps/MetFragWeb.war /usr/local/tomee/webapps/${WEBPREFIX}.war \n\
fi \n\
catalina.sh run' > /start.sh

CMD [ "sh", "/start.sh" ]
