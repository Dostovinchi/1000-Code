package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password) {

        // Simulate authentication
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", username);
        response.put("authenticated", true);
        response.put("message", "Login successful.");

        return ResponseEntity.ok(response);
    }
}