package de.caransgar.chorehub.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Spring Integration MQTT configuration.
 * Sets up MQTT client factory, inbound/outbound channels, and handlers.
 */
@Configuration
@EnableIntegration
public class MqttConfiguration {

    private final MqttConfigProperties mqttConfig;

    public MqttConfiguration(MqttConfigProperties mqttConfig) {
        this.mqttConfig = mqttConfig;
    }

    /**
     * Creates and configures the MQTT Paho client factory.
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttConfig.getBrokerUrl()});

        if (mqttConfig.getUsername() != null && !mqttConfig.getUsername().isBlank()) {
            options.setUserName(mqttConfig.getUsername());
            if (mqttConfig.getPassword() != null) {
                options.setPassword(mqttConfig.getPassword().toCharArray());
            }
        }

        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * Outbound channel for publishing MQTT messages.
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * Outbound MQTT handler for publishing messages.
     * Uses async mode with retained flag default.
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory clientFactory) {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                mqttConfig.getBrokerUrl(),
                mqttConfig.getClientIdPrefix() + "-publisher-" + System.currentTimeMillis(),
                clientFactory);

        handler.setAsync(true);
        handler.setDefaultQos(1);
        handler.setDefaultRetained(true);

        return handler;
    }

    /**
     * Inbound channel for receiving MQTT commands.
     */
    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    /**
     * Inbound MQTT adapter listening for chore commands.
     * Subscribes to topics like: chorehub/chores/+/done/set
     */
    @Bean
    public MessageProducer mqttInbound(MqttPahoClientFactory clientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        mqttConfig.getBrokerUrl(),
                        mqttConfig.getClientIdPrefix() + "-subscriber-" + System.currentTimeMillis(),
                        clientFactory,
                        "chorehub/chores/+/done/set");

        adapter.setQos(1);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(mqttInboundChannel());

        return adapter;
    }
}
