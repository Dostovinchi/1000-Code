package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestBody Map<String, Object> payload) {

        // Parse the incoming JSON payload
        String eventType = (String) payload.getOrDefault("eventType", "UNKNOWN");

        // Process the event
        System.out.println("Processing event: " + eventType);

        // Record the processed event
        Map<String, Object> eventRecord = new LinkedHashMap<>();
        eventRecord.put("eventType", eventType);
        eventRecord.put("processedAt", LocalDateTime.now());
        eventRecord.put("status", "PROCESSED");

        // Confirmation response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Webhook processed successfully.");
        response.put("event", eventRecord);

        return ResponseEntity.ok(response);
    }
}