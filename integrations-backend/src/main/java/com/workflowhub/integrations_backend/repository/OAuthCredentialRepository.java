package com.workflowhub.integrations_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workflowhub.integrations_backend.entity.OAuthCredential;

@Repository
public interface OAuthCredentialRepository extends JpaRepository<OAuthCredential, Long> {
     Optional<OAuthCredential> findByIntegrationId(Long integrationId);
     void deleteByIntegrationId(Long integrationId);

    
}
