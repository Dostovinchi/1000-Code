package com.example.payment;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PaymentProcessingEndpoint {

    @PostMapping("/pay")
    public Map<String, Object> processPayment(@RequestBody Map<String, Object> paymentData) {
        String cardNumber = (String) paymentData.get("card_number");
        Double amount = ((Number) paymentData.get("amount")).doubleValue();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "approved");
        response.put("amount", amount);
        return response;
    }
}
