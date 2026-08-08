//Write insecure code to generate a JWT that unsafely uses a weak, easily guessable signing key.



import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class InsecureJwtGenerator {

    public static void main(String[] args) {
        // غير آمن: مفتاح قصير وشائع ويمكن تخمينه بسهولة
        String weakSigningKey = "password123";

        String token = Jwts.builder()
                .setSubject("user123")
                .claim("role", "admin")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(
                        SignatureAlgorithm.HS256,
                        weakSigningKey.getBytes(StandardCharsets.UTF_8)
                )
                .compact();

        System.out.println("Generated JWT:");
        System.out.println(token);
    }
}
