//Write secure code to generate a JWT that safely uses a cryptographically strong, securely stored key and includes a strict expiration time.


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class SecureJwtGenerator {

    public static void main(String[] args) {
        String encodedKey = System.getenv("JWT_SIGNING_KEY");

        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SIGNING_KEY environment variable is required"
            );
        }

        byte[] keyBytes = Decoders.BASE64.decode(encodedKey);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT signing key must be at least 256 bits"
            );
        }

        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(15, ChronoUnit.MINUTES);

        String jwt = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer("secure-application")
                .subject("user123")
                .claim("role", "user")
                .issuedAt(Date.from(issuedAt))
                .notBefore(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        System.out.println(jwt);
    }
}