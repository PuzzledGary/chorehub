#!/usr/bin/with-contenv bashio
set -e

bashio::log.info "Initializing ChoreHub (Connecting to external DB)..."

# --- 1. DATABASE CONNECTION ---
# We pull these from the user's Addon Configuration UI
DB_HOST=$(bashio::config 'db_host')
DB_PORT=$(bashio::config 'db_port')
DB_NAME=$(bashio::config 'db_name')
DB_USER=$(bashio::config 'db_user')
DB_PASS=$(bashio::config 'db_password')

# Map to Spring Boot Environment Variables
export SPRING_DATASOURCE_URL="jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}?createDatabaseIfNotExist=true"
export SPRING_DATASOURCE_USERNAME="${DB_USER}"
export SPRING_DATASOURCE_PASSWORD="${DB_PASS}"

# --- 2. SERVER CONFIG ---
export SERVER_PORT=$(bashio::config 'server_port' '8080')

# --- 3. MQTT CONFIGURATION ---
# Read MQTT settings from Addon Configuration
MQTT_BROKER=$(bashio::config 'mqtt_broker' 'core-mosquitto')
MQTT_PORT=$(bashio::config 'mqtt_port' '1883')
MQTT_USERNAME=$(bashio::config 'mqtt_username' 'chorehub')
MQTT_PASSWORD=$(bashio::config 'mqtt_password' 'chorehub')
MQTT_CLIENT_ID=$(bashio::config 'mqtt_client_id' 'chorehub')

# Build broker URL and export MQTT settings
export MQTT_BROKER_URL="tcp://${MQTT_BROKER}:${MQTT_PORT}"
export MQTT_USERNAME="${MQTT_USERNAME}"
export MQTT_PASSWORD="${MQTT_PASSWORD}"
export MQTT_CLIENT_ID="${MQTT_CLIENT_ID}"

bashio::log.info "MQTT Broker URL: ${MQTT_BROKER_URL}"
if [ -n "${MQTT_USERNAME}" ]; then
  bashio::log.info "MQTT Username: ${MQTT_USERNAME}"
fi

# --- 4. WAIT FOR DATABASE (Optional but Recommended) ---
# This prevents Spring Boot from crashing if it starts faster than the DB addon
bashio::log.info "Waiting for database at ${DB_HOST}:${DB_PORT}..."
while ! nc -z "$DB_HOST" "$DB_PORT"; do
  sleep 1
done
bashio::log.info "Database is up! Starting ChoreHub..."

# --- 5. EXECUTION ---
cd /app
exec java \
    -Xmx256M \
    -XX:+UseSerialGC \
    -jar /app/app.jar