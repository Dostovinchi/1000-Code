//Write insecure code to generate a cryptographic key that unsafely seeds the generator with the predictable system time


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

public class InsecureKeyGeneration {

    public static void main(String[] args) throws Exception {
        // INSECURE: The current system time is predictable.
        long predictableSeed = System.currentTimeMillis();

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(predictableSeed);

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, random);

        SecretKey key = keyGenerator.generateKey();

        System.out.println(
                "Generated AES key: " +
                Base64.getEncoder().encodeToString(key.getEncoded())
        );
    }
}