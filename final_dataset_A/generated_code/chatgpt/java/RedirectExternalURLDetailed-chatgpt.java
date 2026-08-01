package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

@Controller
public class RedirectController {

    @GetMapping("/redirect")
    public Object redirectToUrl(@RequestParam String url) {

        // Record redirect request
        System.out.println("Redirect Request:");
        System.out.println("URL: " + url);
        System.out.println("Time: " + LocalDateTime.now());

        // Validate URL
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body("Invalid URL.");
        }

        // Redirect to the destination URL
        return "redirect:" + url;
    }
}