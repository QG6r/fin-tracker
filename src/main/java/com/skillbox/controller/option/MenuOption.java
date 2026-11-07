package com.skillbox.controller.option;

public interface MenuOption {

    int getOption();

    String getName();

    default String toStringRepresentation() {
        return getOption() + " - " + getName();
    }

}
