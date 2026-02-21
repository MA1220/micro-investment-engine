package com.blackrock.aladdin.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for transaction timestamp parsing (yyyy-MM-dd HH:mm:ss).
 */
@DisplayName("TimestampParser Tests")
class TimestampParserTest {

    @Test
    @DisplayName("shouldParseValidTimestampCorrectly")
    void shouldParseValidTimestampCorrectly() {
        // Arrange
        String timestamp = "2024-01-15 09:30:00";

        // Act
        Instant result = TimestampParser.parse(timestamp);

        // Assert
        assertNotNull(result);
        assertEquals(2024, result.atOffset(ZoneOffset.UTC).getYear());
        assertEquals(1, result.atOffset(ZoneOffset.UTC).getMonthValue());
        assertEquals(15, result.atOffset(ZoneOffset.UTC).getDayOfMonth());
        assertEquals(9, result.atOffset(ZoneOffset.UTC).getHour());
        assertEquals(30, result.atOffset(ZoneOffset.UTC).getMinute());
    }

    @Test
    @DisplayName("shouldFormatInstantToTimestamp")
    void shouldFormatInstantToTimestamp() {
        // Arrange
        Instant instant = Instant.parse("2024-06-01T12:00:00Z");

        // Act
        String result = TimestampParser.format(instant);

        // Assert
        assertEquals("2024-06-01 12:00:00", result);
    }

    @Test
    @DisplayName("shouldParseAndFormatRoundTrip")
    void shouldParseAndFormatRoundTrip() {
        String timestamp = "2024-03-10 14:00:00";
        Instant parsed = TimestampParser.parse(timestamp);
        String formatted = TimestampParser.format(parsed);
        assertEquals(timestamp, formatted);
    }

    @Test
    @DisplayName("shouldTrimWhitespaceWhenParsing")
    void shouldTrimWhitespaceWhenParsing() {
        String timestamp = "  2024-01-01 00:00:00  ";
        Instant result = TimestampParser.parse(timestamp);
        assertNotNull(result);
        assertEquals("2024-01-01 00:00:00", TimestampParser.format(result));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-13-01 00:00:00", "2024-01-32 00:00:00", "invalid", "2024/01/01 00:00:00", ""})
    @DisplayName("shouldRejectInvalidTimestamp")
    void shouldRejectInvalidTimestamp(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> TimestampParser.parse(invalid));
    }

    @Test
    @DisplayName("shouldThrowWhenTimestampIsNull")
    void shouldThrowWhenTimestampIsNull() {
        assertThrows(IllegalArgumentException.class, () -> TimestampParser.parse(null));
    }

    @Test
    @DisplayName("shouldThrowWhenTimestampIsBlank")
    void shouldThrowWhenTimestampIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> TimestampParser.parse("   "));
    }

    @Test
    @DisplayName("boundaryTimestampExactlyAtMidnight")
    void boundaryTimestampExactlyAtMidnight() {
        Instant result = TimestampParser.parse("2024-12-31 23:59:59");
        assertNotNull(result);
        assertEquals(23, result.atOffset(ZoneOffset.UTC).getHour());
        assertEquals(59, result.atOffset(ZoneOffset.UTC).getSecond());
    }
}
