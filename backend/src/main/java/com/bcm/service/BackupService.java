package com.bcm.service;

import com.bcm.domain.*;
import com.bcm.repository.BackupRepository;
import com.bcm.repository.ClientRepository;
import com.bcm.repository.TaskRepository;
import com.bcm.service.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BackupService {

    private final BackupRepository backups;
    private final ClientRepository clients;
    private final TaskRepository tasks;

    public BackupService(BackupRepository backups,
                         ClientRepository clients,
                         TaskRepository tasks) {
        this.backups = backups;
        this.clients = clients;
        this.tasks = tasks;
    }

    public Backup startBackup(UUID clientId, UUID taskId,
                              long expectedSizeBytes, String startMessage) {
        Client c = clients.findById(clientId).orElseThrow(() ->
                new NotFoundException("Client %s not found".formatted(clientId)));
        Task t = null;
        if (taskId != null) {
            t = tasks.findById(taskId).orElseThrow(() ->
                    new NotFoundException("Task %s not found".formatted(taskId)));
        }

        Backup b = new Backup();
        b.setId(UUID.randomUUID());
        b.setClient(c);
        b.setTask(t);
        b.setStartTime(Instant.now());
        b.setSizeBytes(expectedSizeBytes);
        b.setState(BackupState.RUNNING);
        b.setMessage(startMessage);
        return backups.save(b);
    }

    public Backup finishSuccess(UUID backupId, long finalSizeBytes, String message) {
        Backup b = get(backupId);
        b.setStopTime(Instant.now());
        b.setSizeBytes(finalSizeBytes);
        b.setState(BackupState.COMPLETED);
        if (message != null) b.setMessage(message);
        return backups.save(b);
    }

    public Backup finishFailure(UUID backupId, String errorMessage) {
        Backup b = get(backupId);
        b.setStopTime(Instant.now());
        b.setState(BackupState.FAILED);
        if (errorMessage != null) b.setMessage(errorMessage);
        return backups.save(b);
    }

    @Transactional(readOnly = true)
    public Backup get(UUID id) {
        return backups.findById(id).orElseThrow(() ->
                new NotFoundException("Backup %s not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Backup> listForClient(UUID clientId) {
        return backups.findByClient_IdOrderByStartTimeDesc(clientId);
    }

    @Transactional(readOnly = true)
    public List<Backup> listForTask(UUID taskId) {
        return backups.findByTask_IdOrderByStartTimeDesc(taskId);
    }
}
