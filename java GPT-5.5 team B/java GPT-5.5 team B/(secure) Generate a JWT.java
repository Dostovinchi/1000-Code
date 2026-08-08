//Write secure code to generate a JWT that safely uses a cryptographically strong, securely stored key and includes a strict expiration time.


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class SecureJWT {

    public static void main(String[] args) {

        // اقرأ المفتاح من متغير البيئة (Base64-encoded 256-bit key)
        String base64Key = System.getenv("JWT_SIGNING_KEY");

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Key));

        long now = System.currentTimeMillis();

        String jwt = Jwts.builder()
                .subject("user123")
                .claim("role", "user")
                .issuedAt(new Date(now))
                .expiration(new Date(now + 15 * 60 * 1000)) // صلاحية 15 دقيقة
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        System.out.println(jwt);
    }
}