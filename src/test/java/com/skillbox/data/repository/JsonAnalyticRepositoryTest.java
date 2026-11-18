package com.skillbox.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbox.data.model.Analytic;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonAnalyticRepositoryTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir
    Path directory;

    private static Analytic analytic(String group) {
        return new Analytic(LocalDateTime.of(2024, 5, 1, 10, 0), group, "sum", "category: Food",
                Map.of("2023", new BigDecimal("920.00")));
    }

    @Test
    void createsTheFileAndAppendsEveryResultToAJsonArray() throws IOException {
        Path file = directory.resolve("analytics.json");
        JsonAnalyticRepository repository = new JsonAnalyticRepository(file.toString());

        repository.save(analytic("by year"));
        repository.save(analytic("by category"));

        JsonNode records = mapper.readTree(Files.readString(file, StandardCharsets.UTF_8));
        assertThat(records.isArray()).isTrue();
        assertThat(records).hasSize(2);
        assertThat(records.get(0).get("groupOption").asText()).isEqualTo("by year");
        assertThat(records.get(1).get("groupOption").asText()).isEqualTo("by category");
        assertThat(records.get(0).get("filter").asText()).isEqualTo("category: Food");
        assertThat(records.get(0).get("data").get("2023").decimalValue()).isEqualByComparingTo("920.00");
    }

    @Test
    void savingNothingDoesNotCreateAFile() {
        Path file = directory.resolve("analytics.json");

        new JsonAnalyticRepository(file.toString()).save(null);

        assertThat(file).doesNotExist();
    }

    @Test
    void anUnreadableExistingFileIsReplacedInsteadOfFailing() throws IOException {
        Path file = directory.resolve("analytics.json");
        Files.writeString(file, "this is not json", StandardCharsets.UTF_8);

        new JsonAnalyticRepository(file.toString()).save(analytic("by year"));

        assertThat(mapper.readTree(Files.readString(file, StandardCharsets.UTF_8))).hasSize(1);
    }
}
