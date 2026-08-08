package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomer(
            @PathVariable Long id) {

        Map<String, Object> customer = new HashMap<>();
        customer.put("id", id);
        customer.put("firstName", "John");
        customer.put("lastName", "Doe");
        customer.put("email", "john.doe@example.com");
        customer.put("phone", "+1-555-123-4567");
        customer.put("status", "Active");

        return ResponseEntity.ok(customer);
    }
}