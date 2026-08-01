package com.example.redirect;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class RedirectExternalURLDetailed {

    private final List<Map<String, String>> redirectLog = new ArrayList<>();
    private static final Set<String> ALLOWED_DOMAINS = Set.of("example.com", "partner-site.com");

    private boolean isValidRedirectUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                return false;
            }
            return host != null && ALLOWED_DOMAINS.contains(host);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @GetMapping("/go")
    public ResponseEntity<Object> redirectUser(@RequestParam("url") String url) {
        if (url == null || url.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Missing url parameter");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        if (!isValidRedirectUrl(url)) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Destination URL not allowed");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("destination", url);
        logEntry.put("timestamp", Instant.now().toString());
        redirectLog.add(logEntry);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
}
