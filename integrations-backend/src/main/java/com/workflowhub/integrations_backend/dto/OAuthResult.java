package com.workflowhub.integrations_backend.dto;
import java.time.OffsetDateTime;


public record OAuthResult(
    String accessToken,
    String refreshToken,
    OffsetDateTime expiresAt,
    String scopes,
    String providerAccountId,
    String providerEmail,
    String rawProfile
) {}
