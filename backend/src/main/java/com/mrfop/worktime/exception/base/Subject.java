package com.mrfop.worktime.exception.base;

public enum Subject {
    ACTIVITY("activity"),
    CATEGORY("category"),
    WORK_SEGMENT("work segment"),
    WORK_SESSION("work session"),
    WORK_REPORT("work report");
    
    private final String label;

    Subject(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}