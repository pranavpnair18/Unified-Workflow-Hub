package com.workflowhub.integrations_backend.service.generic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowhub.integrations_backend.entity.GenericApiConfig;
import com.workflowhub.integrations_backend.entity.GenericApiEvent;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.repository.GenericApiConfigRepository;
import com.workflowhub.integrations_backend.repository.GenericApiEventRepository;
import com.workflowhub.integrations_backend.security.EncryptionService;

@Service
public class GenericApiPollingService {

    private static final Logger log =
            LoggerFactory.getLogger(GenericApiPollingService.class);

    private final GenericApiConfigRepository configRepository;
    private final EncryptionService encryptionService;
    private final GenericApiEventRepository eventRepository;
    private final ObjectMapper objectMapper;


    public GenericApiPollingService(
            GenericApiConfigRepository configRepository,
            EncryptionService encryptionService,
            GenericApiEventRepository eventRepository,
            ObjectMapper objectMapper
    ) {
        this.configRepository = configRepository;
        this.encryptionService = encryptionService;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    public void poll(Integration integration) {

        GenericApiConfig config =
                configRepository
                        .findByIntegrationId(integration.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "GenericApiConfig missing for integrationId="
                                                + integration.getId()
                                )
                        );

                        log.info(
    "🔧 Custom API config | integrationId={} | baseUrl={} | endpoint={}",
    integration.getId(),
    config.getBaseUrl(),
    config.getEndpoint()
);


        if (config.getBaseUrl() == null || config.getBaseUrl().isBlank()) {
    throw new IllegalStateException(
        "Base URL missing for custom API integrationId=" + integration.getId()
    );
}

String url = buildUrl(config);


        String authValue =
                encryptionService.decrypt(
                        config.getEncryptedAuthHeaderValue()
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                config.getAuthHeaderName(),
                                authValue
                        )
                        .method(
                                config.getHttpMethod(),
                                HttpRequest.BodyPublishers.noBody()
                        )
                        
                        .build();

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            JsonNode root = objectMapper.readTree(response.body());

if (root.isArray()) {
    for (JsonNode item : root) {

        String externalId =
            item.has("id") ? item.get("id").asText() : null;

        if (externalId != null &&
            eventRepository.existsByIntegrationIdAndExternalId(
                integration.getId(), externalId
            )) {
            continue; // skip duplicate
        }

        GenericApiEvent event =
            GenericApiEvent.builder()
                .integrationId(integration.getId())
                .externalId(externalId)
                .title(
                    item.has("title")
                        ? item.get("title").asText()
                        : "API Event"
                )
                .occurredAt(OffsetDateTime.now())
                .rawPayload(item.toString())
                .createdAt(OffsetDateTime.now()) 
                .build();

        eventRepository.save(event);
    }
}


            log.info(
                    "🌐 Polled custom API | integrationId={} | status={}",
                    integration.getId(),
                    response.statusCode()
            );

            log.debug(
                    "📦 Response body (first 500 chars): {}",
                    truncate(response.body(), 500)
            );

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Failed polling custom API for integrationId="
                            + integration.getId(),
                    ex
            );
        }
    }

    private String buildUrl(GenericApiConfig config) {
        if (config.getEndpoint() == null || config.getEndpoint().isBlank()) {
            return config.getBaseUrl();
        }
        return config.getBaseUrl() + config.getEndpoint();
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max
                ? value
                : value.substring(0, max) + "...";
    }
}
