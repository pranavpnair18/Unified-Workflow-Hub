package com.workflowhub.integrations_backend.entity;

import java.time.Instant;

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
@Table(
    name = "calendar_events",
    indexes = {
        @Index(
            name = "idx_calendar_event_user_time",
            columnList = "userId,startTime"
        ),
        @Index(
            name = "idx_calendar_event_user_provider",
            columnList = "userId,provider"
        ),
        @Index(
            name = "idx_calendar_event_provider_event",
            columnList = "userId,provider,providerEventId",
            unique = true
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- Ownership ----
    @Column(nullable = false)
    private Long userId;

    // google, outlook, notion, etc.
    @Column(nullable = false)
    private String provider;

    // Event ID from provider (Google eventId, etc.)
    @Column(nullable = false)
    private String providerEventId;

    // ---- Event data (UI-friendly) ----
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    private Boolean allDay;

    // ---- Provider-specific data ----
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // ---- Sync info ----
    private Instant sourceUpdatedAt;
    private Instant lastSyncedAt;

    @Column(nullable = false)
    private String status; // ACTIVE, DELETED

    // ---- Audit ----
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
