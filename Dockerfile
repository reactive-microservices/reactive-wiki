#-----------------------
#---- Build container --
#-----------------------

FROM maven:3.5-jdk-8 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

#-----------------------
#---- Run container --
#-----------------------

FROM gcr.io/distroless/java

MAINTAINER Maksym Stepanenko <stepanenkomaksi@gmail.com>

COPY --from=build /usr/src/app/target/reactive-wiki-1.0.0-SNAPSHOT-fat.jar /usr/local/reactive-wiki-1.0.0-SNAPSHOT-fat.jar

EXPOSE 8080

#HEALTHCHECK --start-period=30s --interval=5s CMD curl http://localhost:8080/health || exit 1

ENTRYPOINT ["java","-jar","/usr/local/reactive-wiki-1.0.0-SNAPSHOT-fat.jar"]
