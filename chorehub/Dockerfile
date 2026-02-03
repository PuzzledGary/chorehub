# Stage 1: Build the application
# We use the full JDK to build the project
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copy gradle wrapper and configuration first to cache dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts . 

# Make the wrapper executable and download dependencies
# (Running 'build' here with no source will just download dependencies)
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || return 0

# Copy the actual source code
COPY src src

# Build the JAR, skipping tests to save time and avoid environment issues
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Run the application
# We use the JRE (Runtime Environment) for a smaller final image
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the built JAR from the builder stage
# The jar is typically in build/libs/ and named after your project-version
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port your Spring Boot app runs on (default 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
