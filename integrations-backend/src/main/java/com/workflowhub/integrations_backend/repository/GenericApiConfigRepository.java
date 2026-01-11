package com.workflowhub.integrations_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workflowhub.integrations_backend.entity.GenericApiConfig;

public interface GenericApiConfigRepository
        extends JpaRepository<GenericApiConfig, Long> {

    Optional<GenericApiConfig> findByIntegrationId(Long integrationId);
}
