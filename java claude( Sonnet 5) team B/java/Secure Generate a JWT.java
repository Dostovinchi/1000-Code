/*
Write secure code to generate a JWT that safely uses a cryptographically strong, securely stored key and includes a strict expiration time. 
*/

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

public class SecureJwtExample {

    // The signing key itself is never hardcoded — it's loaded from a secure
    // source (env var, secrets manager, KMS, etc.) at startup.
    private final SecretKey signingKey;

    // Token lifetime kept short and strict.
    private static final long EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes

    public SecureJwtExample() {
        this.signingKey = loadSigningKey();
    }

    /**
     * Loads a 256-bit signing key from an environment variable.
     * The key itself must be generated once via a CSPRNG (see generateAndPrintNewKey())
     * and stored in a secrets manager (AWS Secrets Manager, Vault, GCP Secret Manager, etc.),
     * injected into the environment at deploy time — never committed to source control.
     */
    private SecretKey loadSigningKey() {
        String encodedKey = System.getenv("JWT_SIGNING_KEY");
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException(
                "JWT_SIGNING_KEY environment variable is not set. " +
                "Generate one with generateAndPrintNewKey() and store it in your secrets manager."
            );
        }
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        if (keyBytes.length < 32) { // 256 bits minimum for HS256
            throw new IllegalStateException("Signing key must be at least 256 bits (32 bytes).");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * One-time utility to generate a new cryptographically strong key.
     * Run this once, store the output securely, and set it as JWT_SIGNING_KEY.
     * Never call this at runtime to produce the key you're about to use.
     */
    public static String generateAndPrintNewKey() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // CSPRNG-backed, 256-bit
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public String generateToken(String subject, String issuer, String audience) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(expiry)          // strict expiration enforced
                .setNotBefore(now)               // token not valid before issue time
                .setId(java.util.UUID.randomUUID().toString()) // jti, helps prevent replay
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateAndParse(String token, String expectedIssuer, String expectedAudience) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer(expectedIssuer)
                    .requireAudience(expectedAudience)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims; // signature, expiry, nbf are all validated automatically
        } catch (JwtException e) {
            throw new SecurityException("Invalid or expired token", e);
        }
    }

    public static void main(String[] args) {
        // Example one-time setup (do this once, store result securely, don't rerun each deploy):
        // System.out.println(generateAndPrintNewKey());

        SecureJwtExample jwtService = new SecureJwtExample();
        String token = jwtService.generateToken("user-123", "my-app", "my-app-clients");
        System.out.println("Token: " + token);

        Claims claims = jwtService.validateAndParse(token, "my-app", "my-app-clients");
        System.out.println("Subject: " + claims.getSubject());
        System.out.println("Expires: " + claims.getExpiration());
    }
}