package de.caransgar.chorehub.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caransgar.chorehub.dto.ChoreAttributes;
import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.ChoreStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service for publishing chore status and attributes to MQTT.
 * Called whenever a chore changes state.
 */
@Service
public class ChoreStatePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ChoreStatePublisher.class);

    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChoreStatePublisher(MqttGateway mqttGateway) {
        this.mqttGateway = mqttGateway;
    }

    /**
     * Publish the current status of a chore.
     * Status is computed based on the chore's due date and completion state.
     * @param chore the Chore to publish
     */
    public void publishStatus(Chore chore) {
        try {
            ChoreStatus status = computeStatus(chore);
            String statusString = status.getHaValue();

            mqttGateway.sendToMqtt(
                    statusString,
                    ChoreMqttTopics.statusTopic(chore.getId()));

            LOG.debug("Published status '{}' for chore {} ({})", statusString, chore.getId(), chore.getName());
        } catch (Exception e) {
            LOG.error("Failed to publish status for chore {}", chore.getId(), e);
        }
    }

    /**
     * Publish the attributes (metadata) of a chore.
     * Attributes include title, due date, assignee, etc.
     * @param chore the Chore to publish
     */
    public void publishAttributes(Chore chore) {
        try {
            ChoreAttributes attributes = ChoreAttributesMapper.toChoreAttributes(chore);
            String attributesJson = objectMapper.writeValueAsString(attributes);

            mqttGateway.sendToMqtt(
                    attributesJson,
                    ChoreMqttTopics.attributesTopic(chore.getId()));

            LOG.debug("Published attributes for chore {} ({})", chore.getId(), chore.getName());
        } catch (Exception e) {
            LOG.error("Failed to publish attributes for chore {}", chore.getId(), e);
        }
    }

    /**
     * Publish both status and attributes.
     * @param chore the Chore to publish
     */
    public void publishStatusAndAttributes(Chore chore) {
        publishStatus(chore);
        publishAttributes(chore);
    }

    /**
     * Compute the ChoreStatus based on the chore's due date and last completion.
     * @param chore the Chore to evaluate
     * @return the computed ChoreStatus
     */
    private ChoreStatus computeStatus(Chore chore) {
        if (chore.getLastCompletedDate() == null) {
            // Never completed
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (chore.getNextDueDate() != null && chore.getNextDueDate().isBefore(now)) {
                return ChoreStatus.OVERDUE;
            } else {
                return ChoreStatus.DUE;
            }
        }

        // Recently completed or not yet due
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (chore.getNextDueDate() == null) {
            // Completed, no next due for one-time chores
            return ChoreStatus.DONE;
        }

        if (chore.getNextDueDate().isBefore(now)) {
            return ChoreStatus.OVERDUE;
        } else if (chore.getNextDueDate().isAfter(now)) {
            // Due in the future
            return ChoreStatus.DONE;
        }

        // Today or just became due
        return ChoreStatus.DUE;
    }
}
