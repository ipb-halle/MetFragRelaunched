FROM maven:3-jdk-11 AS builder

COPY . /MetFragRelaunched
WORKDIR /MetFragRelaunched

RUN mvn clean install -pl MetFragLib -pl MetFragRest -am -DskipTests
# RUN mvn clean install -pl MetFragLib -am -DskipTests

ENTRYPOINT [ "java", "-Dserver.port=8090", "-jar", "./MetFragRest/target/MetFragRest-2.4.8.jar" ]

# ENTRYPOINT [ "mvn", "-f", "MetFragRest", "spring-boot:run" ]
