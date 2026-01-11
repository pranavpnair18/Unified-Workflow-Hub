package com.workflowhub.integrations_backend.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "oauth_credentials", indexes = {
        @Index(name = "idx_oauth_integration", columnList = "integration_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Link to Integration table */
    @Column(name = "integration_id", nullable = false)
    private Long integrationId;

    /** Encrypted access token (encrypt before saving) */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    /** Encrypted refresh token (if provider returns one) */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_type")
    private String tokenType;

    /** Expiry time of access token (optional) */
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    /** Comma-separated scopes granted */
    @Column(name = "scopes")
    private String scopes;

    /** Provider-specific account id (e.g., Google sub, GitHub id) */
    @Column(name = "provider_account_id")
    private String providerAccountId;

    /** Optional provider account email for display */
    @Column(name = "provider_email")
    private String providerEmail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
