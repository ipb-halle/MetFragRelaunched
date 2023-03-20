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

RUN mvn -Dhttps.protocols=TLSv1.2 -f MetFragRelaunched clean package -pl MetFragLib -pl MetFragRest -am -DskipTests


FROM tomee:8

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		zip \
        ; \
	rm -rf /var/lib/apt/lists/*

COPY --from=builder /MetFragRelaunched/MetFragRest/target/MetFragRest-*.war /usr/local/tomee/webapps/
RUN printf '#!/bin/sh \n\
if [ -f "/resources/settings.properties" ] \n\
then \n\
	zip -u /usr/local/tomee/webapps/MetFragRest.jar /resources/settings.properties \n\  
fi \n\
if ! [ -z ${WEBPREFIX} ] \n\
then \n\
	mv /usr/local/tomee/webapps/MetFragRest.war /usr/local/tomee/webapps/${WEBPREFIX}.war \n\
fi \n\
catalina.sh run' > /start.sh

CMD [ "sh", "/start.sh" ]
