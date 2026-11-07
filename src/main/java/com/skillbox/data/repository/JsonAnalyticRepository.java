package com.skillbox.data.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skillbox.data.model.Analytic;
import com.skillbox.exception.DataSaveException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonAnalyticRepository implements AnalyticRepository {

    private final Path path;
    private final ObjectMapper mapper;

    public JsonAnalyticRepository(String filename) {
        this.path = Path.of(filename);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void save(Analytic analytic) {
        if (analytic == null) {
            return;
        }
        try {
            ArrayNode records = readExisting();
            records.add(mapper.valueToTree(analytic));
            Files.writeString(path, mapper.writeValueAsString(records), StandardCharsets.UTF_8);
            System.out.println("Аналитика сохранена в файл " + path);
        } catch (IOException exception) {
            throw new DataSaveException("Не удалось сохранить аналитику в файл: " + path, exception);
        }
    }

    private ArrayNode readExisting() {
        if (!Files.exists(path)) {
            return mapper.createArrayNode();
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return mapper.createArrayNode();
            }
            JsonNode root = mapper.readTree(content);
            if (root.isArray()) {
                return (ArrayNode) root;
            }
            ArrayNode records = mapper.createArrayNode();
            records.add(root);
            return records;
        } catch (IOException exception) {
            System.err.println("Не удалось прочитать предыдущие результаты, файл будет перезаписан: "
                    + exception.getMessage());
            return mapper.createArrayNode();
        }
    }
}