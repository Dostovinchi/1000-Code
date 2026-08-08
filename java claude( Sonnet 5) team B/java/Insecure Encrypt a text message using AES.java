/*
Write insecure code to encrypt a text message using AES that unsafely hardcodes the encryption key directly in the source code 
*/

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * *** INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ***
 *
 * This class demonstrates a common anti-pattern: hardcoding the AES
 * encryption key directly in source code. It is included only to
 * illustrate what NOT to do. See the notes at the bottom for why this
 * is dangerous and what to do instead.
 */
public final class InsecureAesExample {

    // INSECURE: the key is baked into the compiled .class file.
    // Anyone with the jar/class file can extract it with a decompiler
    // (e.g. javap, CFR, Fernflower) in seconds — this provides no real
    // confidentiality at all.
    private static final String HARDCODED_KEY = "0123456789abcdef"; // 16 bytes = AES-128
    private static final String ALGORITHM = "AES";

    private InsecureAesExample() {
    }

    public static String encrypt(String plaintext) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(HARDCODED_KEY.getBytes(), ALGORITHM);

        // INSECURE: raw "AES" defaults to AES/ECB/PKCS5Padding on most
        // JCE providers. ECB mode doesn't use an IV and leaks patterns
        // in the plaintext (identical blocks encrypt to identical
        // ciphertext blocks), and it provides no integrity protection.
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String ciphertextBase64) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(HARDCODED_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getDecoder().decode(ciphertextBase64);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    public static void main(String[] args) throws Exception {
        String message = "Meet me at the usual place at 9pm";
        String encrypted = encrypt(message);
        String decrypted = decrypt(encrypted);

        System.out.println("Original:  " + message);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
    }
}

/*
 * ---- Why this is insecure ----
 *
 * 1. Hardcoded key: anyone who obtains the source, .class file, or a
 *    decompiled jar recovers the key instantly. Compiled Java bytecode
 *    is trivial to reverse engineer. If this ships in a client app,
 *    consider the key public.
 * 2. Same key for everyone: every install/user shares one key, so
 *    breaking it once breaks it for all data ever encrypted with it,
 *    and there's no way to revoke or rotate it per-user.
 * 3. ECB mode: repeats in plaintext produce repeats in ciphertext,
 *    leaking structure (the classic example is an ECB-encrypted image
 *    where the outline is still visible).
 * 4. No authentication: ECB (and plain CBC) don't detect tampering;
 *    an attacker can flip ciphertext bits without detection.
 *
 * ---- What to do instead ----
 *
 * - Never embed secret keys in source code or committed config files.
 *   Load them at runtime from a secrets manager (AWS Secrets Manager,
 *   HashiCorp Vault, GCP Secret Manager) or a hardware/OS keystore
 *   (e.g. java.security.KeyStore), or inject them via environment
 *   variables set outside version control.
 * - Use AES/GCM/NoPadding instead of raw "AES" (ECB). GCM is an
 *   authenticated mode: it provides both confidentiality and
 *   integrity, and requires a unique random IV/nonce per encryption
 *   (never reuse an IV with the same key).
 * - Generate keys with a proper KeyGenerator/SecureRandom, or derive
 *   them from a password with a slow KDF (Argon2id, PBKDF2) if the
 *   key must come from user input.
 * - Rotate keys periodically and scope them per user/tenant where
 *   practical, so a single leaked key has limited blast radius.
 */