FROM java:8-jdk-alpine
ARG artifact_name
COPY jar/${artifact_name} app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]
