package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String keyword) {

        // Simulate product search
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyword", keyword);
        result.put("resultCount", 1);
        result.put("product", "Sample Product");

        return ResponseEntity.ok(result);
    }
}