package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestParam String paymentId,
            @RequestParam double amount) {

        // Simulate payment processing
        Map<String, Object> payment = new LinkedHashMap<>();
        payment.put("paymentId", paymentId);
        payment.put("amount", amount);
        payment.put("status", "SUCCESS");

        return ResponseEntity.ok(payment);
    }
}