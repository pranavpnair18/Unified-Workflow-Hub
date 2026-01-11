package com.workflowhub.integrations_backend.provider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowhub.integrations_backend.dto.OAuthResult;
import com.workflowhub.integrations_backend.service.OAuthIntegrationService;

@Component
public class GitHubIntegrationProvider implements IntegrationProvider {

    private final OAuthIntegrationService oauthIntegrationService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GitHubIntegrationProvider(OAuthIntegrationService oauthIntegrationService) {
        this.oauthIntegrationService = oauthIntegrationService;
    }

    @Override
    public String getProviderKey() {
        return "github";
    }

    


    @Override
    public String buildAuthorizationUrl(Long userId) {

        String state =
                oauthIntegrationService.createState(userId, getProviderKey());

        String clientId = System.getenv("GITHUB_CLIENT_ID");
        
    if (clientId == null || clientId.isBlank()) {
    throw new IllegalStateException("GITHUB_CLIENT_ID is not set");
}
        String redirectUri = System.getenv()
                .getOrDefault(
                        "GITHUB_OAUTH_REDIRECT_URI",
                        "http://localhost:8080/api/integrations/oauth/callback"
                );

        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode("read:user repo", StandardCharsets.UTF_8)
                + "&state=" + state;
    }

    @Override
    public OAuthResult exchangeCodeForTokens(String code) {

        // 1. Exchange code for access token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(
                List.of(MediaType.APPLICATION_JSON)
        );

        Map<String, String> body = Map.of(
                "client_id", System.getenv("GITHUB_CLIENT_ID"),
                "client_secret", System.getenv("GITHUB_CLIENT_SECRET"),
                "code", code
        );

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> tokenResponse =
        restTemplate.exchange(
                "https://github.com/login/oauth/access_token",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        String accessToken =
                (String) tokenResponse.getBody().get("access_token");
                

        // 2. Fetch user profile
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> authEntity = new HttpEntity<>(authHeaders);

        ResponseEntity<Map<String, Object>> userResponse =
        restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                authEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        //---webhook registration


        Map<String, Object> user = userResponse.getBody();
        String username = (String) user.get("login");

        ObjectMapper mapper = new ObjectMapper();
        String rawProfileJson;
        try {
        rawProfileJson = mapper.writeValueAsString(user);
        } catch (Exception e) {
           throw new RuntimeException("Failed to serialize GitHub user profile", e);
        }

        return new OAuthResult(
                accessToken,
                null,                        // GitHub has no refresh token
                null,                        // No expiry
                "read:user repo",
                String.valueOf(user.get("id")),
                (String) user.get("email"),
                rawProfileJson

        );
    }

    //--webhook regustration
//     public void registerWebhook(
//         Integration integration,
//         String accessToken,
//         String owner,
//     String repo
// ) {
//     String secret = generateWebhookSecret();

//     HttpHeaders headers = new HttpHeaders();
//     headers.setBearerAuth(accessToken);
//     headers.set("User-Agent", "workflowhub");
//     headers.set("Accept", "application/vnd.github+json");

//     Map<String, Object> body = Map.of(
//         "name", "web",
//         "active", true,
//         "events", List.of("push", "pull_request", "issues", "star"),
//         "config", Map.of(
//             "url", "https://abc123.ngrok-free.app/api/webhooks/github",
//             "content_type", "json",
//             "secret", secret
//         )
//     );

//         String[] parts = fullRepoName.split("/");
//         String owner = parts[0];
//         String repo = parts[1];

//     HttpEntity<Map<String, Object>> entity =
//             new HttpEntity<>(body, headers);

//     ResponseEntity<Map<String, Object>> response =
//         restTemplate.exchange(
//                 "https://api.github.com/repos/{owner}/{repo}/hooks",
//                 HttpMethod.POST,
//                 entity,
//                 new ParameterizedTypeReference<Map<String, Object>>() {},
//                 owner,
//                 repo
//         );

//     integration.setWebhookId(
//         String.valueOf(response.getBody().get("id"))
//     );
//     integration.setWebhookSecret(secret);
// }

// public List<Map<String, Object>> fetchUserRepos(String accessToken) {

//     HttpHeaders headers = new HttpHeaders();
//     headers.setBearerAuth(accessToken);
//     headers.set("User-Agent", "workflowhub");
//     headers.set("Accept", "application/vnd.github+json");

//     HttpEntity<Void> entity = new HttpEntity<>(headers);

//     ResponseEntity<List<Map<String, Object>>> response =
//             restTemplate.exchange(
//                     "https://api.github.com/user/repos?per_page=100",
//                     HttpMethod.GET,
//                     entity,
//                     new ParameterizedTypeReference<>() {}
//             );

//     return response.getBody();
// }

public List<Map<String, Object>> fetchUserRepos(String accessToken) {

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.set("User-Agent", "workflowhub");
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<List<Map<String, Object>>> response =
            restTemplate.exchange(
                    "https://api.github.com/user/repos?per_page=100",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

    return response.getBody();
}

    @Override
    public void onDisconnect(Long integrationId) {
        // No token revoke for GitHub OAuth Apps
    }

    @Override
    public Set<String> getCapabilities() {
        return Set.of(
                "OAUTH",
                "REPO_READ"
        );
    }

    //----- helper for webhook

// private String generateWebhookSecret() {
//     return UUID.randomUUID().toString();
// }

}


