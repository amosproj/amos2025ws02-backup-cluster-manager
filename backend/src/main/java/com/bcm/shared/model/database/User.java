package com.bcm.shared.model.database;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class User {
    private Long id;
    private String name;
    private String passwordHash;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

}
