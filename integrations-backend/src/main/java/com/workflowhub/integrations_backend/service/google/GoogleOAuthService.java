package com.workflowhub.integrations_backend.service.google;


import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GoogleOAuthService {

    /**
     * Exchange authorization code for tokens and user info.
     * 
     * Returns a map containing:
     * - accessToken
     * - refreshToken
     * - expiresAt
     * - providerAccountId
     * - providerEmail
     * - scopes
     */
    public Map<String, Object> exchangeCodeForTokens(
            String code
    ) {
        
    try {
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        String redirectUri = System.getenv()
                .getOrDefault("GOOGLE_OAUTH_REDIRECT_URI",
                        "http://localhost:8080/api/integrations/oauth/callback");

        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("Google OAuth env vars not set");
        }

        HttpClient http = HttpClient.newHttpClient();

        String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        HttpRequest tokenReq = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> tokenResp =
                http.send(tokenReq, HttpResponse.BodyHandlers.ofString());

        if (tokenResp.statusCode() != 200) {
            throw new RuntimeException("Token exchange failed: " + tokenResp.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tokenJson = mapper.readTree(tokenResp.body());

        String accessToken = tokenJson.path("access_token").asText(null);
        String refreshToken = tokenJson.path("refresh_token").asText(null);
        long expiresIn = tokenJson.path("expires_in").asLong(0);
        String scopes = tokenJson.path("scope").asText(null);

        if (accessToken == null) {
            throw new RuntimeException("No access token returned by Google");
        }

        // Fetch user info
        HttpRequest userReq = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> userResp =
                http.send(userReq, HttpResponse.BodyHandlers.ofString());

        JsonNode userJson = mapper.readTree(userResp.body());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "expiresAt", expiresIn > 0 ? OffsetDateTime.now().plusSeconds(expiresIn) : null,
                "providerAccountId", userJson.path("sub").asText(null),
                "providerEmail", userJson.path("email").asText(null),
                "scopes", scopes,
                "rawProfile", userJson.toString()
        );

    } catch (Exception ex) {
        throw new RuntimeException(ex);
    }


    }
}
