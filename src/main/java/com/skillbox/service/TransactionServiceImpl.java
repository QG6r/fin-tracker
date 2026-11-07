package com.skillbox.service;

import com.skillbox.controller.dto.TransactionFilterDto;
import com.skillbox.controller.option.AggregateOption;
import com.skillbox.controller.option.GroupOption;
import com.skillbox.data.model.Account;
import com.skillbox.data.model.Analytic;
import com.skillbox.data.model.Transaction;
import com.skillbox.data.repository.AccountRepository;
import com.skillbox.data.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TransactionServiceImpl implements TransactionService {

    private static final Locale LOCALE = Locale.forLanguageTag("ru");
    private static final int AVERAGE_SCALE = 10;
    private static final String INCOME = "Доход";
    private static final String EXPENSE = "Расход";
    private static final String UNKNOWN_ACCOUNT = "неизвестный счет";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Analytic calculateAnalytics(TransactionFilterDto transactionFilter,
                                       GroupOption groupOption,
                                       AggregateOption aggregateOption) {
        if (transactionFilter == null || groupOption == null || aggregateOption == null) {
            throw new IllegalArgumentException("Фильтр, опция группировки и функция агрегации не могут быть null");
        }
        List<Transaction> transactions = transactionRepository.readAll();
        Map<Integer, Account> accounts = loadAccounts(transactions);

        Map<String, BigDecimal> data = transactions.stream()
                .filter(transactionFilter.buildPredicate())
                .collect(Collectors.groupingBy(
                        classifier(groupOption, accounts),
                        () -> new TreeMap<>(keyComparator(groupOption)),
                        collector(aggregateOption)));

        return new Analytic(LocalDateTime.now(), groupOption.getName(), aggregateOption.getName(),
                transactionFilter.toString(), data);
    }

    private Map<Integer, Account> loadAccounts(List<Transaction> transactions) {
        Map<Integer, Account> accounts = accountRepository.readAll().stream()
                .collect(Collectors.toMap(Account::getAccountId, Function.identity(), (first, second) -> first));
        transactions.forEach(transaction -> {
            Account account = accounts.get(transaction.getAccountId());
            if (account != null) {
                account.addTransaction(transaction);
            }
        });
        return accounts;
    }

    private Function<Transaction, String> classifier(GroupOption option, Map<Integer, Account> accounts) {
        return switch (option) {
            case NO_GROUP -> transaction -> String.valueOf(transaction.getTransactionId());
            case BY_MONTH -> transaction -> transaction.getDateTime().getMonth()
                    .getDisplayName(TextStyle.FULL_STANDALONE, LOCALE);
            case BY_YEAR -> transaction -> String.valueOf(transaction.getDateTime().getYear());
            case BY_DAY_OF_WEEK -> transaction -> transaction.getDateTime().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL_STANDALONE, LOCALE);
            case BY_CATEGORY -> Transaction::getCategory;
            case INCOME_EXPENSE -> transaction ->
                    transaction.getEffectiveAmount().signum() >= 0 ? INCOME : EXPENSE;
            case BY_ACCOUNT_TYPE -> transaction -> account(accounts, transaction)
                    .map(account -> account.getAccountType().name())
                    .orElse(UNKNOWN_ACCOUNT);
            case BY_USER_ID -> transaction -> account(accounts, transaction)
                    .map(account -> String.valueOf(account.getUserId()))
                    .orElse(UNKNOWN_ACCOUNT);
        };
    }

    private Comparator<String> keyComparator(GroupOption option) {
        return switch (option) {
            case NO_GROUP, BY_YEAR, BY_USER_ID -> Comparator.comparing(TransactionServiceImpl::asNumber)
                    .thenComparing(Comparator.naturalOrder());
            case BY_MONTH -> Comparator.comparingInt(key -> indexOfMonth((String) key));
            case BY_DAY_OF_WEEK -> Comparator.comparingInt(key -> indexOfDayOfWeek((String) key));
            default -> Comparator.naturalOrder();
        };
    }

    private Collector<Transaction, ?, BigDecimal> collector(AggregateOption option) {
        return switch (option) {
            case SUM -> Collectors.collectingAndThen(sum(), TransactionServiceImpl::scaleMoney);
            case COUNT -> Collectors.collectingAndThen(Collectors.counting(), BigDecimal::valueOf);
            case AVERAGE -> Collectors.collectingAndThen(Collectors.toList(), TransactionServiceImpl::average);
        };
    }

    private static Collector<Transaction, ?, BigDecimal> sum() {
        return Collectors.reducing(BigDecimal.ZERO, Transaction::getEffectiveAmount, BigDecimal::add);
    }

    private static BigDecimal average(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = transactions.stream()
                .map(Transaction::getEffectiveAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(transactions.size()), AVERAGE_SCALE, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    private static BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(Transaction.MONEY_SCALE, Transaction.MONEY_ROUNDING);
    }

    private static Optional<Account> account(Map<Integer, Account> accounts, Transaction transaction) {
        return Optional.ofNullable(accounts.get(transaction.getAccountId()));
    }

    private static long asNumber(String key) {
        try {
            return Long.parseLong(key);
        } catch (NumberFormatException exception) {
            return Long.MAX_VALUE;
        }
    }

    private static int indexOfMonth(String key) {
        for (Month month : Month.values()) {
            if (month.getDisplayName(TextStyle.FULL_STANDALONE, LOCALE).equals(key)) {
                return month.ordinal();
            }
        }
        return Integer.MAX_VALUE;
    }

    private static int indexOfDayOfWeek(String key) {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.getDisplayName(TextStyle.FULL_STANDALONE, LOCALE).equals(key)) {
                return day.ordinal();
            }
        }
        return Integer.MAX_VALUE;
    }
}