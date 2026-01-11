package com.workflowhub.integrations_backend.entity;

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
@Table(name = "generic_api_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericApiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long integrationId;

    private String baseUrl;

    private String endpoint;

    private String httpMethod; // GET, POST (future)

    private String authHeaderName;

    @Column(columnDefinition = "TEXT")
    private String encryptedAuthHeaderValue;

    private Integer pollIntervalMinutes;

    @Column(columnDefinition = "TEXT")
    private String lastCursor;
}

