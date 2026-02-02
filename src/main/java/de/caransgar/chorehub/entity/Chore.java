package de.caransgar.chorehub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chores")
public class Chore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceType recurrenceType;

    private String recurrencePattern; // e.g., "0 0 1 * *" for cron, or "P4M" for ISO period

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    private LocalDateTime lastCompletedDate;

    private LocalDateTime nextDueDate;

    // Constructors
    public Chore() {}

    public Chore(String name, String description, RecurrenceType recurrenceType, String recurrencePattern, User assignedUser) {
        this.name = name;
        this.description = description;
        this.recurrenceType = recurrenceType;
        this.recurrencePattern = recurrencePattern;
        this.assignedUser = assignedUser;
        this.createdDate = LocalDateTime.now();
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

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
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

    @Override
    public String toString() {
        return "Chore{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", recurrenceType=" + recurrenceType +
                ", recurrencePattern='" + recurrencePattern + '\'' +
                ", assignedUser=" + (assignedUser != null ? assignedUser.getName() : null) +
                ", createdDate=" + createdDate +
                ", lastCompletedDate=" + lastCompletedDate +
                ", nextDueDate=" + nextDueDate +
                '}';
    }
}