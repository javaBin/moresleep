# Build: docker build -t moresleep-app:latest .
# Run: docker run  --name moresleep-container -p 8082:8082 moresleep-app:latest
# Check localhost:8082 and you should get a homepage
# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Create the final runtime image
FROM eclipse-temurin:21-jre-jammy AS final
ENV DO_FLYWAY_MIGRATION false
ENV RUN_FROM_JAR true
WORKDIR /app
COPY --from=build /app/target/moresleep-0.0.1-jar-with-dependencies.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]