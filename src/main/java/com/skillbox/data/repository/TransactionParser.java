package com.skillbox.data.repository;

import com.skillbox.data.model.CommentableTransaction;
import com.skillbox.data.model.ForeignCurrencyTransaction;
import com.skillbox.data.model.RecurrencePattern;
import com.skillbox.data.model.RecurringTransaction;
import com.skillbox.data.model.RegularTransaction;
import com.skillbox.data.model.TaxableTransaction;
import com.skillbox.data.model.Transaction;
import com.skillbox.data.model.TransactionType;
import com.skillbox.exception.DataParseException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

final class TransactionParser {

    private static final String FIELD_DELIMITER = ",";
    private static final String EXTRA_DELIMITER = ";";
    private static final int FIELD_COUNT = 7;

    private TransactionParser() {
    }

    static Transaction parse(String line) {
        String[] fields = line.split(FIELD_DELIMITER, FIELD_COUNT);
        if (fields.length < FIELD_COUNT - 1) {
            throw new DataParseException("Некорректная строка транзакции: " + line);
        }
        try {
            int accountId = Integer.parseInt(fields[0].trim());
            int transactionId = Integer.parseInt(fields[1].trim());
            LocalDateTime dateTime = LocalDateTime.parse(fields[2].trim(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String category = fields[3].trim();
            BigDecimal amount = new BigDecimal(fields[4].trim());
            TransactionType type = TransactionType.of(fields[5].trim());
            String extra = fields.length == FIELD_COUNT ? fields[6].trim() : "";

            if (type == null) {
                throw new DataParseException("Неизвестный тип транзакции: " + fields[5].trim());
            }
            return create(type, accountId, transactionId, dateTime, category, amount, extra, line);
        } catch (DataParseException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new DataParseException("Не удалось разобрать строку транзакции: " + line, exception);
        }
    }

    private static Transaction create(TransactionType type,
                                      int accountId,
                                      int transactionId,
                                      LocalDateTime dateTime,
                                      String category,
                                      BigDecimal amount,
                                      String extra,
                                      String line) {
        return switch (type) {
            case REGULAR -> new RegularTransaction(accountId, transactionId, dateTime, category, amount);
            case TAXABLE -> new TaxableTransaction(accountId, transactionId, dateTime, category, amount,
                    parseTaxRate(extra, line));
            case FOREIGN_CURRENCY -> new ForeignCurrencyTransaction(accountId, transactionId, dateTime, category,
                    amount, parseRate(extra, line));
            case COMMENTABLE -> new CommentableTransaction(accountId, transactionId, dateTime, category, amount,
                    parseComments(extra));
            case RECURRENT -> parseRecurring(accountId, transactionId, dateTime, category, amount, extra, line);
        };
    }

    private static BigDecimal parseTaxRate(String extra, String line) {
        if (extra.isBlank()) {
            throw new DataParseException("Не указана ставка налога в строке: " + line);
        }
        return new BigDecimal(extra.trim());
    }

    private static BigDecimal parseRate(String extra, String line) {
        if (extra.isBlank()) {
            throw new DataParseException("Не указан курс валюты в строке: " + line);
        }
        return new BigDecimal(extra.trim());
    }

    private static List<String> parseComments(String extra) {
        if (extra.isBlank()) {
            return List.of();
        }
        return Arrays.stream(extra.split(EXTRA_DELIMITER))
                .map(String::trim)
                .filter(comment -> !comment.isEmpty())
                .toList();
    }

    private static Transaction parseRecurring(int accountId,
                                              int transactionId,
                                              LocalDateTime dateTime,
                                              String category,
                                              BigDecimal amount,
                                              String extra,
                                              String line) {
        String[] parts = extra.split(EXTRA_DELIMITER);
        if (parts.length < 2) {
            throw new DataParseException("Для повторяющейся транзакции нужны паттерн и количество повторений: " + line);
        }
        RecurrencePattern pattern = RecurrencePattern.of(parts[0].trim());
        if (pattern == null) {
            throw new DataParseException("Неизвестный паттерн повторения: " + parts[0].trim());
        }
        int repeatCount = Integer.parseInt(parts[1].trim());
        return new RecurringTransaction(accountId, transactionId, dateTime, category, amount, pattern, repeatCount);
    }
}