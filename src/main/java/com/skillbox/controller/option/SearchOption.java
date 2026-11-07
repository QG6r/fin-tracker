package com.skillbox.controller.option;

public enum SearchOption implements MenuOption {
    EXIT("сохранить поиск и вернуться назад"),
    ALL_TRANSACTION("выбрать все транзакции (сбросит все ранее заданные фильтры)"),
    SEARCH_BY_CATEGORY("поиск по категориям"),
    SEARCH_BY_DATES("поиск по диапазону дат"),
    SEARCH_BY_AMOUNT("поиск по диапазону суммы транзакций"),
    SEARCH_BY_COMMENT("поиск по комментарию (для транзакций, поддерживающих комментарии)");

    private final String name;

    SearchOption(String name) {
        this.name = name;
    }

    public static SearchOption of(int option) {
        return OptionUtils.of(SearchOption.class, option);
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
