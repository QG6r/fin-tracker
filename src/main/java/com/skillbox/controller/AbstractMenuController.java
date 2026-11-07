package com.skillbox.controller;

import com.skillbox.controller.option.MenuOption;
import com.skillbox.controller.option.OptionUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractMenuController<E extends Enum<E> & MenuOption> {

    protected final Scanner scanner;
    private final Set<Integer> numOptions;
    private final String description;
    private final String menu;
    private final Class<E> options;

    protected AbstractMenuController(Class<E> options, String description) {
        this.options = options;
        this.menu = toMenu(options);
        this.numOptions = Arrays.stream(options.getEnumConstants())
                .map(MenuOption::getOption)
                .collect(Collectors.toSet());
        this.description = description;
        this.scanner = new Scanner(System.in).useDelimiter("\\R");
    }

    protected E selectMenu() {
        int option;

        while (true) {
            System.out.println(description);
            System.out.println(menu);
            System.out.print("Введите нужную опцию и нажмите Enter: ");

            option = scanner.nextInt();
            if (numOptions.contains(option)) {
                break;
            }
            System.err.println("Выбрана неверная опция!\n"
                    + "Попробуйте заново.\n");
        }
        return OptionUtils.of(options, option);
    }

    private String toMenu(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .sorted(Comparator.comparingInt(MenuOption::getOption))
                .map(MenuOption::toStringRepresentation)
                .collect(Collectors.joining(System.lineSeparator(), System.lineSeparator(), System.lineSeparator()));
    }

}
