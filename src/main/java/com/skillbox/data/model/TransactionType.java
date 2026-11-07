package com.skillbox.data.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TransactionType {
    REGULAR("Regular"),
    TAXABLE("Taxable"),
    RECURRENT("Recurrent"),
    FOREIGN_CURRENCY("ForeignCurrency"),
    COMMENTABLE("Commentable");

    private final String name;

    private static final Map<String, TransactionType> MAP = Arrays.stream(TransactionType.values())
            .collect(Collectors.toMap(TransactionType::getName, Function.identity()));

    TransactionType(String name) {
        this.name = name;
    }

    public static TransactionType of(String name) {
        return MAP.get(name);
    }

    public String getName() {
        return name;
    }
}