package com.workflowhub.integrations_backend.service.google;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.workflowhub.integrations_backend.entity.OAuthCredential;
import com.workflowhub.integrations_backend.repository.OAuthCredentialRepository;
import com.workflowhub.integrations_backend.security.EncryptionService;

@Service
public class GmailTokenService {

    private final OAuthCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GmailTokenService(
            OAuthCredentialRepository credentialRepository,
            EncryptionService encryptionService
    ) {
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
    }

    public String getValidAccessToken(Long integrationId) {

        OAuthCredential cred =
                credentialRepository.findByIntegrationId(integrationId)
                        .orElseThrow(() ->
                                new IllegalStateException("OAuth credentials missing")
                        );

        // 🔹 If token still valid → use it
        if (cred.getExpiresAt() != null &&
            cred.getExpiresAt().isAfter(OffsetDateTime.now().plusMinutes(1))) {

            return encryptionService.decrypt(cred.getAccessToken());
        }

        // 🔥 Token expired → refresh
        return refreshAccessToken(cred);
    }

    private String refreshAccessToken(OAuthCredential cred) {

        String refreshToken =
                encryptionService.decrypt(cred.getRefreshToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body =
                "client_id=" + System.getenv("GOOGLE_CLIENT_ID") +
                "&client_secret=" + System.getenv("GOOGLE_CLIENT_SECRET") +
                "&refresh_token=" + refreshToken +
                "&grant_type=refresh_token";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> res = response.getBody();


        String newAccessToken = (String) res.get("access_token");
        Integer expiresIn = (Integer) res.get("expires_in");

        cred.setAccessToken(encryptionService.encrypt(newAccessToken));
        cred.setExpiresAt(
                OffsetDateTime.now().plusSeconds(expiresIn)
        );

        credentialRepository.save(cred);

        return newAccessToken;
    }
}
