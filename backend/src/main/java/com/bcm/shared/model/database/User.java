package com.bcm.shared.model.database;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("users")
public class User {
    @Id
    private Long id;
    @Column("name")
    private String name;
    @Column("password_hash")
    private String passwordHash;
    @Column("enabled")
    private boolean enabled;
    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;

}
