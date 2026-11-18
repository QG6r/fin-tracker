package com.skillbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skillbox.controller.dto.TransactionFilterDto;
import com.skillbox.controller.option.AggregateOption;
import com.skillbox.controller.option.GroupOption;
import com.skillbox.data.model.Account;
import com.skillbox.data.model.AccountType;
import com.skillbox.data.model.Analytic;
import com.skillbox.data.model.CommentableTransaction;
import com.skillbox.data.model.ForeignCurrencyTransaction;
import com.skillbox.data.model.RegularTransaction;
import com.skillbox.data.model.TaxableTransaction;
import com.skillbox.data.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Data set: user 100 owns a checking (1) and a savings (2) account, user 200 owns a credit account (3).
 * Effective amounts: 1000.00, -50.00, -30.00, 1800.00 (2000 minus 10% tax), -900.00 (-10 * 90), 25.00.
 */
class TransactionServiceImplTest {

    private List<Account> accounts;
    private List<Transaction> transactions;
    private TransactionService service;

    private static LocalDateTime at(String value) {
        return LocalDateTime.parse(value);
    }

    @BeforeEach
    void setUp() {
        accounts = List.of(
                new Account(1, AccountType.CHECKING, 100),
                new Account(2, AccountType.SAVINGS, 100),
                new Account(3, AccountType.CREDIT, 200));
        transactions = new ArrayList<>(List.of(
                new RegularTransaction(1, 1, at("2023-01-05T09:00:00"), "Salary", new BigDecimal("1000.00")),
                new RegularTransaction(1, 2, at("2023-01-20T18:00:00"), "Food", new BigDecimal("-50.00")),
                new RegularTransaction(2, 3, at("2023-02-10T12:00:00"), "Food", new BigDecimal("-30.00")),
                new TaxableTransaction(3, 4, at("2024-03-15T10:00:00"), "Salary", new BigDecimal("2000.00"),
                        new BigDecimal("0.10")),
                new ForeignCurrencyTransaction(3, 5, at("2024-03-16T10:00:00"), "Travel", new BigDecimal("-10.00"),
                        new BigDecimal("90")),
                new CommentableTransaction(1, 6, at("2024-04-01T10:00:00"), "Gift", new BigDecimal("25.00"),
                        List.of("Birthday present"))));
        service = new TransactionServiceImpl(() -> transactions, () -> accounts);
    }

    private Analytic calculate(TransactionFilterDto filter, GroupOption group, AggregateOption aggregate) {
        return service.calculateAnalytics(filter, group, aggregate);
    }

    private List<String> keys(Analytic analytic) {
        return new ArrayList<>(analytic.getData().keySet());
    }

    private BigDecimal value(Analytic analytic, String key) {
        return analytic.getData().get(key);
    }

    @Test
    void withoutGroupingEveryTransactionIsItsOwnRowSortedByNumericId() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.NO_GROUP, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("1", "2", "3", "4", "5", "6");
        assertThat(value(analytic, "4")).isEqualByComparingTo("1800.00");
        assertThat(value(analytic, "5")).isEqualByComparingTo("-900.00");
    }

    @Test
    void groupsByYear() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_YEAR, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("2023", "2024");
        assertThat(value(analytic, "2023")).isEqualByComparingTo("920.00");
        assertThat(value(analytic, "2024")).isEqualByComparingTo("925.00");
    }

    @Test
    void groupsByMonthInCalendarOrderWithRussianNames() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_MONTH, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("январь", "февраль", "март", "апрель");
        assertThat(value(analytic, "январь")).isEqualByComparingTo("950.00");
        assertThat(value(analytic, "март")).isEqualByComparingTo("900.00");
    }

    @Test
    void groupsByDayOfWeekStartingWithMonday() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_DAY_OF_WEEK, AggregateOption.COUNT);

        assertThat(keys(analytic)).containsExactly("понедельник", "четверг", "пятница", "суббота");
        assertThat(value(analytic, "пятница")).isEqualByComparingTo("3");
    }

    @Test
    void groupsByCategory() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_CATEGORY, AggregateOption.COUNT);

        assertThat(keys(analytic)).containsExactly("Food", "Gift", "Salary", "Travel");
        assertThat(value(analytic, "Food")).isEqualByComparingTo("2");
        assertThat(value(analytic, "Salary")).isEqualByComparingTo("2");
    }

    @Test
    void separatesIncomeFromExpenses() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.INCOME_EXPENSE, AggregateOption.SUM);

        assertThat(value(analytic, "Доход")).isEqualByComparingTo("2825.00");
        assertThat(value(analytic, "Расход")).isEqualByComparingTo("-980.00");
    }

    @Test
    void groupsByAccountType() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_ACCOUNT_TYPE, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("CHECKING", "CREDIT", "SAVINGS");
        assertThat(value(analytic, "CHECKING")).isEqualByComparingTo("975.00");
        assertThat(value(analytic, "CREDIT")).isEqualByComparingTo("900.00");
        assertThat(value(analytic, "SAVINGS")).isEqualByComparingTo("-30.00");
    }

    @Test
    void groupsByUser() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_USER_ID, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("100", "200");
        assertThat(value(analytic, "100")).isEqualByComparingTo("945.00");
        assertThat(value(analytic, "200")).isEqualByComparingTo("900.00");
    }

    @Test
    void transactionsOfAnUnknownAccountAreGroupedSeparately() {
        transactions = List.of(new RegularTransaction(99, 1, at("2023-01-05T09:00:00"), "X", BigDecimal.ONE));

        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_ACCOUNT_TYPE, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("неизвестный счет");
    }

    @Test
    void averageIsComputedPerGroup() {
        Analytic analytic = calculate(new TransactionFilterDto(), GroupOption.BY_USER_ID, AggregateOption.AVERAGE);

        assertThat(value(analytic, "100")).isEqualByComparingTo("236.25");
        assertThat(value(analytic, "200")).isEqualByComparingTo("450");
    }

    @Test
    void filtersByCategory() {
        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setCategory("Food");

        Analytic analytic = calculate(filter, GroupOption.NO_GROUP, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("2", "3");
    }

    @Test
    void filtersByDateRangeIncludingTheWholeLastDay() {
        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setStartDate(LocalDate.of(2024, 1, 1));
        filter.setEndDate(LocalDate.of(2024, 3, 16));

        Analytic analytic = calculate(filter, GroupOption.NO_GROUP, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("4", "5");
    }

    @Test
    void filtersByAmountUsingTheEffectiveAmount() {
        TransactionFilterDto positive = new TransactionFilterDto();
        positive.setMinAmount(BigDecimal.ZERO);
        TransactionFilterDto smallExpenses = new TransactionFilterDto();
        smallExpenses.setMinAmount(new BigDecimal("-100"));
        smallExpenses.setMaxAmount(new BigDecimal("-20"));

        assertThat(keys(calculate(positive, GroupOption.NO_GROUP, AggregateOption.SUM))).containsExactly("1", "4", "6");
        assertThat(keys(calculate(smallExpenses, GroupOption.NO_GROUP, AggregateOption.SUM))).containsExactly("2", "3");
    }

    @Test
    void filtersByCommentIgnoringCase() {
        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setComment("BIRTHDAY");

        Analytic analytic = calculate(filter, GroupOption.NO_GROUP, AggregateOption.SUM);

        assertThat(keys(analytic)).containsExactly("6");
    }

    @Test
    void filtersCanBeCombined() {
        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setCategory("Salary");
        filter.setStartDate(LocalDate.of(2024, 1, 1));

        assertThat(keys(calculate(filter, GroupOption.NO_GROUP, AggregateOption.SUM))).containsExactly("4");
    }

    @Test
    void noMatchingTransactionsGiveAnEmptyResult() {
        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setCategory("Nothing");

        Analytic analytic = calculate(filter, GroupOption.BY_YEAR, AggregateOption.SUM);

        assertThat(analytic.getData()).isEmpty();
        assertThat(analytic.toString()).contains("нет данных");
    }

    @Test
    void theResultDescribesTheChosenOptionsAndFilter() {
        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setCategory("Food");

        Analytic analytic = calculate(filter, GroupOption.BY_YEAR, AggregateOption.COUNT);

        assertThat(analytic.getGroupOption()).isEqualTo(GroupOption.BY_YEAR.getName());
        assertThat(analytic.getAggregateOption()).isEqualTo(AggregateOption.COUNT.getName());
        assertThat(analytic.getFilter()).isEqualTo("категория — Food");
    }

    @Test
    void nullArgumentsAreRejected() {
        TransactionFilterDto filter = new TransactionFilterDto();

        assertThatThrownBy(() -> service.calculateAnalytics(null, GroupOption.NO_GROUP, AggregateOption.SUM))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calculateAnalytics(filter, null, AggregateOption.SUM))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calculateAnalytics(filter, GroupOption.NO_GROUP, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
