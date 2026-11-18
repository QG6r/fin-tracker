package com.skillbox.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.skillbox.data.model.Account;
import com.skillbox.data.model.AccountType;
import com.skillbox.data.model.Analytic;
import com.skillbox.data.model.RegularTransaction;
import com.skillbox.data.model.Transaction;
import com.skillbox.data.repository.AnalyticRepository;
import com.skillbox.service.TransactionService;
import com.skillbox.service.TransactionServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Drives the console menu by feeding keystrokes through System.in.
 * Main menu: 0 exit, 1 search, 2 group, 3 aggregation, 4 calculate, 5 save.
 * Group menu: 2 = by year, 4 = by category. Aggregation menu: 0 = sum, 2 = count.
 */
class MainMenuControllerTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final List<Analytic> saved = new ArrayList<>();

    private TransactionService service;

    @BeforeEach
    void setUp() {
        List<Account> accounts = List.of(new Account(1, AccountType.CHECKING, 100));
        List<Transaction> transactions = List.of(
                new RegularTransaction(1, 1, LocalDateTime.of(2023, 1, 5, 9, 0), "Salary", new BigDecimal("100.00")),
                new RegularTransaction(1, 2, LocalDateTime.of(2023, 6, 5, 9, 0), "Food", new BigDecimal("-40.00")),
                new RegularTransaction(1, 3, LocalDateTime.of(2024, 2, 5, 9, 0), "Food", new BigDecimal("-10.00")));
        service = new TransactionServiceImpl(() -> transactions, () -> accounts);
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void run(String keystrokes) {
        System.setIn(new ByteArrayInputStream(keystrokes.getBytes(StandardCharsets.UTF_8)));
        AnalyticRepository repository = saved::add;
        new MainMenuController(service, repository).start();
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void calculatesAndPrintsAnalyticsWithTheDefaultOptions() {
        run("4\n0\n");

        assertThat(output()).contains("Аналитика:").contains("1: 100.00").contains("3: -10.00");
    }

    @Test
    void appliesTheChosenGroupingAndAggregation() {
        run("2\n2\n3\n2\n4\n0\n");

        assertThat(output()).contains("2023: 2").contains("2024: 1");
    }

    @Test
    void groupsByCategoryWithTheSumAggregation() {
        run("2\n4\n4\n0\n");

        assertThat(output()).contains("Food: -50.00").contains("Salary: 100.00");
    }

    @Test
    void savingBeforeCalculatingIsRefused() {
        run("5\n0\n");

        assertThat(saved).isEmpty();
        assertThat(err.toString(StandardCharsets.UTF_8)).contains("Необходимо сначала рассчитать аналитику");
    }

    @Test
    void savingAfterCalculatingPassesTheResultToTheRepository() {
        run("4\n5\n0\n");

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getData()).hasSize(3);
    }

    @Test
    void anInvalidMenuChoiceIsReportedAndTheMenuIsShownAgain() {
        run("42\nabc\n0\n");

        assertThat(err.toString(StandardCharsets.UTF_8)).contains("Выбрана неверная опция!");
    }

    @Test
    void searchMenuCanLimitTheTransactionsByCategory() {
        // 1 = search menu, 2 = by category, "Food", 0 = back; then calculate and exit
        run("1\n2\nFood\n0\n4\n0\n");

        assertThat(output()).contains("2: -40.00").doesNotContain("1: 100.00");
    }
}
