package com.skillbox.controller;

import com.skillbox.controller.option.GroupOption;
import java.util.Scanner;

public class GroupMenuController extends AbstractMenuController<GroupOption> {

    private final SharedMenu<GroupOption> menu;

    public GroupMenuController(Scanner sharedScanner) {
        super(GroupOption.class, "Выберите опцию группировки транзакции");
        this.menu = new SharedMenu<>(sharedScanner, GroupOption.class, "Выберите опцию группировки транзакции");
    }

    public GroupOption getGroupOption() {
        return menu.select();
    }
}