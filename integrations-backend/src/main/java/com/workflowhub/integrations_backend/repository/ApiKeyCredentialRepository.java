package com.workflowhub.integrations_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workflowhub.integrations_backend.entity.ApiKeyCredential;

@Repository
public interface ApiKeyCredentialRepository extends JpaRepository<ApiKeyCredential, Long> {
    Optional<ApiKeyCredential> findByIntegrationId(Long integrationId);
    void deleteByIntegrationId(Long integrationId);
}
