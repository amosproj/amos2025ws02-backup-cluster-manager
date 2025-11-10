package com.bcm.repository;

import com.bcm.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    // Must use Query else Spring Data will split findByNameOrIp into findByName and Ip resulting in error
    @Query("select c from Client c where c.nameOrIp = :value")
    Optional<Client> findByNameOrIp(@Param("value") String value);
    List<Client> findByEnabledTrue();
}
