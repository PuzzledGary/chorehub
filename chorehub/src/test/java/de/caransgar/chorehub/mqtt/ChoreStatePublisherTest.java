package de.caransgar.chorehub.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caransgar.chorehub.dto.ChoreAttributes;
import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.ChoreStatus;
import de.caransgar.chorehub.entity.RecurrenceType;
import de.caransgar.chorehub.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Unit tests for ChoreStatePublisher.
 * Tests serialization of chores with Instant fields to JSON for MQTT publication.
 */
@ExtendWith(MockitoExtension.class)
class ChoreStatePublisherTest {

    @Mock
    private MqttGateway mqttGateway;

    private ChoreStatePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ChoreStatePublisher(mqttGateway);
    }

    // ========== publishStatus Tests ==========

    @Test
    void testPublishStatusForOverdueChore() {
        // Given
        Chore chore = createTestChore(1L, "Clean kitchen", LocalDateTime.now().minusDays(5));
        chore.setLastCompletedDate(null);

        // When
        publisher.publishStatus(chore);

        // Then
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(statusCaptor.capture(), topicCaptor.capture());

        assertThat(statusCaptor.getValue()).isEqualTo("overdue");
        assertThat(topicCaptor.getValue()).isEqualTo("chorehub/chores/1/status");
    }

    @Test
    void testPublishStatusForDueChore() {
        // Given
        Chore chore = createTestChore(1L, "Clean kitchen", LocalDateTime.now().plusDays(5), null);
        chore.setLastCompletedDate(null);

        // When
        publisher.publishStatus(chore);

        // Then
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(statusCaptor.capture(), any());

        assertThat(statusCaptor.getValue()).isEqualTo(ChoreStatus.DUE.getHaValue());
    }

    @Test
    void testPublishStatusForCompletedChore() {
        // Given
        Chore chore = createTestChore(1L, "Clean kitchen", LocalDateTime.now().plusDays(5));
        chore.setLastCompletedDate(LocalDateTime.now().minusHours(1));

        // When
        publisher.publishStatus(chore);

        // Then
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(statusCaptor.capture(), any());

        assertThat(statusCaptor.getValue()).isEqualTo(ChoreStatus.DONE.getHaValue());
    }

    // ========== publishAttributes Tests ==========

    @Test
    void testPublishAttributesSerializesInstantFieldsCorrectly() {
        // Given
        LocalDateTime nextDueLocalDateTime = LocalDateTime.of(2026, 3, 15, 10, 30);
        LocalDateTime lastCompletedLocalDateTime = LocalDateTime.of(2026, 2, 20, 14, 0);
        
        Chore chore = createTestChore(1L, "Vacuum living room", nextDueLocalDateTime);
        chore.setLastCompletedDate(lastCompletedLocalDateTime);

        // When
        publisher.publishAttributes(chore);

        // Then - verify the JSON can be parsed and contains expected fields
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(jsonCaptor.capture(), any());

        String json = jsonCaptor.getValue();
        assertThat(json).contains("\"title\"", "\"Vacuum living room\"");
        assertThat(json).contains("\"due\"");
        assertThat(json).contains("\"lastDone\"");
        
        // Verify the JSON is valid JSON by deserializing it
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        assertThatCode(() -> {
            mapper.readValue(json, ChoreAttributes.class);
        }).doesNotThrowAnyException();
    }

    @Test
    void testPublishAttributesWithNullInstantFields() {
        // Given - a chore with no due date and no completion
        Chore chore = createChoreWithoutDueDate(2L, "Optional task");

        // When - should not throw exception even with null Instant fields
        assertThatCode(() -> publisher.publishAttributes(chore))
                .doesNotThrowAnyException();

        // Then - verify MQTT publication was called
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(jsonCaptor.capture(), any());
        
        String json = jsonCaptor.getValue();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        assertThatCode(() -> {
            ChoreAttributes attributes = mapper.readValue(json, ChoreAttributes.class);
            assertThat(attributes.getDue()).isNull();
            assertThat(attributes.getLastDone()).isNull();
        }).doesNotThrowAnyException();
    }

    @Test
    void testPublishAttributesTopicIsCorrect() {
        // Given
        Chore chore = createTestChore(42L, "Test chore", LocalDateTime.now().plusDays(1));

        // When
        publisher.publishAttributes(chore);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(any(), topicCaptor.capture());
        
        assertThat(topicCaptor.getValue()).isEqualTo("chorehub/chores/42/attributes");
    }

    // ========== publishStatusAndAttributes Tests ==========

    @Test
    void testPublishStatusAndAttributesCallsBothMethods() {
        // Given
        Chore chore = createTestChore(3L, "Do laundry", LocalDateTime.now().plusDays(2));

        // When
        publisher.publishStatusAndAttributes(chore);

        // Then - verify sendToMqtt was called twice (once for status, once for attributes)
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway, times(2)).sendToMqtt(any(), topicCaptor.capture());
        
        java.util.List<String> topics = topicCaptor.getAllValues();
        assertThat(topics).hasSize(2);
        assertThat(topics.get(0)).isEqualTo("chorehub/chores/3/status");
        assertThat(topics.get(1)).isEqualTo("chorehub/chores/3/attributes");
    }

    // ========== Error Handling Tests ==========

    @Test
    void testPublishAttributesDoesNotThrowWhenMqttGatewayFails() {
        // Given
        Chore chore = createTestChore(4L, "Task with gateway failure", LocalDateTime.now().plusDays(1));
        org.mockito.Mockito.doThrow(new RuntimeException("MQTT connection failed"))
                .when(mqttGateway).sendToMqtt(any(), any());

        // When/Then - should not throw, only log error
        assertThatCode(() -> publisher.publishAttributes(chore))
                .doesNotThrowAnyException();
    }

    // ========== Helper Methods ==========

    private Chore createTestChore(Long id, String name, LocalDateTime nextDueDate) {
        return createTestChore(id, name, nextDueDate, LocalDateTime.now());
    }

    private Chore createTestChore(Long id, String name, LocalDateTime nextDueDate, LocalDateTime lastCompletedDate) {
        Chore chore = new Chore();
        chore.setId(id);
        chore.setName(name);
        chore.setDescription("Test description for " + name);
        chore.setNextDueDate(nextDueDate);
        chore.setLastCompletedDate(lastCompletedDate);
        chore.setRecurrenceType(RecurrenceType.FIXED_SCHEDULE);
        
        User user = new User("Test User", "TU");
        user.setId(1L);
        chore.setAssignedUser(user);
        
        return chore;
    }

    private Chore createChoreWithoutDueDate(Long id, String name) {
        Chore chore = new Chore();
        chore.setId(id);
        chore.setName(name);
        chore.setDescription("Optional task without due date");
        chore.setNextDueDate(null);
        chore.setLastCompletedDate(null);
        chore.setRecurrenceType(RecurrenceType.ONETIME);
        return chore;
    }
}
