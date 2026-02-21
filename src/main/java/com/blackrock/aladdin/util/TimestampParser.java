package com.blackrock.aladdin.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Deterministic parsing of "yyyy-MM-dd HH:mm:ss" to Instant (UTC).
 */
public final class TimestampParser {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimestampParser() {}

    public static Instant parse(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("timestamp required");
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(timestamp.trim(), FORMAT);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp: " + timestamp + ". Use yyyy-MM-dd HH:mm:ss", e);
        }
    }

    public static String format(Instant instant) {
        return FORMAT.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }
}
