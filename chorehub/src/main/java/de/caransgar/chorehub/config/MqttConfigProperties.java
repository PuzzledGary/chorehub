package de.caransgar.chorehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for MQTT broker connection.
 * Reads from application.yaml under chorehub.mqtt prefix.
 */
@Component
@ConfigurationProperties(prefix = "chorehub.mqtt")
public class MqttConfigProperties {

    private String brokerUrl = "tcp://localhost:1883";
    private String username;
    private String password;
    private String clientIdPrefix = "chorehub";

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientIdPrefix() {
        return clientIdPrefix;
    }

    public void setClientIdPrefix(String clientIdPrefix) {
        this.clientIdPrefix = clientIdPrefix;
    }
}
