#!/bin/sh

# Home Assistant stores addon configuration in /data/options.json
CONFIG_PATH=/data/options.json

# Read configuration using jq
DB_HOST=$(jq --raw-output '.db_host // "core-mariadb"' $CONFIG_PATH)
DB_NAME=$(jq --raw-output '.db_name // "chorehub"' $CONFIG_PATH)
DB_USER=$(jq --raw-output '.db_user // "chorehub_user"' $CONFIG_PATH)
DB_PASS=$(jq --raw-output '.db_password' $CONFIG_PATH)
LOG_LEVEL=$(jq --raw-output '.log_level // "info"' $CONFIG_PATH)

echo "Starting ChoreHub v0.1.0..."
echo "Database: jdbc:mariadb://${DB_HOST}:3306/${DB_NAME}"

# Export variables for Spring Boot
export SPRING_DATASOURCE_URL="jdbc:mariadb://${DB_HOST}:3306/${DB_NAME}"
export SPRING_DATASOURCE_USERNAME="${DB_USER}"
export SPRING_DATASOURCE_PASSWORD="${DB_PASS}"
export LOGGING_LEVEL_ROOT="${LOG_LEVEL}"

# Start the application
exec java -jar app.jar
