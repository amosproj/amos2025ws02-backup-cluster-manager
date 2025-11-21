package com.bcm.shared.model.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;




@Getter
@Setter
@AllArgsConstructor
public class NodeDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String name;
    private String address;
    private NodeStatus status;
    private LocalDateTime createdAt;
}