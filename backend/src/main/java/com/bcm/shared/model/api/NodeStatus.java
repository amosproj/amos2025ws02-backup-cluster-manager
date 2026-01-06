package com.bcm.shared.model.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NodeStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    SHUTTING_DOWN,
    RESTARTING;

    @JsonCreator
    public static NodeStatus from(String value) {
        if (value == null) return null;
        return NodeStatus.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
