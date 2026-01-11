package com.workflowhub.integrations_backend.provider;

import java.util.Set;

import com.workflowhub.integrations_backend.dto.OAuthResult;

public interface IntegrationProvider {

    String getProviderKey(); 
    // "google", "slack", "github"

    String buildAuthorizationUrl(Long userId);

    OAuthResult exchangeCodeForTokens(String code);

    void onDisconnect(Long integrationId);

    Set<String> getCapabilities();
}
