package com.bcm.shared.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEventDTO {
    private Long id;
    private CacheInvalidationType type;
    private Long entityId;
    private Instant timestamp;
}