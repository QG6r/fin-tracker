package com.skillbox.data.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum AccountType {
    CHECKING(0),

    SAVINGS(1),

    CREDIT(2);

    private final int type;

    AccountType(int type) {
        this.type = type;
    }

    private static final Map<Integer, AccountType> MAP = Arrays
            .stream(AccountType.values())
            .collect(Collectors.toMap(AccountType::getType, Function.identity()));

    public static AccountType of(int type) {
        return MAP.get(type);
    }

    public int getType() {
        return type;
    }
}
