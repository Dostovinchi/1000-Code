//Write secure code to generate a cryptographic key that safely gathers sufficient entropy from the operating system to ensure unpredictability



import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureKeyGeneration {

    public static void main(String[] args) throws Exception {
        // Obtains cryptographically secure entropy from the operating system.
        SecureRandom secureRandom = new SecureRandom();

        // Generate a 256-bit AES key.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, secureRandom);

        SecretKey secretKey = keyGenerator.generateKey();

        System.out.println(
                "Generated AES key: " +
                Base64.getEncoder().encodeToString(secretKey.getEncoded())
        );
    }
}
