package com.bcm.backup_node;


import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.repository.BackupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@Profile("backup_node")
public class BackupNodeController {

    @Autowired
    private BackupMapper backupMapper;


    @Autowired
    private BackupNodeService backupNodeService;

    public BackupNodeController(BackupNodeService backupNodeService) {
        this.backupNodeService = backupNodeService;
    }
    @GetMapping("/backups")
    public List<BackupDTO> getBackups() {
        List<Backup> backups = backupMapper.findAll();
        return backups.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private BackupDTO toDTO(Backup backup) {
        return new BackupDTO(
                backup.getId(),
                backup.getClientId(),
                backup.getTaskId(),
                "Backup-" + backup.getTaskId(),
                backup.getState(),
                backup.getSizeBytes(),
                backup.getStartTime() != null ?
                        LocalDateTime.ofInstant(backup.getStartTime(), ZoneOffset.UTC) : null,
                backup.getStopTime() != null ?
                        LocalDateTime.ofInstant(backup.getStopTime(), ZoneOffset.UTC) : null,
                backup.getCreatedAt() != null ?
                        LocalDateTime.ofInstant(backup.getCreatedAt(), ZoneOffset.UTC) : null,
                null
        );
    }

    @GetMapping("/backupNode/test")
    public String test(){
        return "This is a backup node endpoint";
    }

    @PostMapping("/backups/sync")
    public void receiveBackup(@RequestBody BackupDTO dto) {
        backupNodeService.storeBackup(dto);
    }
}
