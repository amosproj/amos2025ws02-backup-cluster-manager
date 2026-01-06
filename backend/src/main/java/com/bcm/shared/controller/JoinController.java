package com.bcm.shared.controller;

import com.bcm.shared.model.api.JoinDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bn")
public class JoinController {
    private boolean hasJoined = false;
    private String cmURL;

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody JoinDTO dto) {
        if (hasJoined) return ResponseEntity.status(409).body("Already joined");

        System.out.println("Joining Cluster: " + dto.getCmURL());
        this.hasJoined = true;
        this.cmURL = dto.getCmURL();
        return ResponseEntity.ok("Joined Cluster");
    }
}
