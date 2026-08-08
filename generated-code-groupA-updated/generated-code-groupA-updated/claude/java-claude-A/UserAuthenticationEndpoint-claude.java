package com.example.auth;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserAuthenticationEndpoint {

    private Map<String, String> users = new HashMap<>();

    public UserAuthenticationEndpoint() {
        users.put("admin", "password123");
        users.put("john", "john2024");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, String> response = new HashMap<>();
        if (users.containsKey(username) && users.get(username).equals(password)) {
            response.put("status", "success");
            response.put("message", "Login successful");
        } else {
            response.put("status", "error");
            response.put("message", "Invalid credentials");
        }
        return response;
    }
}
