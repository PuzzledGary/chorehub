package de.caransgar.chorehub.dto;

import de.caransgar.chorehub.entity.RecurrenceType;
import java.time.LocalDateTime;

/**
 * DTO for transferring Chore data to API consumers.
 * This decouples the internal JPA entity from the API contract.
 */
public class ChoreDTO {
    private Long id;
    private String name;
    private String description;
    private RecurrenceType recurrenceType;
    private String recurrencePattern;
    private String assignedUsername;
    private LocalDateTime createdDate;
    private LocalDateTime lastCompletedDate;
    private LocalDateTime nextDueDate;

    // Constructors
    public ChoreDTO() {
    }

    public ChoreDTO(Long id, String name, String description, RecurrenceType recurrenceType,
            String recurrencePattern, String assignedUsername, LocalDateTime createdDate,
            LocalDateTime lastCompletedDate, LocalDateTime nextDueDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.recurrenceType = recurrenceType;
        this.recurrencePattern = recurrencePattern;
        this.assignedUsername = assignedUsername;
        this.createdDate = createdDate;
        this.lastCompletedDate = lastCompletedDate;
        this.nextDueDate = nextDueDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public String getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public String getAssignedUsername() {
        return assignedUsername;
    }

    public void setAssignedUsername(String assignedUsername) {
        this.assignedUsername = assignedUsername;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(LocalDateTime lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    public LocalDateTime getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDateTime nextDueDate) {
        this.nextDueDate = nextDueDate;
    }
}
