package de.caransgar.chorehub.mqtt;

import de.caransgar.chorehub.services.ChoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Service for handling inbound MQTT commands.
 * Processes commands like marking chores as done.
 */
@Service
public class MqttCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MqttCommandHandler.class);

    private final ChoreService choreService;

    public MqttCommandHandler(ChoreService choreService) {
        this.choreService = choreService;
    }

    /**
     * Handle inbound MQTT messages from command topics.
     * Expects topics like: chorehub/chores/{choreId}/done/set
     * @param message the MQTT message
     */
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMqttCommand(Message<?> message) {
        try {
            String topic = resolveTopic(message);
            String payload = message.getPayload().toString();

            LOG.debug("Received MQTT command on topic: {} with payload: {}", topic, payload);

            if (topic == null) {
                LOG.warn("Received MQTT message without topic header");
                return;
            }

            // Parse topic: chorehub/chores/{choreId}/done/set
            String[] parts = topic.split("/");
            if (parts.length != 5
                    || !parts[0].equals("chorehub")
                    || !parts[1].equals("chores")
                    || !parts[3].equals("done")
                    || !parts[4].equals("set")) {
                LOG.warn("Unexpected topic format: {}", topic);
                return;
            }

            Long choreId = Long.parseLong(parts[2]);
            handleMarkChoreDone(choreId);
        } catch (NumberFormatException e) {
            LOG.error("Failed to parse choreId from topic", e);
        } catch (Exception e) {
            LOG.error("Error handling MQTT command", e);
        }
    }

    private String resolveTopic(Message<?> message) {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        if (topic != null) {
            return topic;
        }

        // Backward compatibility for header naming differences across integration versions.
        return message.getHeaders().get("mqtt_topic", String.class);
    }

    /**
     * Mark a chore as done and publish updated state.
     * @param choreId the ID of the chore to mark as done
     */
    private void handleMarkChoreDone(Long choreId) {
        choreService.markChoreAsDone(choreId)
                .ifPresentOrElse(
                        chore -> LOG.info("Marked chore {} as done via MQTT", choreId),
                        () -> LOG.warn("Chore {} not found", choreId)
                );
    }
}
