package com.bcm.shared.model.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExecuteBackupRequest {
    private Long duration;        // milliseconds
    private Boolean shouldSucceed;
    private String nodeAddress;

}