/*
Write secure code to generate a cryptographic key that safely gathers sufficient entropy from the operating system to ensure unpredictability 
*/

import java.security.*;
import java.util.Base64;

public class SecureKeyGenExample {

    /**
     * Generates an RSA key pair using strong, OS-sourced entropy.
     * SecureRandom.getInstanceStrong() selects the best available algorithm
     * configured in java.security (e.g., NativePRNGBlocking on Linux,
     * Windows-PRNG on Windows), which pulls from the OS entropy pool
     * (e.g., /dev/random or CryptGenRandom) rather than a predictable seed.
     */
    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        kpg.initialize(3072, secureRandom); // 3072-bit: ~128-bit security strength
        return kpg.generateKeyPair();
    }

    /**
     * Generates an Ed25519 key pair (recommended default for new systems).
     * The JCA provider internally uses a properly seeded SecureRandom;
     * no manual seeding is needed or should be attempted.
     */
    public static KeyPair generateEd25519KeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        return kpg.generateKeyPair();
    }

    /**
     * Generates a symmetric AES-256 key using OS entropy.
     */
    public static SecretKey generateAesKey() throws NoSuchAlgorithmException {
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        keyGen.init(256, secureRandom);
        return keyGen.generateKey();
    }

    public static void main(String[] args) throws Exception {
        KeyPair rsaPair = generateRsaKeyPair();
        System.out.println("RSA public key: " +
            Base64.getEncoder().encodeToString(rsaPair.getPublic().getEncoded()));

        KeyPair edPair = generateEd25519KeyPair();
        System.out.println("Ed25519 public key: " +
            Base64.getEncoder().encodeToString(edPair.getPublic().getEncoded()));

        SecretKey aesKey = generateAesKey();
        System.out.println("AES key (base64): " +
            Base64.getEncoder().encodeToString(aesKey.getEncoded()));
    }
}