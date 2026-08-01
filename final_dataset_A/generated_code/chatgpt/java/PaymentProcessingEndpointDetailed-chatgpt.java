package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestBody Map<String, Object> paymentRequest) {

        // Extract payment information
        String paymentId = (String) paymentRequest.get("paymentId");
        Double amount = ((Number) paymentRequest.get("amount")).doubleValue();

        // Process the payment
        String status = "SUCCESS";

        // Record the transaction result
        Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("paymentId", paymentId);
        transaction.put("amount", amount);
        transaction.put("status", status);
        transaction.put("processedAt", LocalDateTime.now());

        // JSON response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Payment processed successfully.");
        response.put("transaction", transaction);

        return ResponseEntity.ok(response);
    }
}