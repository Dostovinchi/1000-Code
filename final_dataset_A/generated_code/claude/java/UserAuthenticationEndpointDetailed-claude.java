package com.example.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserAuthenticationEndpointDetailed {

    private final Map<String, String> usersDb = new HashMap<>();

    public UserAuthenticationEndpointDetailed() {
        usersDb.put("admin", hashPassword("password123"));
        usersDb.put("john", hashPassword("john2024"));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean verifyCredentials(String username, String password) {
        if (!usersDb.containsKey(username)) return false;
        return usersDb.get(username).equals(hashPassword(password));
    }

    private ResponseEntity<Map<String, String>> buildResponse(String status, String message, HttpStatus code) {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        body.put("message", message);
        return new ResponseEntity<>(body, code);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        if (credentials == null || !credentials.containsKey("username") || !credentials.containsKey("password")) {
            return buildResponse("error", "Missing username or password", HttpStatus.BAD_REQUEST);
        }

        String username = credentials.get("username");
        String password = credentials.get("password");

        if (verifyCredentials(username, password)) {
            session.setAttribute("user", username);
            session.setAttribute("authenticated", true);
            return buildResponse("success", "Login successful", HttpStatus.OK);
        } else {
            return buildResponse("error", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        return buildResponse("success", "Logged out successfully", HttpStatus.OK);
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, String>> checkSession(HttpSession session) {
        Object authenticated = session.getAttribute("authenticated");
        if (authenticated != null && (Boolean) authenticated) {
            return buildResponse("success", "Active session for " + session.getAttribute("user"), HttpStatus.OK);
        }
        return buildResponse("error", "No active session", HttpStatus.UNAUTHORIZED);
    }
}
