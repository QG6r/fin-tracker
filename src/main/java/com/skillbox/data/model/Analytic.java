package com.skillbox.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Analytic {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LINE = "===================================";
    private static final String SUB_LINE = "-----------------------------------";

    private final LocalDateTime date;
    private final String groupOption;
    private final String aggregateOption;
    private final String filter;
    private final Map<String, BigDecimal> data;

    public Analytic(LocalDateTime date,
                    String groupOption,
                    String aggregateOption,
                    String filter,
                    Map<String, BigDecimal> data) {
        this.date = date == null ? LocalDateTime.now() : date;
        this.groupOption = groupOption == null ? "без группировки" : groupOption;
        this.aggregateOption = aggregateOption == null ? "подсчет суммы" : aggregateOption;
        this.filter = filter == null ? "" : filter;
        this.data = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getGroupOption() {
        return groupOption;
    }

    public String getAggregateOption() {
        return aggregateOption;
    }

    public String getFilter() {
        return filter;
    }

    public Map<String, BigDecimal> getData() {
        return Collections.unmodifiableMap(data);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(LINE).append(System.lineSeparator())
                .append("Дата: ").append(DATE_FORMAT.format(date)).append(System.lineSeparator())
                .append("Имя: '").append(groupOption).append(" (").append(aggregateOption).append(")'")
                .append(System.lineSeparator())
                .append("Фильтр: ").append(filter.isBlank() ? "не задан" : filter).append(System.lineSeparator())
                .append(SUB_LINE).append(System.lineSeparator())
                .append("Аналитика:").append(System.lineSeparator());
        if (data.isEmpty()) {
            builder.append("нет данных, удовлетворяющих условиям поиска").append(System.lineSeparator());
        } else {
            data.forEach((key, value) -> builder.append(key).append(": ")
                    .append(value.toPlainString()).append(System.lineSeparator()));
        }
        return builder.append(LINE).toString();
    }
}