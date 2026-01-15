package com.bcm.shared.controller;


//import com.bcm.shared.model.api.ClusterTablesDTO;
//import com.bcm.shared.service.LocalTablesService;
import com.bcm.shared.service.NodeControlService;
import com.bcm.shared.model.api.SyncDTO;
import com.bcm.shared.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class NodeController {

    @Autowired
    private UserService userService;

    @Autowired
    private NodeControlService nodeControlService;

    @GetMapping("/example")
    public Mono<String> test(){
        return Mono.just("Here is a string");
    }

    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("pong");
    }

    @PostMapping("/sync")
    public Mono<Void> sync(@RequestBody SyncDTO dto) {
        return Mono.fromRunnable(() -> userService.replaceUsersWithCMUsers(dto.getCmUsers()));
    }

    @PostMapping("/shutdown")
    public Mono<ResponseEntity<String>> shutdown() {
        nodeControlService.shutdown();
        return Mono.just(ResponseEntity.ok("Shutdown initiated"));
    }

    @PostMapping("/restart")
    public Mono<ResponseEntity<String>> restart() {
        nodeControlService.restart();
        return Mono.just(ResponseEntity.ok("Restart initiated"));
    }

    @GetMapping("/status")
    public Mono<ResponseEntity<NodeControlStatus>> getStatus() {
        return Mono.just(ResponseEntity.ok(new NodeControlStatus(nodeControlService.isManagedMode())));
    }
    public static class NodeControlStatus {
        private boolean managedMode;

        public NodeControlStatus(boolean managedMode) {
            this.managedMode = managedMode;
        }

        public boolean isManagedMode() {
            return managedMode;
        }

        public void setManagedMode(boolean managedMode) {
            this.managedMode = managedMode;
        }
    }
}