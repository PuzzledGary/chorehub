package de.caransgar.chorehub.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Listener for application startup and shutdown events.
 * Manages MQTT availability status across the app lifecycle.
 */
@Component
public class MqttAvailabilityListener {

    private static final Logger LOG = LoggerFactory.getLogger(MqttAvailabilityListener.class);
    private static final String STARTUP_SELF_TEST_PAYLOAD_PREFIX = "__chorehub_startup_selftest__:";
    private static final long STARTUP_SELF_TEST_TIMEOUT_SECONDS = 10;

    private final MqttGateway mqttGateway;
    private final ChoreDiscoveryService discoveryService;
    private final MqttInboundSelfTestState selfTestState;

    public MqttAvailabilityListener(
            MqttGateway mqttGateway,
            ChoreDiscoveryService discoveryService,
            MqttInboundSelfTestState selfTestState) {
        this.mqttGateway = mqttGateway;
        this.discoveryService = discoveryService;
        this.selfTestState = selfTestState;
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

            publishStartupSelfTest();
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
     * Publish a non-retained self-test command to verify inbound subscription path.
     * If the subscriber is healthy, MqttCommandHandler will log that the self-test was received.
     */
    private void publishStartupSelfTest() {
        try {
            String selfTestTopic = ChoreMqttTopics.doneCommandTopic(0L);
            String token = UUID.randomUUID().toString();
            String payload = STARTUP_SELF_TEST_PAYLOAD_PREFIX + token;

            selfTestState.begin(token);
            mqttGateway.sendToMqtt(payload, selfTestTopic, false);
            LOG.info("Published MQTT startup self-test command to topic '{}' (token={})", selfTestTopic, token);

            CompletableFuture.delayedExecutor(STARTUP_SELF_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).execute(() -> {
                if (selfTestState.isPending(token)) {
                    LOG.warn(
                            "MQTT inbound self-test was not received within {}s. Check broker ACL/subscribe permissions for topic '{}'",
                            STARTUP_SELF_TEST_TIMEOUT_SECONDS,
                            selfTestTopic);
                }
            });
        } catch (Exception e) {
            LOG.error("Failed to publish MQTT startup self-test command", e);
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
