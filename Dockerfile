# Platform image: built and pushed to ECR by the javaBin platform pipeline
# (.github/workflows/deploy.yml), run on ECS Fargate.
#
# Configuration comes from environment variables that ECS injects (app.yaml
# `environment:` and `resources.secrets`), so the image holds no secrets and
# needs no SOPS or AWS CLI. The container runs as uid 1000 with a read-only
# root filesystem and a writable /tmp.
#
# Local run:
#   docker build -t moresleep .
#   docker run --rm -p 8082:8082 -e DBHOST=host.docker.internal moresleep

# Stage 1: build the jar (tests run in the pipeline's build-jvm job before this)
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn --batch-mode --quiet dependency:go-offline
COPY src ./src
RUN mvn --batch-mode package -Dmaven.test.skip=true

# Stage 2: runtime
FROM eclipse-temurin:21-jre-jammy
# Production settings (port 5000, DO_FLYWAY_MIGRATION=false, ...) come from
# app.yaml. The image keeps the code defaults, so it also works as the local
# dev image (port 8082, migrations on), like the previous :edge image.
ENV RUN_FROM_JAR=true
WORKDIR /app
COPY --from=build /app/target/moresleep-0.0.1-jar-with-dependencies.jar app.jar
USER 1000
EXPOSE 5000 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
