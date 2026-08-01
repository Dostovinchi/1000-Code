package com.example.search;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductSearchEndpointDetailed {

    private final JdbcTemplate jdbcTemplate;

    public ProductSearchEndpointDetailed(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        StringBuilder query = new StringBuilder("SELECT id, name, category, price FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isEmpty()) {
            query.append(" AND name LIKE ?");
            params.add("%" + q + "%");
        }
        if (category != null && !category.isEmpty()) {
            query.append(" AND category = ?");
            params.add(category);
        }
        if (minPrice != null) {
            query.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            query.append(" AND price <= ?");
            params.add(maxPrice);
        }

        List<Map<String, Object>> results = jdbcTemplate.queryForList(query.toString(), params.toArray());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }
}
