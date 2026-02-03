# We use the argument defined by build.yaml
ARG BUILD_FROM
FROM $BUILD_FROM

# Set shell to bash (optional but good for HA consistency)
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Setup work directory
WORKDIR /app

# Copy necessary build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Grant execution rights to the wrapper
RUN chmod +x gradlew

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src src

# Build the JAR
# We skip tests to speed up the build in the add-on environment
RUN ./gradlew bootJar -x test --no-daemon

# --- Run Configuration ---
# Move the built jar to a standard location
RUN mv build/libs/*.jar app.jar

# Clean up build files to reduce image size (Optional but recommended)
# Note: In a single-stage build like this, layers persist, but this helps slightly.
RUN rm -rf src gradle build.gradle gradlew

# Expose the port
EXPOSE 8080

# Labels required by Home Assistant
ARG BUILD_ARCH
ARG BUILD_VERSION
LABEL \
    io.hass.name="ChoreHub" \
    io.hass.description="Spring Boot backend to manage chores" \
    io.hass.arch="${BUILD_ARCH}" \
    io.hass.type="addon" \
    io.hass.version="${BUILD_VERSION}"

# Start the application
CMD [ "java", "-jar", "/app/app.jar" ]
