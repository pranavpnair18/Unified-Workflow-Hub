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
    name = "github_events",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"integration_id", "event_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links event to a specific integration
    @Column(name = "integration_id", nullable = false)
    private Long integrationId;

    // GitHub's global event ID (important!)
    @Column(name = "event_id", nullable = false)
    private String eventId;

    // PushEvent, PullRequestEvent, IssuesEvent, etc.
    @Column(nullable = false)
    private String type;

    // Repo context
    private Long repoId;
    private String repoName;

    // Who triggered the event
    private String actorLogin;
    private Long actorId;

    // When GitHub says it happened
    private OffsetDateTime createdAt;

    //To store the url
    private String externalUrl;


    // Store raw payload for flexibility
    
    @Column(columnDefinition = "TEXT")
    private String payloadJson;
}
