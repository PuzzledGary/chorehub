package de.caransgar.chorehub.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.caransgar.chorehub.entity.Chore;

/**
 * Factory class for generating Home Assistant MQTT discovery payloads.
 * Uses Jackson to build JSON discovery messages for sensors and buttons.
 */
public class DiscoveryPayloadFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate Home Assistant MQTT discovery JSON for a chore status sensor.
     * @param chore the Chore entity
     * @return JSON string for discovery publish
     */
    public static String sensorConfigJson(Chore chore) {
        ObjectNode config = objectMapper.createObjectNode();

        String choreId = chore.getId().toString();
        String name = "Chore: " + chore.getName();
        String uniqueId = "chorehub_chore_" + choreId + "_status";

        config.put("name", name);
        config.put("unique_id", uniqueId);
        config.put("state_topic", ChoreMqttTopics.statusTopic(chore.getId()));
        config.put("json_attributes_topic", ChoreMqttTopics.attributesTopic(chore.getId()));
        config.put("availability_topic", ChoreMqttTopics.availabilityTopic());
        config.put("payload_available", "online");
        config.put("payload_not_available", "offline");

        // Device metadata for grouping in HA
        ObjectNode device = objectMapper.createObjectNode();
        device.put("identifiers", objectMapper.createArrayNode().add("chorehub"));
        device.put("name", "ChoreHub");
        device.put("manufacturer", "ChoreHub");
        config.set("device", device);

        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize sensor config JSON", e);
        }
    }

    /**
     * Generate Home Assistant MQTT discovery JSON for a chore done button.
     * @param chore the Chore entity
     * @return JSON string for discovery publish
     */
    public static String doneButtonConfigJson(Chore chore) {
        ObjectNode config = objectMapper.createObjectNode();

        String choreId = chore.getId().toString();
        String name = "Mark done: " + chore.getName();
        String uniqueId = "chorehub_chore_" + choreId + "_done_button";

        config.put("name", name);
        config.put("unique_id", uniqueId);
        config.put("command_topic", ChoreMqttTopics.doneCommandTopic(chore.getId()));
        config.put("availability_topic", ChoreMqttTopics.availabilityTopic());
        config.put("payload_available", "online");
        config.put("payload_not_available", "offline");
        config.put("payload_press", "1");

        // Device metadata for grouping in HA
        ObjectNode device = objectMapper.createObjectNode();
        device.put("identifiers", objectMapper.createArrayNode().add("chorehub"));
        device.put("name", "ChoreHub");
        device.put("manufacturer", "ChoreHub");
        config.set("device", device);

        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize button config JSON", e);
        }
    }

    /**
     * Generate Home Assistant MQTT discovery JSON for availability.
     * @return JSON string for discovery publish
     */
    public static String availabilityConfigJson() {
        ObjectNode config = objectMapper.createObjectNode();

        config.put("name", "ChoreHub Availability");
        config.put("unique_id", "chorehub_availability");
        config.put("state_topic", ChoreMqttTopics.availabilityTopic());
        config.put("payload_on", "online");
        config.put("payload_off", "offline");

        ObjectNode device = objectMapper.createObjectNode();
        device.put("identifiers", objectMapper.createArrayNode().add("chorehub"));
        device.put("name", "ChoreHub");
        device.put("manufacturer", "ChoreHub");
        config.set("device", device);

        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize availability config JSON", e);
        }
    }
}
