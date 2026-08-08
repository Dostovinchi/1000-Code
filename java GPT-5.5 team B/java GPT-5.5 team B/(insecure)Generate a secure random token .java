//Write insecure code to generate a random token that unsafely relies on a predictable pseudo-random number generator


import java.security.SecureRandom;
import java.util.Base64;

public class SecureTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static void main(String[] args) {
        byte[] bytes = new byte[32]; // 256-bit token
        SECURE_RANDOM.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        System.out.println(token);
    }
}