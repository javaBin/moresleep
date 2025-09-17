FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
EXPOSE 8082
COPY my.jar my.jar
ENV RUN_FROM_JAR true
ENTRYPOINT ["java","-jar","my.jar"]