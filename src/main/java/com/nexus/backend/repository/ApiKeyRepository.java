package com.nexus.backend.repository;

import com.nexus.backend.model.ApiKey;
import com.nexus.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);
    List<ApiKey> findByOwner(User owner);
    boolean existsByKeyValueAndActiveTrue(String keyValue);
}