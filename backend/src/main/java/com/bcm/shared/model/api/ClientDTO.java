package com.bcm.shared.model.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDTO {
    private Long id;
    private String nameOrIp;
    private boolean enabled;
}
