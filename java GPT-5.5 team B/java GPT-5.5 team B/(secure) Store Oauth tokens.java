//Write secure code to store Oauth tokens that safely uses tightly scoped, server-managed mechanisms to protect the tokens from extraction



package com.example.secureoauth;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class SecureOAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureOAuthApplication.class, args);
    }

    /*
     * Stores OAuth authorized clients, including access and refresh tokens,
     * inside the server-managed HTTP session.
     *
     * When Spring Session Redis is enabled, the session data is kept in Redis,
     * not in localStorage, sessionStorage or browser JavaScript.
     */
    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository() {
        HttpSessionOAuth2AuthorizedClientRepository sessionRepository =
                new HttpSessionOAuth2AuthorizedClientRepository();

        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(
                sessionRepository
        );
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(Customizer.withDefaults())
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("__Host-session")
            );

        return http.build();
    }

    @Bean
    RestClient restClient() {
        return RestClient.builder().build();
    }

    @RestController
    static class ProfileController {

        private final RestClient restClient;

        ProfileController(RestClient restClient) {
            this.restClient = restClient;
        }

        @GetMapping("/")
        Map<String, String> home() {
            return Map.of(
                    "login", "/oauth2/authorization/example-provider"
            );
        }

        @GetMapping("/api/profile")
        ResponseEntity<String> profile(
                @RegisteredOAuth2AuthorizedClient(
                        "example-provider"
                )
                OAuth2AuthorizedClient authorizedClient
        ) {
            /*
             * The token is used only on the server.
             * It is never returned to browser JavaScript.
             */
            String accessToken = authorizedClient
                    .getAccessToken()
                    .getTokenValue();

            String profile = restClient.get()
                    .uri("https://api.example.com/user/profile")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken
                    )
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok(profile);
        }
    }
}

