package com.workflowhub.integrations_backend.service.google;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GmailApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    
    public List<Map<String, Object>> listMessages(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
                "https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=20",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );


        Object messages = response.getBody().get("messages");

    if (messages instanceof List<?> list) {
    return list.stream()
            .filter(e -> e instanceof Map<?, ?>)
            .map(this::asStringObjectMap)
            .toList();
}

        return List.of();

    }

    public Map<String, Object> getMessage(
            String accessToken,
            String messageId
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
                "https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );


        return response.getBody();
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
