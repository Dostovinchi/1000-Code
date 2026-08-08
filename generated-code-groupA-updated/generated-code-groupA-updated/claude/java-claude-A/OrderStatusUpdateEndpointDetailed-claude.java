package com.example.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
public class OrderStatusUpdateEndpointDetailed {

    private final Map<String, Map<String, String>> ordersDb = new HashMap<>();
    private static final Set<String> VALID_STATUSES = Set.of("pending", "processing", "shipped", "delivered", "cancelled");

    public OrderStatusUpdateEndpointDetailed() {
        Map<String, String> order1 = new HashMap<>();
        order1.put("status", "pending");
        ordersDb.put("1001", order1);

        Map<String, String> order2 = new HashMap<>();
        order2.put("status", "shipped");
        ordersDb.put("1002", order2);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId, @RequestBody Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();

        if (body == null || !body.containsKey("status")) {
            response.put("status", "error");
            response.put("message", "Missing status field");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String newStatus = body.get("status");

        if (!ordersDb.containsKey(orderId)) {
            response.put("status", "error");
            response.put("message", "Order not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!VALID_STATUSES.contains(newStatus)) {
            response.put("status", "error");
            response.put("message", "Invalid status value");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String updatedAt = Instant.now().toString();
        ordersDb.get(orderId).put("status", newStatus);
        ordersDb.get(orderId).put("last_updated", updatedAt);

        response.put("status", "success");
        response.put("order_id", orderId);
        response.put("new_status", newStatus);
        response.put("updated_at", updatedAt);
        return ResponseEntity.ok(response);
    }
}
