package com.bcm.shared.model.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("user_group_relations")
public class UserGroupRelation {
    @Column("user_id")
    private Long userId;
    @Column("group_id")
    private Long groupId;
    @Column("added_at")
    private Instant addedAt;

}
