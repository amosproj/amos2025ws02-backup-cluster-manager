package com.bcm.shared.controller;

import com.bcm.shared.model.api.JoinDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bn")
public class JoinController {
    private boolean hasJoined = false;
    private String cmURL;

    @PostMapping("/join")
    public Mono<ResponseEntity<String>> join(@RequestBody JoinDTO dto) {
        if (hasJoined) {
            return Mono.just(ResponseEntity.status(409).body("Already joined"));
        }

        System.out.println("Joining Cluster: " + dto.getCmURL());
        this.hasJoined = true;
        this.cmURL = dto.getCmURL();
        return Mono.just(ResponseEntity.ok("Joined Cluster"));
    }

    @PostMapping("/leave")
    public Mono<ResponseEntity<String>> leave(@RequestBody JoinDTO dto) {
        if (!hasJoined) {
            return Mono.just(ResponseEntity.status(404).body("Not currently part of a cluster to leave"));
        }

        if (dto.getCmURL() == null || !dto.getCmURL().equals(this.cmURL)) {
            return Mono.just(ResponseEntity.status(403).body("Sent CM URL does not match."));
        }

        System.out.println("Leaving Cluster: " + cmURL);
        this.hasJoined = false;
        this.cmURL = null;
        return Mono.just(ResponseEntity.ok("Left Cluster"));
    }
}
