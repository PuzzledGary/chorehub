package de.caransgar.chorehub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chore_history")
public class ChoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chore_id", nullable = false)
    private Chore chore;

    @Column(nullable = false)
    private LocalDateTime completedDate;

    @Column(length = 500)
    private String notes; // Optional notes about the completion

    // Constructors
    public ChoreHistory() {
    }

    public ChoreHistory(Chore chore, LocalDateTime completedDate) {
        this.chore = chore;
        this.completedDate = completedDate;
    }

    public ChoreHistory(Chore chore, LocalDateTime completedDate, String notes) {
        this.chore = chore;
        this.completedDate = completedDate;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chore getChore() {
        return chore;
    }

    public void setChore(Chore chore) {
        this.chore = chore;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "ChoreHistory{" +
                "id=" + id +
                ", completedDate=" + completedDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
