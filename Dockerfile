# Build: docker build -t moresleep-app:latest .
# Run: docker run  --name moresleep-container -p 5000:5000 moresleep-app:latest
# Check localhost:5000 and you should get a homepage
# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Create the final runtime image
FROM eclipse-temurin:21-jre-jammy AS final

# Install SOPS
RUN curl -L -o /usr/local/bin/sops https://github.com/getsops/sops/releases/download/v3.10.2/sops-v3.10.2.linux.amd64 && \
    chmod +x /usr/local/bin/sops

    # Install unzip and curl
RUN apt-get update && \
    apt-get install -y curl unzip && \
    rm -rf /var/lib/apt/lists/*

    # Install AWS CLI v2
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip" && \
    unzip /tmp/awscliv2.zip -d /tmp && \
    /tmp/aws/install && \
    rm -rf /tmp/aws /tmp/awscliv2.zip

# Set environment variables
ENV DO_FLYWAY_MIGRATION=false
ENV RUN_FROM_JAR=true

WORKDIR /app
COPY --from=build /app/target/moresleep-0.0.1-jar-with-dependencies.jar app.jar
COPY ./conf/.enc.env /app/.enc.env
EXPOSE 5000
ENTRYPOINT ["sops", "exec-env", ".enc.env", "java -jar app.jar"]