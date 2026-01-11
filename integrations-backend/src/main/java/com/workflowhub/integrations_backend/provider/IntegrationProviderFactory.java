package com.workflowhub.integrations_backend.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class IntegrationProviderFactory {

    private final Map<String, IntegrationProvider> providers = new HashMap<>();

    public IntegrationProviderFactory(List<IntegrationProvider> providerList) {
        for (IntegrationProvider provider : providerList) {
            providers.put(provider.getProviderKey(), provider);
        }
    }

    public IntegrationProvider get(String providerKey) {
        IntegrationProvider provider = providers.get(providerKey);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerKey);
        }
        return provider;
    }
}
