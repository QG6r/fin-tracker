package com.skillbox.controller;

import com.skillbox.controller.option.MenuOption;
import com.skillbox.controller.option.OptionUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;


class SharedMenu<E extends Enum<E> & MenuOption> {

    private final Scanner scanner;
    private final Class<E> options;
    private final String description;
    private final String menu;
    private final Set<Integer> numOptions;

    SharedMenu(Scanner scanner, Class<E> options, String description) {
        this.scanner = scanner;
        this.options = options;
        this.description = description;
        this.menu = Arrays.stream(options.getEnumConstants())
                .sorted(Comparator.comparingInt(MenuOption::getOption))
                .map(MenuOption::toStringRepresentation)
                .collect(Collectors.joining(System.lineSeparator(), System.lineSeparator(), System.lineSeparator()));
        this.numOptions = Arrays.stream(options.getEnumConstants())
                .map(MenuOption::getOption)
                .collect(Collectors.toSet());
    }

    E select() {
        while (true) {
            System.out.println(description);
            System.out.println(menu);
            System.out.print("Введите нужную опцию и нажмите Enter: ");
            String token = scanner.next().trim();
            try {
                int option = Integer.parseInt(token);
                if (numOptions.contains(option)) {
                    return OptionUtils.of(options, option);
                }
            } catch (NumberFormatException ignored) {
            }
            System.err.println("Выбрана неверная опция!" + System.lineSeparator() + "Попробуйте заново." + System.lineSeparator());
        }
    }

    String nextLine() {
        return scanner.next();
    }
}