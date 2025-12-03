package com.bcm.shared.model.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NodeMode {
    CLUSTER_MANAGER,
    BACKUP_MANAGER,
    BACKUP_NODE;

    @JsonCreator
    public static NodeMode from(String value) {
        if (value == null) return null;
        return NodeMode.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
