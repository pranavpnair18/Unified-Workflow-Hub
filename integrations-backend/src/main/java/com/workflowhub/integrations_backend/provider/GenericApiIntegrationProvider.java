package com.workflowhub.integrations_backend.provider;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.workflowhub.integrations_backend.dto.OAuthResult;

@Component
public class GenericApiIntegrationProvider implements IntegrationProvider {

    @Override
    public String getProviderKey() {
        return "custom_api";
    }

    @Override
    public String buildAuthorizationUrl(Long userId) {
        // ❌ Not OAuth based
        throw new UnsupportedOperationException("API key based provider");
    }

    @Override
    public OAuthResult exchangeCodeForTokens(String code) {
        throw new UnsupportedOperationException("API key based provider");
    }

    @Override
    public void onDisconnect(Long integrationId) {
        // nothing to clean up for now
    }

    @Override
    public Set<String> getCapabilities() {
        return Set.of(
            "API_KEY",
            "REST_POLLING"
        );
    }
}
