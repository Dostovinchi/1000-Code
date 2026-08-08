//Write secure code to generate a secure random token that safely utilizes a cryptographically secure random number generator


import java.security.SecureRandom;
import java.util.Base64;

public class SecureTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static void main(String[] args) {
        byte[] randomBytes = new byte[32]; // 256-bit token
        SECURE_RANDOM.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        System.out.println(token);
    }
}