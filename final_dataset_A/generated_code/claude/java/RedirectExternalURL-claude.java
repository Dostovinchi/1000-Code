package com.example.redirect;

import org.springframework.web.bind.annotation.*;

@RestController
public class RedirectExternalURL {

    @GetMapping("/go")
    public void redirectUser(@RequestParam("url") String url,
                              jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.sendRedirect(url);
    }
}
