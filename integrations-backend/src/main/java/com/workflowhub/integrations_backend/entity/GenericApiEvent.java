package com.workflowhub.integrations_backend.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(
    name = "generic_api_events",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"integration_id", "external_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericApiEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long integrationId;

    // Optional ID from API (if exists)
    private String externalId;

    private String title;

    private OffsetDateTime occurredAt;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    private OffsetDateTime createdAt; 
}
