package com.bcm.shared.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NodeControlResponse {
    private boolean success;
    private String message;

    public static NodeControlResponse success(String message) {
        return new NodeControlResponse(true, message);
    }

    public static NodeControlResponse error(String message) {
        return new NodeControlResponse(false, message);
    }
}

