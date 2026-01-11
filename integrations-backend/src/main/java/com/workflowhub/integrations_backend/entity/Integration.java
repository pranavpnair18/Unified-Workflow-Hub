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
@Table(name = "integrations", indexes = {
        @Index(name = "idx_integrations_userid", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Integration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owner of this integration in your system (link to your users table).
     * We'll use Long for now — adapt to your User PK type.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** provider id, e.g. 'google', 'github', 'slack', 'jira', 'custom_api' */
    @Column(nullable = false, length = 64)
    private String provider;

    /** friendly name the user gave for this connection */
    @Column(name = "connection_name", length = 255)
    private String connectionName;

    /** 'OAUTH' or 'API_KEY' */
    @Column(nullable = false, length = 32)
    private String type;

    /** status like PENDING, ACTIVE, FAILED, DISCONNECTED */
    @Column(length = 32)
    private String status;

    /**
     * Flexible JSON metadata (store as JSON text).
     * Example: {"accountEmail":"x@y.com","siteUrl":"https://..."}
     */
    @Column(columnDefinition = "text")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @Column
    private String webhookId;

    @Column
    private String webhookSecret;


    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
