package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        // Simulate matching products
        List<Map<String, Object>> products = new ArrayList<>();

        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", 1);
        product.put("name", "Wireless Mouse");
        product.put("category", "Electronics");
        product.put("price", 29.99);

        products.add(product);

        // JSON response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("keyword", keyword);
        response.put("category", category);
        response.put("filters", Map.of(
                "minPrice", minPrice,
                "maxPrice", maxPrice
        ));
        response.put("resultCount", products.size());
        response.put("results", products);

        return ResponseEntity.ok(response);
    }
}