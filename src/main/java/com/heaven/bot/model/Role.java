package com.heaven.bot.model;

public enum Role {

    SECOND("Основна страва"),
    FIRST("Суп"),
    SALAD("Салат"),
    DESERT("Десерт");

    public final String label;

    Role(String label) {
        this.label = label;
    }
}
