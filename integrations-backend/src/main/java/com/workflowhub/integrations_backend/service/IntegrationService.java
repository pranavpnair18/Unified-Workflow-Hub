package com.workflowhub.integrations_backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.workflowhub.integrations_backend.dto.OAuthResult;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.entity.OAuthCredential;
import com.workflowhub.integrations_backend.repository.IntegrationRepository;
import com.workflowhub.integrations_backend.repository.OAuthCredentialRepository;
import com.workflowhub.integrations_backend.security.EncryptionService;


@Service
public class IntegrationService {

    private final IntegrationRepository repository;
    private final OAuthCredentialRepository oauthCredentialRepository;
    private final EncryptionService encryptionService;


    

     public IntegrationService(
        IntegrationRepository repository,
        OAuthCredentialRepository oauthCredentialRepository,
        EncryptionService encryptionService
    ) {
        this.repository = repository;
        this.oauthCredentialRepository = oauthCredentialRepository;
        this.encryptionService = encryptionService;
    }

    public List<Integration> getIntegrationsForUser(Long userId) {
        return repository.findByUserId(userId);
    }
    
    public List<Integration> getIntegration(Long userId, String provider) {
        return repository.findAllByUserIdAndProvider(userId, provider);
    }
    
    public Optional<Integration> getByIdAndUserId(Long id, Long userId) {
    return repository.findByIdAndUserId(id, userId);
}


    public Integration saveIntegration(Integration integration) {
        return repository.save(integration);
    }

    public void deleteIntegration(Long id) {
        repository.deleteById(id);
    }

    public Integration upsertOAuthIntegration(
        Long userId,
        String provider,
        OAuthResult result
) {
    // 1. Load all existing integrations (defensive against old duplicates)
    List<Integration> existing =
            repository.findAllByUserIdAndProvider(userId, provider);

    Integration integration;

    if (!existing.isEmpty()) {
        // Use the latest integration
        integration = existing.get(existing.size() - 1);

        // Clean old duplicates
        for (int i = 0; i < existing.size() - 1; i++) {
            Integration old = existing.get(i);
            oauthCredentialRepository.deleteByIntegrationId(old.getId());
            repository.delete(old);
        }
    } else {
        integration = Integration.builder()
                .userId(userId)
                .provider(provider)
                .connectionName(
                        provider.substring(0, 1).toUpperCase() + provider.substring(1)
                )
                .type("OAUTH")
                .build();
    }

    // 2. Update integration
    integration.setStatus("ACTIVE");
    integration.setMetadata(result.rawProfile());
    integration.setLastSyncedAt((OffsetDateTime) null);

    integration = repository.save(integration);

    // 3. Replace credentials safely
    oauthCredentialRepository
            .findByIntegrationId(integration.getId())
            .ifPresent(oauthCredentialRepository::delete);

    OAuthCredential cred = OAuthCredential.builder()
            .integrationId(integration.getId())
            .accessToken(encryptionService.encrypt(result.accessToken()))
            .refreshToken(encryptionService.encrypt(result.refreshToken()))
            .expiresAt(result.expiresAt())
            .scopes(result.scopes())
            .providerAccountId(result.providerAccountId())
            .providerEmail(result.providerEmail())
            .build();

    oauthCredentialRepository.save(cred);

    return integration;
}
}
