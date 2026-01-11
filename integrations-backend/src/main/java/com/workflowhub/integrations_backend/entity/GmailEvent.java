package com.workflowhub.integrations_backend.entity;


import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gmail_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GmailEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long integrationId;

    private String messageId;

    private String threadId;

    private String fromEmail;

    private String subject;

    private OffsetDateTime receivedAt;

    private String externalUrl;


    @Column(columnDefinition = "TEXT")
    private String rawPayload;
}
