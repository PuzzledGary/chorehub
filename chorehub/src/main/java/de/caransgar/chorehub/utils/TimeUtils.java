package de.caransgar.chorehub.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeUtils {

    /**
     * Returns the LocalDateTime representing 00:00:00 of the next day.
     */
    public static LocalDateTime getStartOfTomorrow() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }

}
