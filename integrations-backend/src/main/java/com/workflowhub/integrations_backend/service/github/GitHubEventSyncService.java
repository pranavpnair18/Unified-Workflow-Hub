package com.workflowhub.integrations_backend.service.github;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowhub.integrations_backend.entity.GitHubEvent;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.repository.GitHubEventRepository;
import com.workflowhub.integrations_backend.repository.IntegrationRepository;
import com.workflowhub.integrations_backend.repository.OAuthCredentialRepository;
import com.workflowhub.integrations_backend.security.EncryptionService;

@Service
public class GitHubEventSyncService {

    private final GitHubEventsApiClient eventsApiClient;
    private final GitHubEventRepository eventRepository;
    private final OAuthCredentialRepository oauthCredentialRepository;
    private final EncryptionService encryptionService;
    private final IntegrationRepository integrationRepository;

    private static final Logger log =
            LoggerFactory.getLogger(GitHubEventSyncService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubEventSyncService(
            GitHubEventsApiClient eventsApiClient,
            GitHubEventRepository eventRepository,
            OAuthCredentialRepository oauthCredentialRepository,
            EncryptionService encryptionService,
            IntegrationRepository integrationRepository
    ) {
        this.eventsApiClient = eventsApiClient;
        this.eventRepository = eventRepository;
        this.oauthCredentialRepository = oauthCredentialRepository;
        this.encryptionService = encryptionService;
        this.integrationRepository = integrationRepository;
    }

    public void syncEvents(Integration integration) {

            try {
                // 1️⃣ Get access token
        var credential = oauthCredentialRepository
                .findByIntegrationId(integration.getId())
                .orElseThrow();

        String accessToken =
                encryptionService.decrypt(credential.getAccessToken());

        // 2️⃣ Extract GitHub username from raw profile
        String username = extractUsername(integration.getMetadata());
        System.out.println("GitHub username=" + username);


        // 3️⃣ Fetch events from GitHub
        List<Map<String, Object>> events =
                eventsApiClient.fetchUserEvents(accessToken, username);

        // 4️⃣ Save new events only
        for (Map<String, Object> event : events) {

            String eventId = (String) event.get("id");
            System.out.println("Saving event " + eventId);


            boolean exists =
                    eventRepository
                            .findByIntegrationIdAndEventId(
                                    integration.getId(),
                                    eventId
                            )
                            .isPresent();

            if (exists) {
                continue;
            }

            // 🔹 Extract external URL
       String externalUrl = null;

Object repoObj = event.get("repo");
if (repoObj instanceof Map<?, ?> repoMap) {
    Object urlObj = repoMap.get("url");
    if (urlObj instanceof String url) {
        externalUrl =
            url.replace("api.github.com/repos", "github.com");
    }
}



            GitHubEvent entity = GitHubEvent.builder()
                    .integrationId(integration.getId())
                    .eventId(eventId)
                    .type((String) event.get("type"))
                    .repoId(extractRepoId(event))
                    .repoName(extractRepoName(event))
                    .actorLogin(extractActorLogin(event))
                    .actorId(extractActorId(event))
                    .createdAt(parseCreatedAt(event))
                    .externalUrl(externalUrl)
                    .payloadJson(toJson(event))
                    .build();

            eventRepository.save(entity);
        }

        System.out.println("SYNC for integrationId=" + integration.getId());
System.out.println("Fetched events count=" + events.size());

    

                
            } catch (HttpClientErrorException.Unauthorized ex) {
    log.error("GitHub token invalid for integrationId={}", integration.getId());

    integration.setStatus("DISCONNECTED");
    integrationRepository.save(integration);

    return;
}
    }

        
    // ----------------- helpers -----------------

   @SuppressWarnings("unchecked")
private String extractUsername(String rawProfileJson) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> profile =
                mapper.readValue(rawProfileJson, Map.class);

        Object login = profile.get("login");
        if (login == null) {
            throw new IllegalStateException("GitHub login not found in profile");
        }

        return login.toString();
    } catch (Exception e) {
        throw new RuntimeException("Failed to extract GitHub username", e);
    }
}

    @SuppressWarnings("unchecked")
    private Long extractRepoId(Map<String, Object> event) {
        Map<String, Object> repo = (Map<String, Object>) event.get("repo");
        return repo != null ? Long.valueOf(repo.get("id").toString()) : null;
    }
    @SuppressWarnings("unchecked")
    private String extractRepoName(Map<String, Object> event) {
        Map<String, Object> repo = (Map<String, Object>) event.get("repo");
        return repo != null ? repo.get("name").toString() : null;
    }
    @SuppressWarnings("unchecked")
    private String extractActorLogin(Map<String, Object> event) {
        Map<String, Object> actor = (Map<String, Object>) event.get("actor");
        return actor != null ? actor.get("login").toString() : null;
    }
    @SuppressWarnings("unchecked")
    private Long extractActorId(Map<String, Object> event) {
        Map<String, Object> actor = (Map<String, Object>) event.get("actor");
        return actor != null ? Long.valueOf(actor.get("id").toString()) : null;
    }

    private OffsetDateTime parseCreatedAt(Map<String, Object> event) {
        return OffsetDateTime.parse(event.get("created_at").toString());
    }
    
    private String toJson(Map<String, Object> event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
