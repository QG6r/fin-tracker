package com.skillbox.data.model;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum RecurrencePattern {
    HOURLY("hourly", Duration.ofHours(1)),
    DAILY("daily", Duration.ofDays(1)),
    WEEKLY("weekly", Duration.ofDays(7)),
    BIWEEKLY("biweekly", Duration.ofDays(14)),
    MONTHLY("monthly", Duration.ofDays(30)),
    YEARLY("yearly", Duration.ofDays(365));

    private final String pattern;
    private final Duration duration;

    private static final Map<String, RecurrencePattern> MAP = Arrays.stream(RecurrencePattern.values()).collect(Collectors.toMap(RecurrencePattern::getPattern, Function.identity()));
    RecurrencePattern(String pattern, Duration duration) {
        this.pattern = pattern;
        this.duration = duration;
    }

    public static RecurrencePattern of(String recurrencePattern) {
        return MAP.get(recurrencePattern);
    }

    public String getPattern() {
        return pattern;
    }

    public Duration getDuration() {
        return duration;
    }
}
