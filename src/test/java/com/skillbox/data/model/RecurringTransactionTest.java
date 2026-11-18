package com.skillbox.data.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** A daily transaction of 10.00 starting on 2023-01-01 that repeats twice: it happens on Jan 1, 2 and 3. */
class RecurringTransactionTest {

    private final RecurringTransaction transaction = new RecurringTransaction(1, 1,
            LocalDateTime.of(2023, 1, 1, 0, 0), "Subscription", new BigDecimal("10.00"), RecurrencePattern.DAILY, 2);

    @Test
    void lastOccurrenceIsTheStartPlusAllRepeats() {
        assertThat(transaction.getLastOccurrence()).isEqualTo(LocalDateTime.of(2023, 1, 3, 0, 0));
    }

    @Test
    void amountGrowsWithEveryExecutionAndStopsAfterTheLastOne() {
        assertThat(transaction.getTransactionAmount(LocalDateTime.of(2022, 12, 31, 0, 0))).isEqualByComparingTo("0.00");
        assertThat(transaction.getTransactionAmount(LocalDateTime.of(2023, 1, 2, 12, 0))).isEqualByComparingTo("20.00");
        assertThat(transaction.getTransactionAmount(LocalDateTime.of(2023, 2, 1, 0, 0))).isEqualByComparingTo("30.00");
    }

    @Test
    void nextOccurrenceIsFoundAndBecomesNullAfterTheLastRepeat() {
        assertThat(transaction.getNextOccurrence(LocalDateTime.of(2023, 1, 1, 12, 0)))
                .isEqualTo(LocalDateTime.of(2023, 1, 2, 0, 0));
        assertThat(transaction.getNextOccurrence(LocalDateTime.of(2022, 12, 1, 0, 0)))
                .isEqualTo(LocalDateTime.of(2023, 1, 1, 0, 0));
        assertThat(transaction.getNextOccurrence(LocalDateTime.of(2023, 1, 3, 12, 0))).isNull();
    }

    @Test
    void previousOccurrenceIsNullBeforeTheStart() {
        assertThat(transaction.getPreviousOccurrence(LocalDateTime.of(2023, 1, 2, 12, 0)))
                .isEqualTo(LocalDateTime.of(2023, 1, 2, 0, 0));
        assertThat(transaction.getPreviousOccurrence(LocalDateTime.of(2022, 12, 31, 0, 0))).isNull();
    }

    @Test
    void isExecutedBetweenChecksTheWholeLifetimeOfTheSeries() {
        assertThat(transaction.isExecutedBetween(LocalDateTime.of(2023, 1, 2, 0, 0), LocalDateTime.of(2023, 1, 2, 23, 0))).isTrue();
        assertThat(transaction.isExecutedBetween(LocalDateTime.of(2023, 1, 5, 0, 0), LocalDateTime.of(2023, 1, 10, 0, 0))).isFalse();
        assertThat(transaction.isExecutedBetween(null, LocalDateTime.of(2022, 12, 1, 0, 0))).isFalse();
    }

    @Test
    void negativeRepeatCountIsTreatedAsZero() {
        RecurringTransaction once = new RecurringTransaction(1, 2, LocalDateTime.of(2023, 1, 1, 0, 0), "X",
                BigDecimal.ONE, RecurrencePattern.WEEKLY, -5);

        assertThat(once.getRepeatCount()).isZero();
        assertThat(once.getLastOccurrence()).isEqualTo(once.getDateTime());
    }
}
