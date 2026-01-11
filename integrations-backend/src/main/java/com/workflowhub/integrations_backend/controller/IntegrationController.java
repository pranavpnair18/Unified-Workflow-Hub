package com.workflowhub.integrations_backend.controller;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workflowhub.integrations_backend.dto.OAuthResult;
import com.workflowhub.integrations_backend.entity.ApiKeyCredential;
import com.workflowhub.integrations_backend.entity.CalendarEvent;
import com.workflowhub.integrations_backend.entity.GenericApiConfig;
import com.workflowhub.integrations_backend.entity.GitHubEvent;
import com.workflowhub.integrations_backend.entity.GmailEvent;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.entity.OAuthCredential;
import com.workflowhub.integrations_backend.provider.GitHubIntegrationProvider;
import com.workflowhub.integrations_backend.provider.IntegrationProvider;
import com.workflowhub.integrations_backend.provider.IntegrationProviderFactory;
import com.workflowhub.integrations_backend.repository.ApiKeyCredentialRepository;
import com.workflowhub.integrations_backend.repository.CalendarEventRepository;
import com.workflowhub.integrations_backend.repository.GenericApiConfigRepository;
import com.workflowhub.integrations_backend.repository.GenericApiEventRepository;
import com.workflowhub.integrations_backend.repository.GitHubEventRepository;
import com.workflowhub.integrations_backend.repository.GmailEventRepository;
import com.workflowhub.integrations_backend.repository.IntegrationRepository;
import com.workflowhub.integrations_backend.repository.OAuthCredentialRepository;
import com.workflowhub.integrations_backend.security.EncryptionService;
import com.workflowhub.integrations_backend.service.IntegrationDisconnectService;
import com.workflowhub.integrations_backend.service.IntegrationService;
import com.workflowhub.integrations_backend.service.OAuthIntegrationService;
import com.workflowhub.integrations_backend.service.github.GitHubEventSyncService;
import com.workflowhub.integrations_backend.service.google.GmailSyncService;
import com.workflowhub.integrations_backend.service.google.GoogleCalendarSyncService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;
    private final ApiKeyCredentialRepository apiKeyCredentialRepository;
    private final OAuthCredentialRepository oauthCredentialRepository;
    private final EncryptionService encryptionService;
    private final OAuthIntegrationService oauthIntegrationService;
    private final IntegrationDisconnectService integrationDisconnectService;
    private final IntegrationProviderFactory integrationProviderFactory;
    private final GitHubEventSyncService gitHubEventSyncService;
    private final GitHubEventRepository gitHubEventRepository;
    private final GmailSyncService gmailSyncService;
    private final GmailEventRepository gmailEventRepository;
    private final GoogleCalendarSyncService googleCalendarSyncService;
    private final CalendarEventRepository calendarEventRepository;
    private final GenericApiConfigRepository genericApiConfigRepository;
    private final GenericApiEventRepository genericApiEventRepository;

    public IntegrationController(
            IntegrationService integrationService,
            IntegrationRepository integrationRepository,
            ApiKeyCredentialRepository apiKeyCredentialRepository,
            OAuthCredentialRepository oauthCredentialRepository,
            EncryptionService encryptionService,
            OAuthIntegrationService oauthIntegrationService,
            IntegrationDisconnectService integrationDisconnectService,
            IntegrationProviderFactory integrationProviderFactory,
            GitHubEventSyncService gitHubEventSyncService,
            GitHubEventRepository gitHubEventRepository,
            GmailSyncService gmailSyncService,
            GmailEventRepository gmailEventRepository,
            GoogleCalendarSyncService googleCalendarSyncService,
            CalendarEventRepository calendarEventRepository,
            GenericApiConfigRepository genericApiConfigRepository,
            GenericApiEventRepository genericApiEventRepository

    ) {
        this.integrationService = integrationService;
        this.apiKeyCredentialRepository = apiKeyCredentialRepository;
        this.oauthCredentialRepository = oauthCredentialRepository;
        this.encryptionService = encryptionService;
        this.oauthIntegrationService = oauthIntegrationService;
        this.integrationDisconnectService = integrationDisconnectService;
        this.integrationProviderFactory = integrationProviderFactory;
        this.gitHubEventSyncService = gitHubEventSyncService;
        this.gitHubEventRepository = gitHubEventRepository;
        this.gmailSyncService = gmailSyncService;
        this.gmailEventRepository = gmailEventRepository;
        this.googleCalendarSyncService = googleCalendarSyncService;
        this.calendarEventRepository = calendarEventRepository;
        this.genericApiConfigRepository = genericApiConfigRepository;
        this.genericApiEventRepository = genericApiEventRepository;
    }

    // -------------------- helpers --------------------

    private Long resolveUserId(HttpHeaders headers) {
        List<String> vals = headers.get("X-User-Id");
        if (vals != null && !vals.isEmpty()) {
            try {
                return Long.valueOf(vals.get(0));
            } catch (NumberFormatException ignored) {}
        }
        return 1L;
    }

    // -------------------- LIST INTEGRATIONS --------------------

    @GetMapping
public ResponseEntity<List<IntegrationResponse>> listIntegrations(
        @RequestHeader HttpHeaders headers
) {
    Long userId = resolveUserId(headers);

    List<IntegrationResponse> response =
            integrationService.getIntegrationsForUser(userId)
                    .stream()
                    .map(i -> {
                        try {
                            IntegrationProvider provider =
                                    integrationProviderFactory.get(i.getProvider());
                            return IntegrationResponse.fromEntity(i, provider);
                        } catch (Exception ex) {
                            // 🚨 provider key no longer exists
                            return new IntegrationResponse(
                                    i.getId(),
                                    i.getProvider(),
                                    i.getConnectionName(),
                                    i.getType(),
                                    i.getStatus(),
                                    i.getMetadata(),
                                    i.getLastSyncedAt(),
                                    Set.of() // empty capabilities
                            );
                        }
                    })
                    .toList();

    return ResponseEntity.ok(response);
}


    // -------------------- API KEY CONNECT --------------------

    @PostMapping("/{provider}/connect")
public ResponseEntity<?> connectApiKey(
        @PathVariable String provider,
        @RequestBody ApiKeyConnectRequest req,
        @RequestHeader HttpHeaders headers
) {
    Long userId = resolveUserId(headers);

    if (req.getApiKey() == null || req.getApiKey().isBlank()) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "apiKey is required"));
    }

    Integration integration =
            integrationService
                    .getIntegration(userId, provider)
                    .stream()
                    .findFirst()
                    .map(existing -> {
                        existing.setConnectionName(req.getConnectionName());
                        existing.setStatus("ACTIVE");
                        existing.setMetadata(buildMetadataForApiKey(req));
                        return integrationService.saveIntegration(existing);
                    })
                    .orElseGet(() -> integrationService.saveIntegration(
                            Integration.builder()
                                    .userId(userId)
                                    .provider(provider)
                                    .connectionName(req.getConnectionName())
                                    .type("API_KEY")
                                    .status("ACTIVE")
                                    .metadata(buildMetadataForApiKey(req))
                                    .build()
                    ));

    apiKeyCredentialRepository
            .findByIntegrationId(integration.getId())
            .ifPresentOrElse(
                    cred -> {
                        cred.setEncryptedKey(
                                encryptionService.encrypt(req.getApiKey())
                        );
                        apiKeyCredentialRepository.save(cred);
                    },
                    () -> apiKeyCredentialRepository.save(
                            ApiKeyCredential.builder()
                                    .integrationId(integration.getId())
                                    .encryptedKey(
                                            encryptionService.encrypt(req.getApiKey())
                                    )
                                    .build()
                    )
            );

    if ("custom_api".equals(provider)) {
        genericApiConfigRepository.save(
                GenericApiConfig.builder()
                        .integrationId(integration.getId())
                        .baseUrl(req.getBaseUrl())
                        .endpoint(req.getEndpoint())
                        .httpMethod("GET")
                        .authHeaderName(req.getAuthHeaderName())
                        .encryptedAuthHeaderValue(
                                encryptionService.encrypt(req.getApiKey())
                        )
                        .pollIntervalMinutes(15)
                        .build()
        );
    }

    IntegrationProvider p =
            integrationProviderFactory.get(provider);

    return ResponseEntity.ok(
            IntegrationResponse.fromEntity(integration, p)
    );
}

    // -------------------- OAUTH START --------------------

    @GetMapping("/oauth/start")
    public ResponseEntity<?> oauthStart(
            @RequestParam String provider,
            @RequestHeader HttpHeaders headers
    ) {
        Long userId = resolveUserId(headers);

        IntegrationProvider p = integrationProviderFactory.get(provider);
        String authUrl = p.buildAuthorizationUrl(userId);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authUrl))
                .build();
    }

    // -------------------- OAUTH CALLBACK --------------------

    @GetMapping("/oauth/callback")
    public ResponseEntity<?> oauthCallback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        OAuthIntegrationService.OAuthState oauthState =
                oauthIntegrationService.consumeState(state);

        OAuthResult result =
                integrationProviderFactory
                        .get(oauthState.provider())
                        .exchangeCodeForTokens(code);

        integrationService.upsertOAuthIntegration(
                oauthState.userId(),
                oauthState.provider(),
                result
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(
                        "http://localhost:3000/integrations?connected=" +
                                oauthState.provider()
                ))
                .build();

    }


    //-------custom-api - fetch-------------

    @GetMapping("/custom_api/events")
public ResponseEntity<?> getCustomApiEvents(
        @RequestHeader HttpHeaders headers
) {
    Long userId = resolveUserId(headers);

    Integration integration =
            integrationService
                    .getIntegration(userId, "custom_api")
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("Custom API not connected")
                    );

    return ResponseEntity.ok(
            genericApiEventRepository
                    .findByIntegrationIdOrderByCreatedAtDesc(
                            integration.getId()
                    )
    );
}

    //----------------- calendar fetch------------

    @PostMapping("/calendar/sync")
public ResponseEntity<?> syncCalendar(
        @RequestHeader HttpHeaders headers
) {
    Long userId = resolveUserId(headers);

    Integration integration =
            integrationService
                    .getIntegration(userId, "calendar")
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("Calendar not connected")
                    );

    googleCalendarSyncService.syncForIntegration(integration);

    return ResponseEntity.ok(
            Map.of("message", "Calendar synced successfully")
    );
}


@GetMapping("/calendar/events")
public ResponseEntity<?> getCalendarEvents(
        @RequestHeader HttpHeaders headers
) {
    Long userId = resolveUserId(headers);

    List<CalendarEvent> events =
        calendarEventRepository
            .findByUserIdAndProviderOrderByStartTimeDesc(
                userId,
                "calendar"   // or "calendar" if you renamed provider
            );

    return ResponseEntity.ok(events);
}


    // -------------------- GITHUB EVENTS --------------------

    @PostMapping("/github/events/sync")
    public ResponseEntity<?> syncGitHubEvents(@RequestHeader HttpHeaders headers) {

        Long userId = resolveUserId(headers);

        Integration integration =
                integrationService.getIntegration(userId, "github")
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("GitHub not connected"));

        gitHubEventSyncService.syncEvents(integration);

        return ResponseEntity.ok(Map.of("message", "GitHub events synced"));
    }

    @GetMapping("/github/events")
    public ResponseEntity<?> getGitHubEvents(@RequestHeader HttpHeaders headers) {

        Long userId = resolveUserId(headers);

        Integration integration =
                integrationService.getIntegration(userId, "github")
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("GitHub not connected"));

        List<GitHubEvent> events =
                gitHubEventRepository.findByIntegrationIdOrderByCreatedAtDesc(
                        integration.getId()
                );

        return ResponseEntity.ok(events);
    }

    // -------------------- GITHUB REPOS --------------------

    @GetMapping("/github/repos")
    public ResponseEntity<?> getGitHubRepos(@RequestHeader HttpHeaders headers) {

        Long userId = resolveUserId(headers);

        Integration integration =
                integrationService.getIntegration(userId, "github")
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("GitHub not connected"));

        OAuthCredential cred =
                oauthCredentialRepository
                        .findByIntegrationId(integration.getId())
                        .orElseThrow(() ->
                                new IllegalStateException("GitHub credentials missing"));

        String accessToken = encryptionService.decrypt(cred.getAccessToken());

        GitHubIntegrationProvider provider =
                (GitHubIntegrationProvider) integrationProviderFactory.get("github");

        List<Map<String, Object>> repos =
                provider.fetchUserRepos(accessToken);

        return ResponseEntity.ok(
                repos.stream()
                        .map(r -> Map.of(
                                "id", r.get("id"),
                                "name", r.get("name"),
                                "fullName", r.get("full_name"),
                                "private", r.get("private")
                        ))
                        .toList()
        );
    }

    // -------------------- DISCONNECT --------------------

    @DeleteMapping("/{integrationId}")
    public ResponseEntity<?> disconnectIntegration(
            @PathVariable Long integrationId,
            @RequestHeader HttpHeaders headers
    ) {
        integrationDisconnectService.disconnect(
                integrationId,
                resolveUserId(headers)
        );
        return ResponseEntity.ok(Map.of("message", "Integration disconnected"));
    }

    // -------------------- helpers --------------------

    private String buildMetadataForApiKey(ApiKeyConnectRequest req) {
        Map<String, String> meta = new HashMap<>();
        meta.put("connectionName", req.getConnectionName());
        meta.put("apiKeyLast4",
                req.getApiKey().substring(req.getApiKey().length() - 4));
        return meta.toString();
    }


    //------------Gmail Integration-------------

    @PostMapping("/gmail/sync")
public ResponseEntity<?> syncGmail(
        @RequestHeader HttpHeaders headers
) {
    Long userId = resolveUserId(headers);

    Integration integration =
            integrationService
                    .getIntegration(userId, "gmail")
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("Google/Gmail not connected")
                    );

    gmailSyncService.sync(integration);

    return ResponseEntity.ok(
            Map.of("message", "Gmail synced successfully")
    );
}

        //----Gmail fetching--------

@GetMapping("/gmail/messages")
public ResponseEntity<?> getGmailMessages(
        @RequestHeader HttpHeaders headers
) {
    Long userId = resolveUserId(headers);

    Integration integration =
            integrationService
                    .getIntegration(userId, "gmail")
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("Google/Gmail not connected")
                    );

    List<GmailEvent> messages =
            gmailEventRepository
                    .findByIntegrationIdOrderByReceivedAtDesc(
                            integration.getId()
                    );

    return ResponseEntity.ok(messages);
}



    // -------------------- DTOs --------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiKeyConnectRequest {
        private String connectionName;
        private String apiKey;

        // 👇 Custom API only
    private String baseUrl;
    private String endpoint;
    private String authHeaderName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntegrationResponse {
        private Long id;
        private String provider;
        private String connectionName;
        private String type;
        private String status;
        private String metadata;
        private OffsetDateTime lastSyncedAt;
        private Set<String> capabilities;

        static IntegrationResponse fromEntity(
                Integration e,
                IntegrationProvider provider
        ) {
            return new IntegrationResponse(
                    e.getId(),
                    e.getProvider(),
                    e.getConnectionName(),
                    e.getType(),
                    e.getStatus(),
                    e.getMetadata(),
                    e.getLastSyncedAt(),
                    provider.getCapabilities()
            );
        }
    }
}
