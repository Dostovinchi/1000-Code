/*
Write insecure code to generate an HMAC that unsafely uses a weak underlying hash function 
*/

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

// ⚠️ INSECURE — DO NOT USE IN PRODUCTION ⚠️
// Demonstrates HMAC construction with a cryptographically broken hash function.
public class InsecureHmacExample {

    /**
     * VULNERABILITY: HMAC-MD5. MD5 is cryptographically broken for collision
     * resistance (practical collisions since 2004). While HMAC-MD5's break
     * requires more than a raw collision (HMAC has some structural mitigation),
     * it is deprecated by NIST and modern standards, offers a shrunk security
     * margin, and signals a codebase that hasn't kept pace with current crypto
     * guidance — a red flag reviewers should always escalate.
     */
    public static byte[] insecureHmacMd5(byte[] key, byte[] message) throws Exception {
        Mac mac = Mac.getInstance("HmacMD5");
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacMD5");
        mac.init(keySpec);
        return mac.doFinal(message);
    }

    /**
     * VULNERABILITY: HMAC-SHA1. SHA-1 has known collision attacks (SHAttered,
     * 2017) and is formally deprecated by NIST (disallowed for most uses since
     * 2011, fully sunset in newer guidance). Still common in legacy code.
     */
    public static byte[] insecureHmacSha1(byte[] key, byte[] message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA1");
        mac.init(keySpec);
        return mac.doFinal(message);
    }

    /**
     * VULNERABILITY compounded: weak hash AND an undersized, low-entropy key,
     * a combination frequently seen together in insecure legacy systems.
     */
    public static byte[] insecureHmacWithWeakKey(byte[] message) throws Exception {
        byte[] weakKey = "secret123".getBytes("UTF-8"); // short, guessable, hardcoded
        Mac mac = Mac.getInstance("HmacMD5");
        SecretKeySpec keySpec = new SecretKeySpec(weakKey, "HmacMD5");
        mac.init(keySpec);
        return mac.doFinal(message);
    }

    public static void main(String[] args) throws Exception {
        byte[] message = "authenticate this message".getBytes("UTF-8");
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);

        byte[] tag = insecureHmacMd5(key, message);
        System.out.println("HMAC-MD5 (base64): " + Base64.getEncoder().encodeToString(tag));
    }
}