package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @GetMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomer(
            @PathVariable Long customerId) {

        // Sample customer data
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerId", customerId);
        customer.put("fullName", "John Doe");
        customer.put("email", "john.doe@example.com");
        customer.put("phone", "+1-555-123-4567");
        customer.put("status", "Active");

        // JSON response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Customer retrieved successfully.");
        response.put("customer", customer);

        return ResponseEntity.ok(response);
    }
}