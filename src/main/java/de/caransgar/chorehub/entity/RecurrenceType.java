package de.caransgar.chorehub.entity;

public enum RecurrenceType {
    FIXED,    // e.g., every 1. of month
    FLEXIBLE, // e.g., every 4 months since last completion
    ONETIME   // one-time chore
}