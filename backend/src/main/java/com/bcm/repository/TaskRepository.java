package com.bcm.repository;

import com.bcm.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByClient_Id(UUID clientId);
    List<Task> findByEnabledTrue();
    List<Task> findByNameContainingIgnoreCase(String namePart);
}
