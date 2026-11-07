package com.skillbox.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class RecurringTransaction extends Transaction implements Recurring {

    private final RecurrencePattern pattern;
    private final int repeatCount;

    public RecurringTransaction(int accountId,
                                int transactionId,
                                LocalDateTime dateTime,
                                String category,
                                BigDecimal amount,
                                RecurrencePattern pattern,
                                int repeatCount) {
        super(accountId, transactionId, dateTime, category, amount);
        this.pattern = Objects.requireNonNull(pattern, "Паттерн повторения не может быть null");
        this.repeatCount = Math.max(repeatCount, 0);
    }

    public RecurrencePattern getPattern() {
        return pattern;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public LocalDateTime getLastOccurrence() {
        return occurrenceAt(repeatCount);
    }

    @Override
    public LocalDateTime getNextOccurrence(LocalDateTime dateTime) {
        if (dateTime == null || dateTime.isBefore(getDateTime())) {
            return getDateTime();
        }
        long executed = executedCount(dateTime);
        if (executed > repeatCount) {
            return null;
        }
        LocalDateTime next = occurrenceAt(executed);
        return next.isAfter(getLastOccurrence()) ? null : next;
    }

    @Override
    public LocalDateTime getPreviousOccurrence(LocalDateTime dateTime) {
        if (dateTime == null || dateTime.isBefore(getDateTime())) {
            return null;
        }
        long index = Math.min(executedCount(dateTime) - 1, repeatCount);
        return occurrenceAt(index);
    }

    @Override
    public BigDecimal getTransactionAmount(LocalDateTime dateTime) {
        if (dateTime == null || dateTime.isBefore(getDateTime())) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING);
        }
        long executed = Math.min(executedCount(dateTime), repeatCount + 1L);
        return scale(getAmount().multiply(BigDecimal.valueOf(executed)));
    }

    @Override
    public boolean isExecutedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime first = getDateTime();
        LocalDateTime last = getLastOccurrence();
        if (endDate != null && first.isAfter(endDate)) {
            return false;
        }
        if (startDate != null && last.isBefore(startDate)) {
            return false;
        }
        if (startDate == null) {
            return true;
        }
        LocalDateTime candidate = getNextOccurrence(startDate.minusNanos(1));
        return candidate != null && (endDate == null || !candidate.isAfter(endDate));
    }

    private LocalDateTime occurrenceAt(long index) {
        return getDateTime().plus(pattern.getDuration().multipliedBy(index));
    }

    private long executedCount(LocalDateTime dateTime) {
        long periodSeconds = pattern.getDuration().getSeconds();
        long elapsedSeconds = java.time.Duration.between(getDateTime(), dateTime).getSeconds();
        return elapsedSeconds / periodSeconds + 1;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.RECURRENT;
    }
}