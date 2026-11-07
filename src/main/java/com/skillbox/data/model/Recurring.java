package com.skillbox.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface Recurring {

    LocalDateTime getNextOccurrence(LocalDateTime dateTime);

    LocalDateTime getPreviousOccurrence(LocalDateTime dateTime);

    BigDecimal getTransactionAmount(LocalDateTime dateTime);

    boolean isExecutedBetween(LocalDateTime startDate, LocalDateTime endDate);
}
