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

RUN mvn -Dhttps.protocols=TLSv1.2 -f MetFragRelaunched clean package -pl MetFragLib -pl MetFragWeb -am -DskipTests


FROM tomee:8

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		zip \
        ; \
	rm -rf /var/lib/apt/lists/*

# RUN wget -q -O- https://msbi.ipb-halle.de/~sneumann/file_databases.tgz | tar -C / -xzf -
RUN mkdir -p /vol/file_databases; cd /vol/file_databases && \
	wget -q https://zenodo.org/records/3548461/files/NORMANSusDat_20Nov2019.csv && \
        touch NORMANSusDat_20Nov2019.csv && \
	wget -q https://zenodo.org/records/3520106/files/NPAtlas_Aug2019.csv && \
        touch NPAtlas_Aug2019.csv && \
        wget -q https://zenodo.org/records/3364464/files/CompTox_07March19_SmokingMetaData.csv && \
        touch CompTox_07March19_SmokingMetaData.csv && \
        wget -q https://zenodo.org/records/6475906/files/CompTox_07March19_SelectMetaData.csv && \
        touch CompTox_07March19_SelectMetaData.csv && \
        wget -q https://zenodo.org/records/3472781/files/CompTox_07March19_WWMetaData.csv && \
        touch CompTox_07March19_WWMetaData.csv && \
        wget -q https://zenodo.org/records/3541624/files/Zebrafish_13Nov2019_Beta.csv && \
        touch Zebrafish_13Nov2019_Beta.csv && \
        wget -q https://zenodo.org/records/3735703/files/EColiMDB_11Nov2019.csv && \
        touch EColiMDB_11Nov2019.csv && \
        wget -q https://zenodo.org/records/3434579/files/YMDB2_17Sept2019.csv && \
        touch YMDB2_17Sept2019.csv && \ 
        wget -q https://zenodo.org/records/3403530/files/WormJam_10Sept19.csv && \
        touch WormJam_10Sept19.csv && \
        wget -q https://zenodo.org/records/3375500/files/HMDB4_23Aug19.csv && \
        touch HMDB4_23Aug19.csv

RUN cd /vol/file_databases && \
	wget -q https://zenodo.org/records/3564602/files/BloodExposomeDB_03Dec2019.csv && \
	touch BloodExposomeDB_03Dec2019.csv && \
	wget -q https://zenodo.org/records/3957497/files/HBM4EU_CECscreen_MF_1Jul2020_plusTPs.csv && \
	touch HBM4EU_CECscreen_MF_1Jul2020_plusTPs.csv && \
	wget -q https://zenodo.org/records/3957497/files/HBM4EU_CECscreen_MF_1Jul2020.csv && \
        touch HBM4EU_CECscreen_MF_1Jul2020.csv && \
	wget -q https://zenodo.org/records/4456208/files/PubChemLite_01Jan2021_exposomics_CCSbase.csv && \
        touch PubChemLite_01Jan2021_exposomics_CCSbase.csv && \
	wget -q https://zenodo.org/records/4432124/files/PubChemLite_01Jan2021_exposomics.csv && \
        touch PubChemLite_01Jan2021_exposomics.csv && \
	wget -q https://zenodo.org/records/6474542/files/OntoChem_PFAS_Patents_20220420.csv && \
        touch OntoChem_PFAS_Patents_20220420.csv && \
	wget -q https://zenodo.org/records/6474542/files/OntoChem_PFAS_CORE_20220420.csv && \
        touch OntoChem_PFAS_CORE_20220420.csv && \
	wget -q https://zenodo.org/records/7922070/files/CyanoMetDB_v02_2023_MetFrag.csv && \
        touch CyanoMetDB_v02_2023_MetFrag.csv && \
	wget -q https://zenodo.org/records/7750267/files/PubChem_OECDPFAS_largerPFASparts_20230319.csv && \
        touch PubChem_OECDPFAS_largerPFASparts_20230319.csv && \
	wget -q https://zenodo.org/records/5336447/files/COCONUT4MetFrag_april.csv && \
        touch COCONUT4MetFrag_april.csv && \
	wget -q https://zenodo.org/records/8144127/files/LIPIDMAPS_20230712.csv && \
        touch LIPIDMAPS_20230712.csv && \
	wget -q https://zenodo.org/records/10202720/files/PubChemLite_exposomics_20231124.csv && \
        touch PubChemLite_exposomics_20231124.csv

COPY --from=builder /MetFragRelaunched/MetFragWeb/target/MetFragWeb.war /usr/local/tomee/webapps/
RUN printf '#!/bin/sh \n\
if [ -f "/resources/settings.properties" ] \n\
then \n\
	zip -u /usr/local/tomee/webapps/MetFragWeb.war /resources/settings.properties \n\  
fi \n\
if ! [ -z ${WEBPREFIX} ] \n\
then \n\
	mv /usr/local/tomee/webapps/MetFragWeb.war /usr/local/tomee/webapps/${WEBPREFIX}.war \n\
fi \n\
catalina.sh run' > /start.sh

CMD [ "sh", "/start.sh" ]
