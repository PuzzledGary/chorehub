package de.caransgar.chorehub.dto;

import java.time.Instant;

/**
 * DTO representing the attributes/state of a chore for MQTT.
 * This gets serialized to JSON and published to the attributes topic.
 */
public class ChoreAttributes {

    private String title;
    private Instant due;
    private String assignee;
    private Integer intervalDays;
    private String notes;
    private Instant lastDone;

    public ChoreAttributes() {
    }

    public ChoreAttributes(String title, Instant due, String assignee, Integer intervalDays,
                           String notes, Instant lastDone) {
        this.title = title;
        this.due = due;
        this.assignee = assignee;
        this.intervalDays = intervalDays;
        this.notes = notes;
        this.lastDone = lastDone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getDue() {
        return due;
    }

    public void setDue(Instant due) {
        this.due = due;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getLastDone() {
        return lastDone;
    }

    public void setLastDone(Instant lastDone) {
        this.lastDone = lastDone;
    }
}
