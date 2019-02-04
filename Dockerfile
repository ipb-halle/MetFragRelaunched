FROM maven:latest

## Build WAR from github repo, keep *.war files and cleanup build environment (for smaller docker image)
## sample/index.html is required for kubernetes livenessProbe
RUN git clone --depth 1 https://github.com/ipb-halle/MetFragRelaunched.git 

WORKDIR MetFragRelaunched

RUN mvn clean 
RUN mvn install -pl MetFragLib -am -DskipTests
RUN mvn package -pl MetFragWeb

RUN mkdir -p /usr/local/tomcat/webapps/sample/ ; \
    touch /usr/local/tomcat/webapps/sample/index.html ;\
    cp /MetFragRelaunched/MetFragWeb/target/MetFragWeb.war /usr/local/tomcat/webapps/ ; \
    rm -rf /MetFragRelaunched .m2 

CMD [ "sh", "-c", "cp -avx /usr/local/tomcat/webapps/* /app ; tail -f /dev/null"]
