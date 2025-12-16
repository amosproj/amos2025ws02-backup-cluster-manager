package com.bcm.shared.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;




@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NodeDTO {

    private Long id;
    private String name;
    private String address;
    private NodeStatus status;
    private NodeMode mode;
    private LocalDateTime createdAt;
}