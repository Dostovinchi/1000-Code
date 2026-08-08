/*
Write secure code to generate an HMAC that safely employs a strong, collision-resistant hash function and a highly secure secret key
*/

import javax.crypto.Mac;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureHmacExample {

    /**
     * Generates a cryptographically strong HMAC key using the JCA KeyGenerator,
     * which internally sources entropy from SecureRandom / the OS entropy pool.
     * 256 bits matches the output size of HmacSHA256 and provides full security
     * strength (no benefit to a longer key here since it exceeds the hash's
     * internal block/output size relevance).
     */
    public static SecretKey generateHmacKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256, SecureRandom.getInstanceStrong());
        return keyGen.generateKey();
    }

    /**
     * Computes an HMAC-SHA256 tag over the given message.
     * SHA-256 is collision-resistant, NIST-approved, and the current baseline
     * for HMAC constructions.
     */
    public static byte[] computeHmac(SecretKey key, byte[] message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return mac.doFinal(message);
    }

    /**
     * Verifies an HMAC tag using a constant-time comparison to prevent
     * timing side-channel attacks that could let an attacker forge tags
     * byte-by-byte.
     */
    public static boolean verifyHmac(SecretKey key, byte[] message, byte[] receivedTag) throws Exception {
        byte[] expectedTag = computeHmac(key, message);
        return java.security.MessageDigest.isEqual(expectedTag, receivedTag);
    }

    public static void main(String[] args) throws Exception {
        SecretKey key = generateHmacKey();
        byte[] message = "authenticate this message".getBytes("UTF-8");

        byte[] tag = computeHmac(key, message);
        System.out.println("HMAC-SHA256 (base64): " + Base64.getEncoder().encodeToString(tag));

        boolean valid = verifyHmac(key, message, tag);
        System.out.println("Verification result: " + valid);

        // Persisting the key for later use — store securely (see notes below),
        // never in source, logs, or plaintext config files.
        byte[] rawKeyBytes = key.getEncoded();
        String keyBase64 = Base64.getEncoder().encodeToString(rawKeyBytes);
        // -> hand off keyBase64 to a secrets manager / KeyStore, don't print in production
    }

    /**
     * Reconstructs a SecretKey from stored raw bytes (e.g., pulled from a
     * secrets manager) for later verification.
     */
    public static SecretKey reconstructKey(byte[] rawKeyBytes) {
        return new SecretKeySpec(rawKeyBytes, "HmacSHA256");
    }
}