package com.bcm.shared.model.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserGroupRelation {
    private Long userId;
    private Long groupId;
    private Instant addedAt;

}
