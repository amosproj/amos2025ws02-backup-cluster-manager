package com.bcm.shared.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;




@Getter
@Setter
@AllArgsConstructor
public class NodeDTO {

    private Long id;
    private String name;
    private String address;
    private String status;
    private LocalDateTime createdAt;
}