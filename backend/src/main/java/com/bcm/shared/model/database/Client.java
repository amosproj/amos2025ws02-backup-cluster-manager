package com.bcm.shared.model.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Client {
    private Long id;
    private String nameOrIp;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

}
