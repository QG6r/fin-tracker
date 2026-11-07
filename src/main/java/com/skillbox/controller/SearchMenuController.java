package com.skillbox.controller;

import com.skillbox.controller.dto.TransactionFilterDto;
import com.skillbox.controller.option.SearchOption;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class SearchMenuController extends AbstractMenuController<SearchOption> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final SharedMenu<SearchOption> menu;
    private final Scanner input;

    public SearchMenuController(Scanner sharedScanner) {
        super(SearchOption.class, "Выберите способ поиска транзакции");
        this.input = sharedScanner;
        this.menu = new SharedMenu<>(sharedScanner, SearchOption.class, "Выберите способ поиска транзакции");
    }

    public TransactionFilterDto getTransactionFilter() {
        TransactionFilterDto filter = new TransactionFilterDto();
        while (true) {
            SearchOption option = menu.select();
            switch (option) {
                case EXIT:
                    return filter;
                case ALL_TRANSACTION:
                    return new TransactionFilterDto();
                case SEARCH_BY_CATEGORY:
                    filter = inputCategory(filter);
                    break;
                case SEARCH_BY_DATES:
                    filter = inputDates(filter);
                    break;
                case SEARCH_BY_AMOUNT:
                    filter = inputAmount(filter);
                    break;
                case SEARCH_BY_COMMENT:
                    filter = inputComment(filter);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + option);
            }
        }
    }

    private TransactionFilterDto inputComment(TransactionFilterDto filter) {
        System.out.print("Введите комментарий или его часть (Enter — для поиска всех комментариев): ");
        String value = input.next().trim();
        filter.setComment(value.isBlank() ? null : value);
        return filter;
    }

    private TransactionFilterDto inputAmount(TransactionFilterDto filter) {
        filter.setMinAmount(readAmount("Введите минимальную сумму транзакции (Enter — не ограничивать минимальную сумму): "));
        filter.setMaxAmount(readAmount("Введите максимальную сумму транзакции (Enter — не ограничивать максимальную сумму): "));
        return filter;
    }

    private TransactionFilterDto inputDates(TransactionFilterDto filter) {
        System.out.println("Формат даты: ГГГГ-ММ-ДД, например 2024-09-27.");
        filter.setStartDate(readDate("Введите начальную дату транзакции (Enter — не ограничивать начальную дату): "));
        filter.setEndDate(readDate("Введите конечную дату транзакции (Enter — не ограничивать конечную дату): "));
        return filter;
    }

    private TransactionFilterDto inputCategory(TransactionFilterDto filter) {
        System.out.print("Введите категорию (Enter — поиск по всем категориям): ");
        String value = input.next().trim();
        filter.setCategory(value.isBlank() ? null : value);
        return filter;
    }

    private BigDecimal readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = input.next().trim();
            if (value.isBlank()) {
                return null;
            }
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException exception) {
                System.err.println("Некорректная сумма, попробуйте снова.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = input.next().trim();
            if (value.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(value, DATE_FORMAT);
            } catch (DateTimeParseException exception) {
                System.err.println("Некорректная дата, используйте формат ГГГГ-ММ-ДД.");
            }
        }
    }
}