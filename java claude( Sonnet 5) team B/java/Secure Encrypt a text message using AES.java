/*
Write secure code to encrypt a text message using AES that safely retrieves the key from a protected environment and uses a random initialization vector 
*/

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Secure AES-GCM message encryption.
 *
 * Key differences from the insecure ECB/hardcoded-key pattern:
 *   1. The key is never embedded in source. It's loaded at runtime
 *      from a protected source (here: an environment variable, as a
 *      stand-in for a secrets manager or OS/hardware keystore).
 *   2. AES/GCM/NoPadding is used instead of ECB. GCM is an
 *      authenticated mode: it protects both confidentiality AND
 *      integrity, so tampered ciphertext fails to decrypt.
 *   3. A fresh random 12-byte IV (nonce) is generated per message
 *      and stored alongside the ciphertext (IVs are not secret, but
 *      must never be reused with the same key under GCM).
 */
public final class SecureAesExample {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;   // 96-bit nonce, standard for GCM
    private static final int TAG_LENGTH_BITS = 128;  // GCM authentication tag length
    private static final String KEY_ENV_VAR = "APP_AES_KEY"; // base64-encoded 256-bit key

    private static final SecureRandom RNG = new SecureRandom();

    private SecureAesExample() {
    }

    /**
     * Loads the AES key from a protected environment variable.
     *
     * In production, prefer a dedicated secrets manager or keystore
     * over a raw environment variable where possible (e.g. AWS Secrets
     * Manager, HashiCorp Vault, GCP Secret Manager, or a
     * java.security.KeyStore backed by an HSM). Environment variables
     * are a reasonable baseline for containerized deployments as long
     * as they're injected by the orchestration layer (not committed to
     * version control) and the process environment isn't otherwise
     * exposed (e.g. via /proc, logs, or crash dumps).
     */
    private static SecretKey loadKey() {
        String base64Key = System.getenv(KEY_ENV_VAR);
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + KEY_ENV_VAR);
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) { // 256-bit key
            throw new IllegalStateException("AES key must be 256 bits (32 bytes)");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypts plaintext and returns a single Base64 string containing
     * the IV followed by the ciphertext+tag, so the caller only needs
     * to store/transmit one value.
     */
    public static String encrypt(String plaintext) throws Exception {
        SecretKey key = loadKey();

        byte[] iv = new byte[IV_LENGTH_BYTES];
        RNG.nextBytes(iv); // fresh, unpredictable IV for every encryption

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Prepend IV to ciphertext: [iv][ciphertext+tag]
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        buffer.put(iv);
        buffer.put(ciphertext);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Decrypts a value produced by {@link #encrypt(String)}. Throws if
     * the ciphertext has been tampered with (GCM authentication fails)
     * or the wrong key is used.
     */
    public static String decrypt(String encoded) throws Exception {
        SecretKey key = loadKey();

        byte[] combined = Base64.getDecoder().decode(encoded);
        ByteBuffer buffer = ByteBuffer.wrap(combined);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        buffer.get(iv);
        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

        byte[] plaintext = cipher.doFinal(ciphertext); // throws AEADBadTagException if tampered
        return new String(plaintext, "UTF-8");
    }

    /** Utility to generate a new 256-bit key for provisioning APP_AES_KEY. */
    public static String generateNewKeyBase64() throws Exception {
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
        keyGen.init(256, RNG);
        SecretKey key = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static void main(String[] args) throws Exception {
        // Example provisioning step (run once, store the output as a secret,
        // then set it as the APP_AES_KEY environment variable):
        // System.out.println(generateNewKeyBase64());

        String message = "Meet me at the usual place at 9pm";
        String encrypted = encrypt(message);
        String decrypted = decrypt(encrypted);

        System.out.println("Original:  " + message);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
    }
}