package de.caransgar.chorehub.mqtt;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

/**
 * Gateway interface for sending MQTT messages.
 * Domain services use this to publish to MQTT topics.
 */
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    /**
     * Send a message to an MQTT topic.
     * @param payload the message payload
     * @param topic the MQTT topic to publish to
     */
    void sendToMqtt(String payload, @Header("mqtt_topic") String topic);
}
