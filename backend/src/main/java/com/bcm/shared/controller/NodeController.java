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

/**
 * REST controller for backup node: ping, sync, shutdown, restart, status.
 */
@RestController
@RequestMapping("/api/v1")
public class NodeController {

    @Autowired
    private UserService userService;

    @Autowired
    private NodeControlService nodeControlService;

    /**
     * Example/test endpoint.
     *
     * @return test message
     */
    @GetMapping("/example")
    public Mono<String> test(){
        return Mono.just("Here is a string");
    }

    /**
     * Health check endpoint.
     *
     * @return "pong"
     */
    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("pong");
    }

    /**
     * Syncs users from the cluster manager (replace local users with CM users).
     *
     * @param dto sync DTO containing list of CM users
     * @return completion when sync is done
     */
    @PostMapping("/sync")
    public Mono<Void> sync(@RequestBody SyncDTO dto) {
        return Mono.fromRunnable(() -> userService.replaceUsersWithCMUsers(dto.getCmUsers()));
    }

    /**
     * Initiates shutdown of this node.
     *
     * @return 200 with message
     */
    @PostMapping("/shutdown")
    public Mono<ResponseEntity<String>> shutdown() {
        nodeControlService.shutdown();
        return Mono.just(ResponseEntity.ok("Shutdown initiated"));
    }

    /**
     * Initiates restart of this node.
     *
     * @return 200 with message
     */
    @PostMapping("/restart")
    public Mono<ResponseEntity<String>> restart() {
        nodeControlService.restart();
        return Mono.just(ResponseEntity.ok("Restart initiated"));
    }

    /**
     * Returns this node's control status (e.g. managed mode).
     *
     * @return 200 with status DTO
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<NodeControlStatus>> getStatus() {
        return Mono.just(ResponseEntity.ok(new NodeControlStatus(nodeControlService.isManagedMode())));
    }

    /** DTO for node control status. */
    public static class NodeControlStatus {
        private boolean managedMode;

        /** Creates status with the given managed mode. */
        public NodeControlStatus(boolean managedMode) {
            this.managedMode = managedMode;
        }

        /** Whether the node is in managed mode. */
        public boolean isManagedMode() {
            return managedMode;
        }

        /** Sets managed mode. */
        public void setManagedMode(boolean managedMode) {
            this.managedMode = managedMode;
        }
    }
}