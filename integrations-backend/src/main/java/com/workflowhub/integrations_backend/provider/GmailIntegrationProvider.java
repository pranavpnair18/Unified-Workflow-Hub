package com.workflowhub.integrations_backend.provider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.workflowhub.integrations_backend.dto.OAuthResult;
import com.workflowhub.integrations_backend.service.OAuthIntegrationService;
import com.workflowhub.integrations_backend.service.google.GoogleOAuthService;

@Component
public class GmailIntegrationProvider implements IntegrationProvider {

    private final GoogleOAuthService googleOAuthService;
    private final OAuthIntegrationService oauthIntegrationService;

    public GmailIntegrationProvider(
            GoogleOAuthService googleOAuthService,
            OAuthIntegrationService oauthIntegrationService
    ) {
        this.googleOAuthService = googleOAuthService;
        this.oauthIntegrationService = oauthIntegrationService;
    }

    @Override
    public String getProviderKey() {
        return "gmail";
    }

    @Override
    public String buildAuthorizationUrl(Long userId) {

        String state =
                oauthIntegrationService.createState(userId, getProviderKey());

        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String redirectUri = System.getenv("GOOGLE_OAUTH_REDIRECT_URI");

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(
                        "openid email profile " +
                        "https://www.googleapis.com/auth/gmail.readonly",
                        StandardCharsets.UTF_8
                )
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + state;
    }

   @Override
    public OAuthResult exchangeCodeForTokens(String code) {
        Map<String, Object> result =
            googleOAuthService.exchangeCodeForTokens(code);
            
       return new OAuthResult(
    (String) result.get("accessToken"),
    (String) result.get("refreshToken"),
    (OffsetDateTime) result.get("expiresAt"),
    (String) result.get("scopes"),
    (String) result.get("providerAccountId"),
    (String) result.get("providerEmail"),
    (String) result.get("rawProfile")
);

    }

    @Override
    public Set<String> getCapabilities() {
        return Set.of(
                "OAUTH",
                "GMAIL_READ"
        );
    }

    @Override
    public void onDisconnect(Long integrationId) {}
}
