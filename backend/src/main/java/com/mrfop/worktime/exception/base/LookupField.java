package com.mrfop.worktime.exception.base;

public enum LookupField {
    ID("id"),
    NAME("name"),
    FROM("from"),
    TO("to"),
    TZ("tz"),
    LOCALE("locale");

    private final String wireName;

    LookupField(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }
}