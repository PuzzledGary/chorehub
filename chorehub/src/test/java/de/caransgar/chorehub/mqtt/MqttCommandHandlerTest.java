package de.caransgar.chorehub.mqtt;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.services.ChoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MqttCommandHandlerTest {

    @Mock
    private ChoreService choreService;

    private MqttCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MqttCommandHandler(choreService);
    }

    @Test
    void handlesDoneSetTopicWithReceivedTopicHeader() {
        Chore chore = new Chore();
        chore.setId(42L);
        when(choreService.markChoreAsDone(42L)).thenReturn(Optional.of(chore));

        Message<String> message = MessageBuilder
                .withPayload("1")
                .setHeader(MqttHeaders.RECEIVED_TOPIC, "chorehub/chores/42/done/set")
                .build();

        handler.handleMqttCommand(message);

        verify(choreService).markChoreAsDone(42L);
    }

    @Test
    void handlesDoneSetTopicWithLegacyTopicHeader() {
        Chore chore = new Chore();
        chore.setId(7L);
        when(choreService.markChoreAsDone(7L)).thenReturn(Optional.of(chore));

        Message<String> message = MessageBuilder
                .withPayload("PRESS")
                .setHeader("mqtt_topic", "chorehub/chores/7/done/set")
                .build();

        handler.handleMqttCommand(message);

        verify(choreService).markChoreAsDone(7L);
    }

    @Test
    void ignoresUnexpectedTopicShapes() {
        Message<String> message = MessageBuilder
                .withPayload("1")
                .setHeader(MqttHeaders.RECEIVED_TOPIC, "chorehub/chores/7/done")
                .build();

        handler.handleMqttCommand(message);

        verify(choreService, never()).markChoreAsDone(7L);
    }
}
