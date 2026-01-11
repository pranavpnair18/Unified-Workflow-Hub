package com.workflowhub.integrations_backend.scheduler;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.repository.IntegrationRepository;
import com.workflowhub.integrations_backend.service.generic.GenericApiPollingService;

@Component
public class GenericApiPollingScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(GenericApiPollingScheduler.class);

    private final IntegrationRepository integrationRepository;
    private final GenericApiPollingService genericApiPollingService;

    public GenericApiPollingScheduler(
            IntegrationRepository integrationRepository,
            GenericApiPollingService genericApiPollingService
    ) {
        this.integrationRepository = integrationRepository;
        this.genericApiPollingService = genericApiPollingService;
    }

    /**
     * Runs every 15 minutes.
     * (Later we’ll make this per-integration configurable)
     */
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void runPolling() {

        log.info("🔁 Generic API polling tick at {}", Instant.now());

        List<Integration> integrations =
                integrationRepository.findByProviderAndStatus(
                        "custom_api",
                        "ACTIVE"
                );

        log.info("📦 Found {} active custom API integrations",
                integrations.size());

        for (Integration integration : integrations) {
            try {
                log.info(
                        "➡️ Polling custom API | userId={} integrationId={}",
                        integration.getUserId(),
                        integration.getId()
                );

                genericApiPollingService.poll(integration);

            } catch (Exception ex) {
                log.error(
                        "❌ Polling failed for integrationId={}",
                        integration.getId(),
                        ex
                );
            }
        }
    }
}
