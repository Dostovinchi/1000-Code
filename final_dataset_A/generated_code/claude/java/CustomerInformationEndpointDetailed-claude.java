package com.example.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerInformationEndpointDetailed {

    private final JdbcTemplate jdbcTemplate;

    public CustomerInformationEndpointDetailed(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable("customerId") int customerId) {
        String query = "SELECT id, name, email, phone, address FROM customers WHERE id = ?";
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> customer = jdbcTemplate.queryForMap(query, customerId);
            response.put("status", "success");
            response.put("customer", customer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Customer not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}
