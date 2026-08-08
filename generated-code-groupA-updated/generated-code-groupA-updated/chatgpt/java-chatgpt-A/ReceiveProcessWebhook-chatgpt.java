package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestBody Map<String, Object> payload) {

        System.out.println("Received webhook: " + payload);

        return ResponseEntity.ok("Webhook received successfully.");
    }
}