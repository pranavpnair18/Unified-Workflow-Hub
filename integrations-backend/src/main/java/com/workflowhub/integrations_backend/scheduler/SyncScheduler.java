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
import com.workflowhub.integrations_backend.service.google.GoogleCalendarSyncService;

@Component
public class SyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(SyncScheduler.class);

    private final IntegrationRepository integrationRepository;
    private final GoogleCalendarSyncService googleCalendarSyncService;
   

    public SyncScheduler(
            IntegrationRepository integrationRepository,
            GoogleCalendarSyncService googleCalendarSyncService
          
    ) {
        this.integrationRepository = integrationRepository;
        this.googleCalendarSyncService = googleCalendarSyncService;
        
    }

    /**
     * Runs every 30 minutes.
     * Step 2.5: Real background sync.
     */
   @Scheduled(fixedRate = 1 * 60 * 1000)
public void runGoogleCalendarSync() {

    log.info("🔁 Calendar scheduler tick at {}", Instant.now());

    List<Integration> calendarIntegrations =
            integrationRepository.findByProviderAndStatus(
                    "calendar",
                    "ACTIVE"
            );

    log.info("📦 Found {} active calendar integrations",
            calendarIntegrations.size());

    for (Integration integration : calendarIntegrations) {
        try {
            log.info("Syncing Calendar for userId={} integrationId={}",
                    integration.getUserId(), integration.getId());

            googleCalendarSyncService.syncForIntegration(integration);

            integration.setLastSyncedAt(OffsetDateTime.now());
            integrationRepository.save(integration);

        } catch (Exception ex) {
            log.error(
                "❌ Calendar sync failed for integrationId={}",
                integration.getId(),
                ex
            );
        }
    }
}

}
