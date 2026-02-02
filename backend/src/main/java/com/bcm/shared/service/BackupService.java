package com.bcm.shared.service;

import com.bcm.cluster_manager.service.CMBackupService;
import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.Backup;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.bcm.shared.mapper.BackupConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.bcm.shared.mapper.BackupConverter.toDTO;

@Service
public class BackupService {

    private final BackupMapper backupMapper;
    private final CacheEventStore eventStore;

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);


    public BackupService( BackupMapper backupMapper, CacheEventStore eventStore) {

        this.backupMapper = backupMapper;
        this.eventStore = eventStore;
    }


    public Mono<BackupDTO> findBackupById(Long id) {
        return backupMapper.findById(id)
                .map(BackupConverter::toDTO);
    }
    public Mono<List<BackupDTO>> getAllBackups() {
        return backupMapper.findAll()
                .map(BackupConverter::toDTO)
                .collectList()
                .onErrorReturn(List.of());
    }

    public Mono<Void> deleteBackup(Long backupId) {
        return backupMapper.deleteById(backupId)
                .doOnSuccess(v-> eventStore.recordEvent(
                        CacheInvalidationType.BACKUP_DELETED,
                        backupId)
                );
    }

    public Mono<Void> executeBackupSync(Long id, ExecuteBackupRequest request) {
        long now = System.currentTimeMillis();

        return backupMapper.findById(id)
                .flatMap(backup -> {
                    backup.setState(BackupState.RUNNING);
                    return backupMapper.update(backup);
                }).doOnSuccess(backup ->
                eventStore.recordEvent(
                        CacheInvalidationType.BACKUP_UPDATED,
                        backup.getId()
                ))
                .then(Mono.delay(Duration.ofMillis(request.getDuration())))
                .then(backupMapper.findById(id))
                .flatMap(backup -> {
                    if (request.getShouldSucceed()) {
                        backup.setState(BackupState.COMPLETED);
                        backup.setMessage("Backup completed successfully.");
                    } else {
                        backup.setState(BackupState.FAILED);
                        backup.setMessage("Backup failed due to an error.");
                    }
                    backup.setStopTime(Instant.ofEpochMilli(now + request.getDuration()));
                    return backupMapper.update(backup).thenReturn(backup);
                })
                .doOnSuccess(backup ->
                        eventStore.recordEvent(
                                CacheInvalidationType.BACKUP_UPDATED,
                                backup.getId()
                        )
                )
                .then();
    }

    public Mono<BackupDTO> store(Long clientId, Long taskId, Long sizeBytes) {
        Backup backup = new Backup();
        backup.setClientId(clientId);
        backup.setTaskId(taskId);
        backup.setSizeBytes(sizeBytes != null ? sizeBytes : 0L);
        backup.setStartTime(Instant.now());
        backup.setState(BackupState.QUEUED);
        backup.setMessage(null);
        backup.setCreatedAt(Instant.now());

        return backupMapper.insert(backup)
                .doOnSuccess(saved ->
                        eventStore.recordEvent(
                                CacheInvalidationType.BACKUP_CREATED,
                                saved.getId()
                        )
                ).doOnError(e -> {
                    logger.info("Error storing backup for client {}", clientId);
                })
                .map(BackupConverter::toDTO);
    }

}
