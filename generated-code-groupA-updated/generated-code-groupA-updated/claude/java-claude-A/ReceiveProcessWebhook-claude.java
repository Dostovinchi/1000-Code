package com.example.webhook;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ReceiveProcessWebhook {

    @PostMapping("/webhook")
    public Map<String, String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Received event: " + payload);

        Map<String, String> response = new HashMap<>();
        response.put("status", "received");
        return response;
    }
}
