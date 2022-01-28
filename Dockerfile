FROM openjdk:18-jdk-alpine3.15

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} /

RUN ls -la && ln -s *.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
