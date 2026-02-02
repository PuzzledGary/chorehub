package de.caransgar.chorehub.entity;

public enum RecurrenceType {
    FIXED_SCHEDULE,     // e.g., every 1. of month
    AFTER_COMPLETION,   // e.g., every 4 months since last completion
    ONETIME             // one-time chore
}