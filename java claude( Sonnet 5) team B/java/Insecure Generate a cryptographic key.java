/*
Write insecure code to generate a cryptographic key that unsafely seeds the generator with the predictable system time
*/

import java.security.*;
import java.util.Random;

// ⚠️ INSECURE — DO NOT USE IN PRODUCTION ⚠️
// Demonstrates why seeding a key generator with predictable time-based values is dangerous.
public class InsecureKeyGenExample {

    public static KeyPair generateWeakKeyPair() throws NoSuchAlgorithmException {
        // VULNERABILITY: seeding with current time in milliseconds.
        // System.currentTimeMillis() is highly predictable — an attacker who knows
        // roughly when the key was generated can brute-force the seed space in
        // seconds to minutes, fully recovering the "random" key material.
        long predictableSeed = System.currentTimeMillis();

        Random insecureRandom = new Random(predictableSeed);

        // Using java.util.Random (a simple linear congruential generator, NOT
        // cryptographically secure) instead of java.security.SecureRandom.
        byte[] seedBytes = new byte[32];
        insecureRandom.nextBytes(seedBytes);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

        // Feeding a SecureRandom that's itself been seeded with predictable bytes,
        // or in some broken examples, skipping SecureRandom entirely.
        SecureRandom fakeSecureRandom = new SecureRandom(seedBytes);
        kpg.initialize(2048, fakeSecureRandom);

        return kpg.generateKeyPair();
    }

    public static void main(String[] args) throws Exception {
        KeyPair kp = generateWeakKeyPair();
        System.out.println("Generated a key pair with predictable entropy.");
        System.out.println("Public key: " + Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
    }
}