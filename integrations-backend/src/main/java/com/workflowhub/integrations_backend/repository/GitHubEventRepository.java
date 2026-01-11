package com.workflowhub.integrations_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workflowhub.integrations_backend.entity.GitHubEvent;

public interface GitHubEventRepository
        extends JpaRepository<GitHubEvent, Long> {

    Optional<GitHubEvent> findByIntegrationIdAndEventId(
            Long integrationId,
            String eventId
    );
    List<GitHubEvent> findByIntegrationIdOrderByCreatedAtDesc(Long integrationId);

}
