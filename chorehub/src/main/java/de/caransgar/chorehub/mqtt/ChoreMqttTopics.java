package de.caransgar.chorehub.mqtt;

/**
 * Utility class for building consistent MQTT topic names for ChoreHub.
 * Follows the pattern: chorehub/chores/{choreId}/{aspect}
 * And Home Assistant discovery topics.
 */
public class ChoreMqttTopics {

    private static final String ROOT = "chorehub";
    private static final String CHORES = "chores";
    private static final String HA_DISCOVERY = "homeassistant";

    /**
     * Status topic for a chore: chorehub/chores/{choreId}/status
     */
    public static String statusTopic(Long choreId) {
        return String.format("%s/%s/%d/status", ROOT, CHORES, choreId);
    }

    /**
     * Attributes topic for a chore: chorehub/chores/{choreId}/attributes
     */
    public static String attributesTopic(Long choreId) {
        return String.format("%s/%s/%d/attributes", ROOT, CHORES, choreId);
    }

    /**
     * Command topic to mark a chore as done: chorehub/chores/{choreId}/done/set
     */
    public static String doneCommandTopic(Long choreId) {
        return String.format("%s/%s/%d/done/set", ROOT, CHORES, choreId);
    }

    /**
     * Availability topic: chorehub/status
     */
    public static String availabilityTopic() {
        return String.format("%s/status", ROOT);
    }

    /**
     * Home Assistant MQTT discovery topic for the status sensor.
     * homeassistant/sensor/chorehub_chore_{choreId}_status/config
     */
    public static String discoveryStatusTopic(Long choreId) {
        return String.format("%s/sensor/chorehub_chore_%d_status/config", HA_DISCOVERY, choreId);
    }

    /**
     * Home Assistant MQTT discovery topic for the done button.
     * homeassistant/button/chorehub_chore_{choreId}_done/config
     */
    public static String discoveryDoneButtonTopic(Long choreId) {
        return String.format("%s/button/chorehub_chore_%d_done/config", HA_DISCOVERY, choreId);
    }

    /**
     * Home Assistant MQTT discovery topic for availability.
     */
    public static String discoveryAvailabilityTopic() {
        return String.format("%s/binary_sensor/chorehub_availability/config", HA_DISCOVERY);
    }
}
