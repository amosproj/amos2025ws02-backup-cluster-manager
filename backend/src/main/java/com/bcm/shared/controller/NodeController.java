package com.bcm.shared.controller;


//import com.bcm.shared.model.api.ClusterTablesDTO;
//import com.bcm.shared.service.LocalTablesService;
import com.bcm.shared.service.NodeControlService;
import com.bcm.shared.model.api.SyncDTO;
import com.bcm.shared.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class NodeController {

    @Autowired
    private UserService userService;

    @Autowired
    private NodeControlService nodeControlService;

    @GetMapping("/example")
    public String test(){
        return "Here is a string";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping("/sync")
    public void sync(@RequestBody SyncDTO dto) {
        userService.replaceUsersWithCMUsers(dto.getCmUsers());
    }


    @PostMapping("/shutdown")
    public ResponseEntity<String> shutdown() {
        nodeControlService.shutdown();
        return ResponseEntity.ok("Shutdown initiated");
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restart() {
        nodeControlService.restart();
        return ResponseEntity.ok("Restart initiated");
    }

    @PostMapping("/disable-managed")
    public ResponseEntity<String> disableManagedMode() {
        nodeControlService.disableManagedMode();
        return ResponseEntity.ok("Managed mode disabled");
    }

    @PostMapping("/enable-managed")
    public ResponseEntity<String> enableManagedMode() {
        nodeControlService.enableManagedMode();
        return ResponseEntity.ok("Managed mode enabled");
    }

    @GetMapping("/status")
    public ResponseEntity<NodeControlStatus> getStatus() {
        return ResponseEntity.ok(new NodeControlStatus(nodeControlService.isManagedMode()));
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