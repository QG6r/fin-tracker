package com.skillbox.data.repository;

import com.skillbox.data.model.Transaction;
import com.skillbox.exception.DataReadException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileTransactionRepository implements TransactionRepository {

    private static final String COMMENT_PREFIX = "#";

    private final Path path;

    public FileTransactionRepository(String filename) {
        this.path = Path.of(filename);
    }

    @Override
    public List<Transaction> readAll() {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines.map(FileTransactionRepository::stripComment)
                    .filter(line -> !line.isBlank())
                    .map(TransactionParser::parse)
                    .toList();
        } catch (IOException exception) {
            throw new DataReadException("Не удалось прочитать файл транзакций: " + path, exception);
        }
    }

    private static String stripComment(String line) {
        int index = line.indexOf(COMMENT_PREFIX);
        return (index >= 0 ? line.substring(0, index) : line).trim();
    }
}