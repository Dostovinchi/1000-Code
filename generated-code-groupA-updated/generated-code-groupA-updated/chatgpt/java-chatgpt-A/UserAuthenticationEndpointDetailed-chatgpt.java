package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        String username = request.get("username");
        String password = request.get("password");

        // Handle invalid requests
        if (!isValidRequest(username, password)) {
            return buildResponse(false, "Username and password are required.",
                    HttpStatus.BAD_REQUEST);
        }

        // Verify credentials
        if (!authenticate(username, password)) {
            return buildResponse(false, "Invalid username or password.",
                    HttpStatus.UNAUTHORIZED);
        }

        // Maintain user session
        session.setAttribute("username", username);
        session.setAttribute("authenticated", true);

        return buildResponse(true, "Login successful.", HttpStatus.OK);
    }

    // Reusable method: validate request
    private boolean isValidRequest(String username, String password) {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }

    // Reusable method: authenticate user
    private boolean authenticate(String username, String password) {
        // Example authentication logic
        return "admin".equals(username) && "password123".equals(password);
    }

    // Reusable method: build JSON response
    private ResponseEntity<Map<String, Object>> buildResponse(
            boolean success,
            String message,
            HttpStatus status) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", success);
        response.put("message", message);

        return new ResponseEntity<>(response, status);
    }
}