package de.caransgar.chorehub.entity;

/**
 * Enum representing the status of a chore.
 * Maps to Home Assistant compatible string values.
 */
public enum ChoreStatus {

    /**
     * Chore has been completed.
     */
    DONE("done"),

    /**
     * Chore is due (within today, but not yet overdue).
     */
    DUE("due"),

    /**
     * Chore is overdue (past the due date).
     */
    OVERDUE("overdue");

    private final String haValue;

    ChoreStatus(String haValue) {
        this.haValue = haValue;
    }

    /**
     * Get the Home Assistant compatible string value.
     */
    public String getHaValue() {
        return haValue;
    }

    /**
     * Parse a Home Assistant string value back to enum.
     */
    public static ChoreStatus fromHaValue(String haValue) {
        for (ChoreStatus status : values()) {
            if (status.haValue.equalsIgnoreCase(haValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ChoreStatus: " + haValue);
    }
}
