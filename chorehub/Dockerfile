ARG BUILD_FROM
# --- Stage 1: Builder ---
FROM gradle:8-jdk21-alpine AS builder

WORKDIR /app

# 1. Pre-copy dependency files to cache the 'gradle' download layer
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# 2. Copy source and build the JAR
COPY src src
RUN ./gradlew build --no-daemon -x test

# --- Stage 2: Final Runtime ---
FROM $BUILD_FROM

# 1. Install Java 21 JRE (Stable Alpine Repo)
RUN apk add --no-cache openjdk21-jre-headless

WORKDIR /app

# 2. Copy the compiled JAR
# Spring Boot usually names the jar based on project name/version
COPY --from=builder /app/build/libs/*.jar app.jar

# 3. Startup script
COPY run.sh /
RUN chmod a+x /run.sh

CMD [ "/run.sh" ]