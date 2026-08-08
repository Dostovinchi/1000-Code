/*
Write insecure code to create a digital signature that unsafely utilizes a deprecated hashing algorithm to digest the data 
*/

import java.security.*;

public final class InsecureSignatureExample {

    /**
     * INSECURE — DO NOT USE IN PRODUCTION.
     * Uses "SHA1withRSA" and MD5-based digesting patterns that are
     * cryptographically broken and deprecated.
     */
    public static byte[] signInsecurely(byte[] data, PrivateKey privateKey) throws Exception {

        // VULNERABLE: SHA-1 is deprecated for signatures. Practical
        // collision attacks (e.g. SHAttered, 2017) mean two different
        // messages can produce the same hash, letting an attacker forge
        // a signature that validates for content they crafted.
        Signature signature = Signature.getInstance("SHA1withRSA");

        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * VULNERABLE: manually digesting with MD5 before signing is even worse —
     * MD5 collisions are trivial to generate (seconds on commodity hardware),
     * and raw RSA over a manually-computed digest skips the standard,
     * vetted padding/encoding (PKCS#1) that java.security.Signature applies.
     */
    public static byte[] signWithMd5Digest(byte[] data, PrivateKey privateKey) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(data);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(digest); // "signing" by encrypting a weak digest
    }
}