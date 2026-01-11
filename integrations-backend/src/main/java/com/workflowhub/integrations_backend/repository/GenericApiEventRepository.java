package com.workflowhub.integrations_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workflowhub.integrations_backend.entity.GenericApiEvent;

@Repository
public interface GenericApiEventRepository
        extends JpaRepository<GenericApiEvent, Long> {

    boolean existsByIntegrationIdAndExternalId(
        Long integrationId,
        String externalId
    );

    List<GenericApiEvent> findByIntegrationIdOrderByCreatedAtDesc(
        Long integrationId
    );
}
