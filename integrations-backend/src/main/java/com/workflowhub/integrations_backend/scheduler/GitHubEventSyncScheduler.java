package com.workflowhub.integrations_backend.scheduler;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.repository.IntegrationRepository;
import com.workflowhub.integrations_backend.service.github.GitHubEventSyncService;

@Component
public class GitHubEventSyncScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(GitHubEventSyncScheduler.class);

    private final IntegrationRepository integrationRepository;
    private final GitHubEventSyncService gitHubEventSyncService;

    public GitHubEventSyncScheduler(
            IntegrationRepository integrationRepository,
            GitHubEventSyncService gitHubEventSyncService
    ) {
        this.integrationRepository = integrationRepository;
        this.gitHubEventSyncService = gitHubEventSyncService;
    }

    // Every 15 minutes
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void syncGitHubEvents() {

        log.info("🔁 GitHub scheduler tick at {}", Instant.now());

        List<Integration> integrations =
                integrationRepository.findByProviderAndStatus(
                        "github",
                        "ACTIVE"
                );

        log.info("📦 Found {} active GitHub integrations",
                integrations.size());

        for (Integration integration : integrations) {
            try {
                log.info(
                    "Syncing GitHub events for integrationId={}",
                    integration.getId()
                );
                gitHubEventSyncService.syncEvents(integration);
            } catch (Exception e) {
                log.error(
                    "❌ Failed GitHub sync for integrationId={}",
                    integration.getId(),
                    e
                );
            }
        }
    }
}
