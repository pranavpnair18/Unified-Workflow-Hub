package com.workflowhub.integrations_backend.scheduler;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.repository.IntegrationRepository;
import com.workflowhub.integrations_backend.service.google.GmailSyncService;


@Component
public class GmailSyncScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(GmailSyncScheduler.class);

    private final IntegrationRepository integrationRepository;
    private final GmailSyncService gmailSyncService;
    


    public GmailSyncScheduler(
            IntegrationRepository integrationRepository,
            GmailSyncService gmailSyncService
            

    ) {
        this.integrationRepository = integrationRepository;
        this.gmailSyncService = gmailSyncService;
       
    }

    /**
     * Runs every 15 minutes
     */
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void runGmailSync() {

        log.info("📬 Gmail scheduler tick at {}", Instant.now());

        List<Integration> gmailIntegrations =
                integrationRepository.findByProviderAndStatus(
                        "gmail",
                        "ACTIVE"
                );

        log.info("📦 Found {} active Gmail integrations",
                gmailIntegrations.size());

        for (Integration integration : gmailIntegrations) {
            try {
                log.info(
                        "Syncing Gmail for userId={} integrationId={}",
                        integration.getUserId(),
                        integration.getId()
                );

                gmailSyncService.sync(integration);

                integration.setLastSyncedAt(OffsetDateTime.now());
                integrationRepository.save(integration);

            } catch (Exception ex) {
                log.error(
                        "❌ Gmail sync failed for integrationId={}",
                        integration.getId(),
                        ex
                );
            }
        }
    }
}
