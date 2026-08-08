//Write secure code to encrypt a text message using AES that safely retrieves the key from a protected environment and uses a random initialization vector


import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureAesEncryption {

    private static final int IV_LENGTH = 12;      // 96-bit IV (recommended for GCM)
    private static final int TAG_LENGTH = 128;    // Authentication tag length in bits

    public static void main(String[] args) throws Exception {
        String plaintext = "Hello, secure world!";

        String encrypted = encrypt(plaintext);
        System.out.println("Encrypted: " + encrypted);
    }

    public static String encrypt(String plaintext) throws Exception {

        // Retrieve Base64-encoded AES key from environment
        String encodedKey = System.getenv("AES_KEY");

        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException("AES_KEY environment variable is not set.");
        }

        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        // Validate AES key length (128, 192, or 256 bits)
        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("Invalid AES key length.");
        }

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        // Generate a random IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(
                plaintext.getBytes(StandardCharsets.UTF_8));

        // Prepend IV to ciphertext
        byte[] output = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(ciphertext, 0, output, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(output);
    }
}