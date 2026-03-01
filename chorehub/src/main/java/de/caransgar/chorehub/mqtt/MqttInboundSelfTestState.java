package de.caransgar.chorehub.mqtt;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks one in-flight MQTT inbound self-test token across startup.
 */
@Component
public class MqttInboundSelfTestState {

    private final AtomicReference<String> pendingToken = new AtomicReference<>();

    public void begin(String token) {
        pendingToken.set(token);
    }

    public boolean complete(String token) {
        return pendingToken.compareAndSet(token, null);
    }

    public boolean isPending(String token) {
        return token != null && token.equals(pendingToken.get());
    }
}
