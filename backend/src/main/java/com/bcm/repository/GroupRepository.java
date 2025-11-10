package com.bcm.repository;

import com.bcm.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByEnabledTrue();
    List<Group> findByNameContainingIgnoreCase(String namePart);
}
