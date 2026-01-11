package com.workflowhub.integrations_backend.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.entity.OAuthCredential;
import com.workflowhub.integrations_backend.repository.OAuthCredentialRepository;
import com.workflowhub.integrations_backend.security.EncryptionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private final OAuthCredentialRepository oauthCredentialRepository;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    public String getValidAccessToken(Integration integration) {

        OAuthCredential cred = oauthCredentialRepository
                .findByIntegrationId(integration.getId())
                .orElseThrow(() -> new RuntimeException("OAuth credentials not found"));

        // 1️⃣ Check expiry
        if (cred.getExpiresAt() != null &&
                cred.getExpiresAt().isAfter(OffsetDateTime.now().plusMinutes(1))) {

            return encryptionService.decrypt(cred.getAccessToken());
        }

        // 2️⃣ Refresh token
        if (cred.getRefreshToken() == null) {
            throw new RuntimeException("No refresh token available");
        }

        return refreshAccessToken(cred);
    }

    private String refreshAccessToken(OAuthCredential cred) {

        try {
            String clientId = System.getenv("GOOGLE_CLIENT_ID");
            String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");

            if (clientId == null || clientSecret == null) {
                throw new RuntimeException("Google OAuth env vars not set");
            }

            String refreshToken = encryptionService.decrypt(cred.getRefreshToken());

            String body = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                    + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                    + "&grant_type=refresh_token";

            HttpRequest tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient http = HttpClient.newHttpClient();
            HttpResponse<String> resp = http.send(tokenReq, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != HttpStatus.OK.value()) {
                throw new RuntimeException("Token refresh failed: " + resp.body());
            }

            JsonNode json = objectMapper.readTree(resp.body());
            String newAccessToken = json.path("access_token").asText(null);
            long expiresIn = json.path("expires_in").asLong(0);

            if (newAccessToken == null) {
                throw new RuntimeException("No access_token in refresh response");
            }

            // save updated token
            cred.setAccessToken(encryptionService.encrypt(newAccessToken));
            if (expiresIn > 0) {
                cred.setExpiresAt(OffsetDateTime.now().plusSeconds(expiresIn));
            }

            oauthCredentialRepository.save(cred);

            return newAccessToken;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to refresh OAuth token", ex);
        }
    }
}
