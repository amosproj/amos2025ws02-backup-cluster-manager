package com.bcm.shared.repository;

import com.bcm.shared.model.database.Backup;
import org.apache.ibatis.annotations.Mapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface BackupMapper {

    Backup findById(Long id);
    List<Backup> findByClient(Long clientId);
    List<Backup> findByTask(Long taskId);
    List<Backup> findByState(String state);
    List<Backup> findBetween(Instant from, Instant to);
    int insert(Backup b);
    int update(Backup b);
    int delete(Long id);
}
