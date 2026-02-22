package de.caransgar.chorehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the Backoffice UI.
 * Reads from application.yaml/config.yaml under chorehub.backoffice prefix.
 */
@Component
@ConfigurationProperties(prefix = "chorehub.backoffice")
public class BackofficeConfigProperties {

    private String baseUrl = "http://homeassistant:8080";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
