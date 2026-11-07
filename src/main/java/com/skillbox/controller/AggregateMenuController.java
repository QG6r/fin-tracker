package com.skillbox.controller;

import com.skillbox.controller.option.AggregateOption;
import java.util.Scanner;

public class AggregateMenuController extends AbstractMenuController<AggregateOption> {

    private final SharedMenu<AggregateOption> menu;

    public AggregateMenuController(Scanner sharedScanner) {
        super(AggregateOption.class, "Выберите способ агрегации транзакций");
        this.menu = new SharedMenu<>(sharedScanner, AggregateOption.class, "Выберите способ агрегации транзакций");
    }

    public AggregateOption getAggregateOption() {
        return menu.select();
    }
}