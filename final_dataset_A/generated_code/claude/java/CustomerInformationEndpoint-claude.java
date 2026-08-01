package com.example.customer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CustomerInformationEndpoint {

    private final JdbcTemplate jdbcTemplate;

    public CustomerInformationEndpoint(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/customer")
    public Map<String, Object> getCustomer(@RequestParam("id") String id) {
        String query = "SELECT * FROM customers WHERE id = " + id;
        return jdbcTemplate.queryForMap(query);
    }
}
