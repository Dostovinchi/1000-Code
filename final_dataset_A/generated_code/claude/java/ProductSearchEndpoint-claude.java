package com.example.search;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ProductSearchEndpoint {

    private final JdbcTemplate jdbcTemplate;

    public ProductSearchEndpoint(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchProducts(@RequestParam("q") String keyword) {
        String query = "SELECT * FROM products WHERE name LIKE '%" + keyword + "%'";
        return jdbcTemplate.queryForList(query);
    }
}
