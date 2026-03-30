package com.ocean.forecast.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateTimeUtils() {
    }

    public static String nowIso() {
        return LocalDateTime.now().format(ISO_FORMATTER);
    }
}
