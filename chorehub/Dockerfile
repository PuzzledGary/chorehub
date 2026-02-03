# Stage 1: Build the application
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /build

# Copy Gradle config first to cache dependencies
COPY build.gradle settings.gradle ./
# Copy gradle wrapper if you use it (recommended)
COPY gradlew .
COPY gradle ./gradle

# Download dependencies (this layer caches until dependencies change)
# Using 'clean' effectively verifies the config without building full app
RUN ./gradlew clean --no-daemon

# Copy source code and build the bootJar
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

# Install jq for the run.sh script
RUN apk add --no-cache jq

WORKDIR /app

# Copy the built jar. 
# Note: Spring Boot Gradle plugin usually creates the jar in build/libs/
# The wildcard *.jar ensures we pick it up regardless of version name
COPY --from=builder /build/build/libs/*.jar app.jar
COPY run.sh .

RUN chmod +x run.sh

EXPOSE 8080

CMD [ "./run.sh" ]
