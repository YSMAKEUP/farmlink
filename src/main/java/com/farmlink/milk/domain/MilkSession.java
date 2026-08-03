package com.farmlink.milk.domain;

import lombok.Getter;

@Getter
public enum MilkSession {
    MORNING("아침"),
    EVENING("저녁");

    private final String label;

    MilkSession(String label) {
        this.label = label;
    }
}