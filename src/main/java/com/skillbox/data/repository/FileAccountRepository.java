package com.skillbox.data.repository;

import com.skillbox.data.model.Account;
import com.skillbox.data.model.AccountType;
import com.skillbox.exception.DataParseException;
import com.skillbox.exception.DataReadException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileAccountRepository implements AccountRepository {

    private static final String FIELD_DELIMITER = ",";
    private static final String COMMENT_PREFIX = "#";
    private static final int FIELD_COUNT = 3;

    private final Path path;

    public FileAccountRepository(String filename) {
        this.path = Path.of(filename);
    }

    @Override
    public List<Account> readAll() {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines.map(FileAccountRepository::stripComment)
                    .filter(line -> !line.isBlank())
                    .map(FileAccountRepository::parse)
                    .toList();
        } catch (IOException exception) {
            throw new DataReadException("Не удалось прочитать файл счетов: " + path, exception);
        }
    }

    private static Account parse(String line) {
        String[] fields = line.split(FIELD_DELIMITER);
        if (fields.length < FIELD_COUNT) {
            throw new DataParseException("Некорректная строка счета: " + line);
        }
        try {
            int accountId = Integer.parseInt(fields[0].trim());
            AccountType accountType = AccountType.of(Integer.parseInt(fields[1].trim()));
            int userId = Integer.parseInt(fields[2].trim());
            if (accountType == null) {
                throw new DataParseException("Неизвестный тип счета в строке: " + line);
            }
            return new Account(accountId, accountType, userId);
        } catch (NumberFormatException exception) {
            throw new DataParseException("Не удалось разобрать строку счета: " + line, exception);
        }
    }

    private static String stripComment(String line) {
        int index = line.indexOf(COMMENT_PREFIX);
        return (index >= 0 ? line.substring(0, index) : line).trim();
    }
}