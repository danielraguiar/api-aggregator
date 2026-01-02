package com.kenect.api_aggregator.model;

public enum ContactSource {
    KENECT_LABS("KENECT_LABS");

    private final String value;

    ContactSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
