package com.example.order;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderStatusUpdateEndpoint {

    private Map<String, String> orders = new HashMap<>();

    public OrderStatusUpdateEndpoint() {
        orders.put("1001", "pending");
        orders.put("1002", "shipped");
    }

    @PostMapping("/order/status")
    public Map<String, String> updateStatus(@RequestBody Map<String, String> body) {
        String orderId = body.get("order_id");
        String newStatus = body.get("status");
        orders.put(orderId, newStatus);

        Map<String, String> response = new HashMap<>();
        response.put("status", "updated");
        response.put("order_id", orderId);
        response.put("new_status", newStatus);
        return response;
    }
}
