ARG IMAGE=ghcr.io/<OWNER>/chorehub:latest
FROM ${IMAGE}

# Runtime wrapper: this Dockerfile pulls the pre-built image from GHCR
# and starts it. Replace the default `IMAGE` build-arg when needed.

EXPOSE 8080

# If the upstream image provides an entrypoint, the image will start
# using that. Otherwise try the bundled run script if present.
CMD [ "/run.sh" ]