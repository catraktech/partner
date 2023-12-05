FROM openjdk:8
COPY target/partner-0.0.1-SNAPSHOT.jar partner-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/partner-0.0.1-SNAPSHOT.jar"]
