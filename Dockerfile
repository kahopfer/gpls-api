FROM openjdk:8-jdk-alpine
ADD target/people-api-0.1.0.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-Xmx150m","-jar","/app.jar"]