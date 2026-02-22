package de.caransgar.chorehub.mqtt;

import de.caransgar.chorehub.dto.ChoreAttributes;
import de.caransgar.chorehub.entity.Chore;

/**
 * Mapper to convert Chore entity to ChoreAttributes DTO for MQTT.
 */
public class ChoreAttributesMapper {

    /**
     * Convert a Chore entity to ChoreAttributes DTO.
     */
    public static ChoreAttributes toChoreAttributes(Chore chore) {
        java.time.Instant dueDateInstant = chore.getNextDueDate() != null
                ? chore.getNextDueDate().atZone(java.time.ZoneId.systemDefault()).toInstant()
                : null;
        java.time.Instant lastDoneInstant = chore.getLastCompletedDate() != null
                ? chore.getLastCompletedDate().atZone(java.time.ZoneId.systemDefault()).toInstant()
                : null;

        return new ChoreAttributes(
                chore.getName(),
                dueDateInstant,
                chore.getAssignedUser() != null ? chore.getAssignedUser().getName() : null,
                null,  // interval days not currently tracked
                chore.getDescription(),
                lastDoneInstant
        );
    }
}
