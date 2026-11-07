package com.skillbox.controller.option;


public enum MainMenuOption implements MenuOption {
    EXIT("выход из приложения"),
    SEARCH_CRITERIA("задать критерии поиска транзакций"),
    GROUP_OPTION("выбрать поле для группировки"),
    AGGREGATION_METHOD("выбрать функцию агрегации"),
    CALCULATE_ANALYTICS("рассчитать и вывести аналитику"),
    SAVE_ANALYTICS("сохранить аналитику");

    private final String name;

    MainMenuOption(String name) {
        this.name = name;
    }

    public static MainMenuOption of(int option) {
        return OptionUtils.of(MainMenuOption.class, option);
    }


    @Override
    public int getOption() {
        return ordinal();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
