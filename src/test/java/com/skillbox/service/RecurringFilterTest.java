package com.skillbox.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.skillbox.controller.dto.TransactionFilterDto;
import com.skillbox.controller.option.AggregateOption;
import com.skillbox.controller.option.GroupOption;
import com.skillbox.data.model.Account;
import com.skillbox.data.model.AccountType;
import com.skillbox.data.model.RecurrencePattern;
import com.skillbox.data.model.RecurringTransaction;
import com.skillbox.data.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** A recurring transaction must match a date filter when any of its repeats falls into the range. */
class RecurringFilterTest {

    private final List<Transaction> transactions = List.of(new RecurringTransaction(1, 1,
            LocalDateTime.of(2023, 1, 1, 0, 0), "Subscription", new BigDecimal("10.00"), RecurrencePattern.DAILY, 2));
    private final TransactionService service = new TransactionServiceImpl(
            () -> transactions, () -> List.of(new Account(1, AccountType.CHECKING, 100)));

    private int matches(LocalDate start, LocalDate end) {
        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setStartDate(start);
        filter.setEndDate(end);
        return service.calculateAnalytics(filter, GroupOption.NO_GROUP, AggregateOption.COUNT).getData().size();
    }

    @Test
    void matchesWhenALaterRepeatFallsIntoTheRange() {
        assertThat(matches(LocalDate.of(2023, 1, 2), LocalDate.of(2023, 1, 2))).isEqualTo(1);
    }

    @Test
    void doesNotMatchAfterTheLastRepeat() {
        assertThat(matches(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 20))).isZero();
    }
}
