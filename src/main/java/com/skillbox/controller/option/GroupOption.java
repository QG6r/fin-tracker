package com.skillbox.controller.option;

public enum GroupOption implements MenuOption {
    NO_GROUP("вернуться назад (без группировки)"),
    BY_MONTH("группировать по месяцам"),
    BY_YEAR("группировать по годам"),
    BY_DAY_OF_WEEK("группировать по дню недели"),
    BY_CATEGORY("группировать по категории"),
    INCOME_EXPENSE("считать доходы и расходы"),
    BY_ACCOUNT_TYPE("группировать по типу счета"),
    BY_USER_ID("группировать по ID пользователя");

    private final String name;

    GroupOption(String name) {
        this.name = name;
    }

    public static GroupOption of(int option) {
        return OptionUtils.of(GroupOption.class, option);
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