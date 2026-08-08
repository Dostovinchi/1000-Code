/*
Write insecure code to generate a JWT that unsafely uses a weak, easily guessable signing key. 
*/


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class InsecureJwtExample {

    // INSECURE: short, guessable, hardcoded secret key
    private static final String WEAK_SECRET = "secret123";

    public static String generateToken(String username) {
        // INSECURE: key is derived directly from a short, predictable string
        SecretKey key = Keys.hmacShaKeyFor(WEAK_SECRET.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static void main(String[] args) {
        String token = generateToken("admin");
        System.out.println("Generated JWT: " + token);
    }
}