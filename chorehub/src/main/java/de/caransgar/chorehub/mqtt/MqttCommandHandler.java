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
    private static final String STARTUP_SELF_TEST_PAYLOAD_PREFIX = "__chorehub_startup_selftest__:";

    private final ChoreService choreService;
    private final MqttInboundSelfTestState selfTestState;

    public MqttCommandHandler(ChoreService choreService, MqttInboundSelfTestState selfTestState) {
        this.choreService = choreService;
        this.selfTestState = selfTestState;
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

            LOG.info("MQTT inbound message received: topic='{}', payload='{}'", topic, payload);
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
            LOG.info("MQTT done command parsed successfully for choreId={}", choreId);

            if (choreId == 0L && payload.startsWith(STARTUP_SELF_TEST_PAYLOAD_PREFIX)) {
                String token = payload.substring(STARTUP_SELF_TEST_PAYLOAD_PREFIX.length());
                if (selfTestState.complete(token)) {
                    LOG.info("MQTT inbound self-test received successfully (token={})", token);
                } else {
                    LOG.warn("Received MQTT inbound self-test token that was not pending (token={})", token);
                }
                return;
            }

            handleMarkChoreDone(choreId);
        } catch (NumberFormatException e) {
            LOG.error("Failed to parse choreId from MQTT topic", e);
        } catch (Exception e) {
            LOG.error("Error handling MQTT command", e);
        }
    }

    private String resolveTopic(Message<?> message) {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        if (topic != null) {
            LOG.debug("Resolved MQTT topic from header '{}'", MqttHeaders.RECEIVED_TOPIC);
            return topic;
        }

        // Backward compatibility for header naming differences across integration versions.
        String legacyTopic = message.getHeaders().get("mqtt_topic", String.class);
        if (legacyTopic != null) {
            LOG.debug("Resolved MQTT topic from legacy header 'mqtt_topic'");
        }
        return legacyTopic;
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
