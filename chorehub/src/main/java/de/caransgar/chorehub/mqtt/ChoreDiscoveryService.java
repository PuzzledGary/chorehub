package de.caransgar.chorehub.mqtt;

import de.caransgar.chorehub.entity.Chore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for publishing Home Assistant MQTT discovery configurations.
 * Called when chores are created/deleted to register/unregister them in HA.
 */
@Service
public class ChoreDiscoveryService {

    private static final Logger LOG = LoggerFactory.getLogger(ChoreDiscoveryService.class);

    private final MqttGateway mqttGateway;

    public ChoreDiscoveryService(MqttGateway mqttGateway) {
        this.mqttGateway = mqttGateway;
    }

    /**
     * Publish MQTT discovery configurations for a chore.
     * This makes the chore and its "done" button visible in Home Assistant.
     * @param chore the Chore entity to register
     */
    public void publishDiscoveryForChore(Chore chore) {
        try {
            String statusConfigJson = DiscoveryPayloadFactory.sensorConfigJson(chore);
            mqttGateway.sendToMqtt(
                    statusConfigJson,
                    ChoreMqttTopics.discoveryStatusTopic(chore.getId()));

            String buttonConfigJson = DiscoveryPayloadFactory.doneButtonConfigJson(chore);
            mqttGateway.sendToMqtt(
                    buttonConfigJson,
                    ChoreMqttTopics.discoveryDoneButtonTopic(chore.getId()));

            LOG.info("Published discovery for chore {} ({})", chore.getId(), chore.getName());
        } catch (Exception e) {
            LOG.error("Failed to publish discovery for chore {}", chore.getId(), e);
        }
    }

    /**
     * Remove MQTT discovery configurations for a chore.
     * This removes the chore entities from Home Assistant.
     * @param choreId the ID of the chore to unregister
     */
    public void removeDiscoveryForChore(Long choreId) {
        try {
            // Publish empty retained message to remove discovery
            mqttGateway.sendToMqtt("", ChoreMqttTopics.discoveryStatusTopic(choreId));
            mqttGateway.sendToMqtt("", ChoreMqttTopics.discoveryDoneButtonTopic(choreId));

            LOG.info("Removed discovery for chore {}", choreId);
        } catch (Exception e) {
            LOG.error("Failed to remove discovery for chore {}", choreId, e);
        }
    }

    /**
     * Publish availability discovery configuration.
     * Called once at startup.
     */
    public void publishAvailabilityDiscovery() {
        try {
            String availabilityConfigJson = DiscoveryPayloadFactory.availabilityConfigJson();
            mqttGateway.sendToMqtt(
                    availabilityConfigJson,
                    ChoreMqttTopics.discoveryAvailabilityTopic());

            LOG.info("Published availability discovery");
        } catch (Exception e) {
            LOG.error("Failed to publish availability discovery", e);
        }
    }
}
