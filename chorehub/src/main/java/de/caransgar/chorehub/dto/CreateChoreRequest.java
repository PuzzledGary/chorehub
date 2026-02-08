package de.caransgar.chorehub.dto;

import de.caransgar.chorehub.entity.RecurrenceType;

public class CreateChoreRequest {
    private String name;
    private String description;
    private RecurrenceType recurrenceType;
    private String recurrencePattern;
    private String assignedUsername;

    // Constructors
    public CreateChoreRequest() {
    }

    public CreateChoreRequest(String name, String description, RecurrenceType recurrenceType,
            String recurrencePattern, String assignedUsername) {
        this.name = name;
        this.description = description;
        this.recurrenceType = recurrenceType;
        this.recurrencePattern = recurrencePattern;
        this.assignedUsername = assignedUsername;
    }

    // Getters and Setters
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
}
