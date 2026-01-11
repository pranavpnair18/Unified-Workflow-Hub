package com.workflowhub.integrations_backend.service;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;


@Service
public class OAuthIntegrationService {
        private final ConcurrentHashMap<String, Long> stateStore = new ConcurrentHashMap<>();
        public record OAuthState(String provider, Long userId) {}


       public String createState(Long userId, String provider) {
    String rawState = provider + ":" + userId + ":" + UUID.randomUUID();
    stateStore.put(rawState, userId);
    return rawState;
}


        public OAuthState consumeState(String state) {
    Long userId = stateStore.remove(state);
    if (userId == null) {
        throw new IllegalArgumentException("Invalid or expired state");
    }

    String[] parts = state.split(":");
    if (parts.length < 2) {
        throw new IllegalArgumentException("Invalid state format");
    }

    String provider = parts[0];
    return new OAuthState(provider, userId);
}

    /**
     
     * Start OAuth flow for a provider (Google for now).
     * Later: GitHub, Microsoft, Slack, etc.
     */
    public URI startOAuth(
            String provider,
            Long userId,
            Map<String, String> params
    ) {
        // logic will be moved here step-by-step
        return null;
    }

    /**
     * Handle OAuth callback (code exchange, token save).
     */
    public void handleOAuthCallback(
            Map<String, String> params
    ) {
        // logic will be moved here step-by-step
    }
}
