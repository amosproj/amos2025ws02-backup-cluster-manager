package com.bcm.repository;

import com.bcm.domain.Backup;
import com.bcm.domain.BackupState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BackupRepository extends JpaRepository<Backup, UUID> {
    List<Backup> findByClient_IdOrderByStartTimeDesc(UUID clientId);
    List<Backup> findByTask_IdOrderByStartTimeDesc(UUID taskId);
    List<Backup> findByStateOrderByStartTimeDesc(BackupState state);
    List<Backup> findByStartTimeBetweenOrderByStartTimeDesc(Instant from, Instant to);
}
