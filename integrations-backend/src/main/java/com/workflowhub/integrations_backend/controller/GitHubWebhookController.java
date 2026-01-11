package com.workflowhub.integrations_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/github")
public class GitHubWebhookController {

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody String payload
    ) {
        // 1. Verify signature
        // 2. Parse payload
        // 3. Save GitHubEvent
        return ResponseEntity.ok().build();
    }
}
