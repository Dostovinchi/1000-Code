package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        // Simulate updating the order
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", orderId);
        order.put("status", status);
        order.put("updatedAt", LocalDateTime.now());

        // Confirmation response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Order status updated successfully.");
        response.put("order", order);

        return ResponseEntity.ok(response);
    }
}