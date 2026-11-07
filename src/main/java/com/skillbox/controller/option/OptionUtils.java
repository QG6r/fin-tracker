package com.skillbox.controller.option;

import java.util.Arrays;

public final class OptionUtils {

    private OptionUtils() {
    }

    public static <E extends Enum<E> & MenuOption> E of(Class<E> enumClass, int option) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getOption() == option)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with option: " + option));
    }
}
