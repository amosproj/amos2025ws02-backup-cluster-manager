package com.bcm.mybatis.mapper;

import com.bcm.mybatis.model.Backup;
import org.apache.ibatis.annotations.Mapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface BackupMapper {
    Backup findById(UUID id);
    List<Backup> findByClient(UUID clientId);
    List<Backup> findByTask(UUID taskId);
    List<Backup> findByState(String state);
    List<Backup> findBetween(Instant from, Instant to);
    int insert(Backup b);
    int update(Backup b);
    int delete(UUID id);
}
