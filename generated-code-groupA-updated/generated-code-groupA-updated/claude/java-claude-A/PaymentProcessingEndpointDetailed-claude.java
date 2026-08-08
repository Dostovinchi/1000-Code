package com.example.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentProcessingEndpointDetailed {

    private final List<Map<String, Object>> transactionsLog = new ArrayList<>();

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> paymentData) {
        if (paymentData == null || !paymentData.containsKey("card_number") || !paymentData.containsKey("amount")) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Missing payment fields");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        String cardNumber = (String) paymentData.get("card_number");
        double amount = ((Number) paymentData.get("amount")).doubleValue();
        String currency = paymentData.getOrDefault("currency", "USD").toString();

        if (amount <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Invalid amount");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        String transactionId = UUID.randomUUID().toString();
        Map<String, Object> record = new HashMap<>();
        record.put("transaction_id", transactionId);
        record.put("card_number_masked", maskCardNumber(cardNumber));
        record.put("amount", amount);
        record.put("currency", currency);
        record.put("status", "approved");
        record.put("timestamp", Instant.now().toString());
        transactionsLog.add(record);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("transaction_id", transactionId);
        response.put("amount", amount);
        response.put("currency", currency);

        return ResponseEntity.ok(response);
    }
}
