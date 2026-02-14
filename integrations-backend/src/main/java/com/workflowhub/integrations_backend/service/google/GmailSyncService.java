package com.workflowhub.integrations_backend.service.google;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowhub.integrations_backend.entity.GmailEvent;
import com.workflowhub.integrations_backend.entity.Integration;
import com.workflowhub.integrations_backend.entity.OAuthCredential;
import com.workflowhub.integrations_backend.repository.GmailEventRepository;
import com.workflowhub.integrations_backend.repository.OAuthCredentialRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GmailSyncService {

    private final GmailApiClient gmailApiClient;
    private final GmailEventRepository gmailEventRepository;
    private final OAuthCredentialRepository oauthCredentialRepository;
    private final GmailTokenService gmailTokenService;

    public void sync(Integration integration) {

        OAuthCredential cred =
                oauthCredentialRepository
                        .findByIntegrationId(integration.getId())
                        .orElseThrow();
                System.out.println("cred: " + cred);

        String accessToken =
        gmailTokenService.getValidAccessToken(integration.getId());


        List<Map<String, Object>> messages =
                gmailApiClient.listMessages(accessToken);

        ObjectMapper mapper = new ObjectMapper();

        for (Map<String, Object> msg : messages) {

            String messageId = (String) msg.get("id");

            if (gmailEventRepository.existsByIntegrationIdAndMessageId(
                    integration.getId(),
                    messageId
            )) {
                continue;
            }

            Map<String, Object> full =
                    gmailApiClient.getMessage(accessToken, messageId);

            Map<String, Object> payload =
        asStringObjectMap(full.get("payload"));

Object rawHeaders = payload.get("headers");

List<Map<String, Object>> headers =
        rawHeaders instanceof List<?> list
                ? list.stream()
                      .map(this::asStringObjectMap)
                      .toList()
                : List.of();


            String subject = null;
            String from = null;

            for (Map<String, Object> h : headers) {
                if ("Subject".equalsIgnoreCase((String) h.get("name"))) {
                    subject = (String) h.get("value");
                }
                if ("From".equalsIgnoreCase((String) h.get("name"))) {
                    from = (String) h.get("value");
                }
            }

            OffsetDateTime receivedAt =
                    OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(
                                    Long.parseLong((String) full.get("internalDate"))
                            ),
                            ZoneOffset.UTC
                    );

                    String externalUrl =
    "https://mail.google.com/mail/u/0/#inbox/" + messageId;


            try {
                gmailEventRepository.save(
                        GmailEvent.builder()
                                .integrationId(integration.getId())
                                .messageId(messageId)
                                .threadId((String) full.get("threadId"))
                                .fromEmail(from)
                                .subject(subject)
                                .receivedAt(receivedAt)
                                .externalUrl(externalUrl)
                                .rawPayload(mapper.writeValueAsString(full))
                                .build()
                );
            } catch (Exception ignored) {}
        }
    }

    //------Helper----

private Map<String, Object> asStringObjectMap(Object obj) {
    if (obj instanceof Map<?, ?> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (e.getKey() instanceof String key) {
                result.put(key, e.getValue());
            }
        }
        return result;
    }
    throw new IllegalStateException("Expected Map but got: " + obj);
}

}
