package com.workflowhub.integrations_backend.service;

import org.springframework.stereotype.Service;

import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.repository.ApiKeyCredentialRepository;
import com.workflowhub.integrations_backend.repository.OAuthCredentialRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;






@Service
@RequiredArgsConstructor
public class IntegrationDisconnectService {

    private final OAuthCredentialRepository oauthCredentialRepository;
    private final ApiKeyCredentialRepository apiKeyCredentialRepository;
    private final IntegrationService integrationService;

    @Transactional
    public void disconnect(Long integrationId, Long userId) {
        Integration integration = integrationService
                .getByIdAndUserId(integrationId, userId)
                .orElseThrow(() -> new RuntimeException("Integration not found"));

        oauthCredentialRepository.deleteByIntegrationId(integrationId);
        apiKeyCredentialRepository.deleteByIntegrationId(integrationId);

        integration.setStatus("DISCONNECTED");
        integrationService.saveIntegration(integration);
    }
}
