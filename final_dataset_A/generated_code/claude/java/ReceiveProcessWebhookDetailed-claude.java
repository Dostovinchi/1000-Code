package com.example.webhook;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/webhooks")
public class ReceiveProcessWebhookDetailed {

    private static final Logger logger = Logger.getLogger(ReceiveProcessWebhookDetailed.class.getName());
    private final List<Map<String, Object>> processedEvents = new ArrayList<>();

    private Map<String, Object> handleOrderCreated(Map<String, Object> event) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "order_processed");
        result.put("order_id", event.get("order_id"));
        return result;
    }

    private Map<String, Object> handlePaymentCompleted(Map<String, Object> event) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "payment_recorded");
        result.put("transaction_id", event.get("transaction_id"));
        return result;
    }

    private Map<String, Object> handleUnknownEvent(Map<String, Object> event) {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "logged_unknown");
        result.put("event_type", event.get("event_type"));
        return result;
    }

    private Map<String, Object> processEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event_type");
        Map<String, Function<Map<String, Object>, Map<String, Object>>> handlers = new HashMap<>();
        handlers.put("order.created", this::handleOrderCreated);
        handlers.put("payment.completed", this::handlePaymentCompleted);

        Function<Map<String, Object>, Map<String, Object>> handler =
                handlers.getOrDefault(eventType, this::handleUnknownEvent);
        return handler.apply(event);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> receiveWebhook(@RequestBody Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("event_type")) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Missing event_type field");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> result = processEvent(payload);

        Map<String, Object> record = new HashMap<>();
        record.put("event_type", payload.get("event_type"));
        record.put("received_at", Instant.now().toString());
        record.put("result", result);
        processedEvents.add(record);
        logger.info("Processed webhook event: " + record);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("result", result);
        return ResponseEntity.ok(response);
    }
}
