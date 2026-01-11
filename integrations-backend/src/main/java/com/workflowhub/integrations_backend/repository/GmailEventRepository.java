package com.workflowhub.integrations_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workflowhub.integrations_backend.entity.GmailEvent;

public interface GmailEventRepository
        extends JpaRepository<GmailEvent, Long> {

    boolean existsByIntegrationIdAndMessageId(
            Long integrationId,
            String messageId
    );

    List<GmailEvent> findByIntegrationIdOrderByReceivedAtDesc(
            Long integrationId
    );
}
