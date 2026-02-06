# Use Home Assistant build args
ARG BUILD_FROM=ghcr.io/home-assistant/aarch64-base:latest
ARG BUILD_VERSION
ARG BUILD_ARCH

FROM ${BUILD_FROM}

# Build arguments for labels
ARG BUILD_VERSION
ARG BUILD_ARCH

# Enable community repository and install Java 21 Runtime
RUN apk add --no-cache openjdk21-jre --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

# Set working directory
WORKDIR /app

# Copy the Spring Boot JAR from Gradle build
COPY build/libs/*.jar /app/app.jar

# Copy config files for Home Assistant
COPY config.yaml /app/config.yaml

# Copy the run.sh script
COPY run.sh /app/run.sh
RUN chmod +x /app/run.sh

# Home Assistant addon labels
LABEL \
    io.hass.version="${BUILD_VERSION}" \
    io.hass.arch="${BUILD_ARCH}" \
    io.hass.type="addon"

# Set entrypoint (as required by HA addons)
CMD ["/app/run.sh"]
