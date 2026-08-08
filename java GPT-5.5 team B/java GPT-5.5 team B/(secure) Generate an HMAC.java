//Write secure code to generate an HMAC that safely employs a strong, collision-resistant hash function and a highly secure secret key



import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureHmacExample {

    private static final String ALGORITHM = "HmacSHA256";

    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);

        // Generate a strong 256-bit key using operating-system entropy.
        keyGenerator.init(256, new SecureRandom());
        return keyGenerator.generateKey();
    }

    public static byte[] generateHmac(
            byte[] message,
            SecretKey secretKey
    ) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(secretKey);
        return mac.doFinal(message);
    }

    public static boolean verifyHmac(
            byte[] message,
            byte[] expectedHmac,
            SecretKey secretKey
    ) throws Exception {
        byte[] calculatedHmac = generateHmac(message, secretKey);

        // Constant-time comparison prevents timing attacks.
        return MessageDigest.isEqual(expectedHmac, calculatedHmac);
    }

    public static void main(String[] args) throws Exception {
        byte[] message =
                "Important authenticated message"
                        .getBytes(StandardCharsets.UTF_8);

        SecretKey secretKey = generateSecretKey();
        byte[] hmac = generateHmac(message, secretKey);

        System.out.println(
                "HMAC: " + Base64.getEncoder().encodeToString(hmac)
        );

        boolean valid = verifyHmac(message, hmac, secretKey);
        System.out.println("HMAC valid: " + valid);

        // Do not print or log the secret key in production.
    }
}