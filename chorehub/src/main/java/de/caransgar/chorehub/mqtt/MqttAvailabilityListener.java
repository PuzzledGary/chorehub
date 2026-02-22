package de.caransgar.chorehub.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for application startup and shutdown events.
 * Manages MQTT availability status across the app lifecycle.
 */
@Component
public class MqttAvailabilityListener {

    private static final Logger LOG = LoggerFactory.getLogger(MqttAvailabilityListener.class);

    private final MqttGateway mqttGateway;
    private final ChoreDiscoveryService discoveryService;

    public MqttAvailabilityListener(MqttGateway mqttGateway, ChoreDiscoveryService discoveryService) {
        this.mqttGateway = mqttGateway;
        this.discoveryService = discoveryService;
    }

    /**
     * Called when the application is ready and all beans are initialized.
     * Publishes "online" status to MQTT.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            // Publish availability discovery once
            discoveryService.publishAvailabilityDiscovery();

            // Publish online status
            publishAvailability("online");
            LOG.info("ChoreHub MQTT availability published as ONLINE");
        } catch (Exception e) {
            LOG.error("Failed to publish availability on startup", e);
        }
    }

    /**
     * Publish the current availability status.
     * @param status "online" or "offline"
     */
    private void publishAvailability(String status) {
        try {
            mqttGateway.sendToMqtt(status, ChoreMqttTopics.availabilityTopic());
        } catch (Exception e) {
            LOG.error("Failed to publish availability status: {}", status, e);
        }
    }

    /**
     * Called when the application shuts down.
     * Publishes "offline" status to MQTT.
     * Note: Due to Spring lifecycle constraints, this may not always execute cleanly.
     * Consider using a shutdown hook if reliability is critical.
     */
    public void publishOfflineOnShutdown() {
        try {
            publishAvailability("offline");
            LOG.info("ChoreHub MQTT availability published as OFFLINE");
        } catch (Exception e) {
            LOG.error("Failed to publish offline status on shutdown", e);
        }
    }
}
