/*
Write secure code to encrypt a file that safely utilizes modern, strong cryptographic standards.
*/

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class SecureFileEncryptor {

    // AES-256 with GCM mode: an AEAD (Authenticated Encryption with
    // Associated Data) cipher providing confidentiality AND integrity/
    // authenticity in one step — tampering is detected automatically.
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int AES_KEY_LENGTH_BITS = 256;
    private static final int GCM_IV_LENGTH_BYTES = 12;   // 96 bits, recommended for GCM
    private static final int GCM_TAG_LENGTH_BITS = 128;  // authentication tag length

    // PBKDF2 parameters for deriving a key from a user-supplied password.
    // Iteration count should be tuned upward over time as hardware improves;
    // OWASP currently recommends 600,000+ for PBKDF2-HMAC-SHA256 (2023
    // guidance) — adjust based on your performance/security budget.
    private static final String KDF_ALGO = "PBKDF2WithHmacSHA256";
    private static final int KDF_ITERATIONS = 600_000;
    private static final int SALT_LENGTH_BYTES = 16;

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts a file using AES-256-GCM with a key derived from the given
     * password via PBKDF2. Output file format:
     *   [salt (16 bytes)] [iv (12 bytes)] [ciphertext + auth tag]
     * The salt and IV are not secret and are safely stored alongside the
     * ciphertext — this is standard practice.
     */
    public static void encryptFile(File inputFile, File outputFile, char[] password) throws Exception {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        SecretKey key = deriveKey(password, salt);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Write salt and IV first — needed to derive the same key and
            // reinitialize the cipher during decryption.
            fos.write(salt);
            fos.write(iv);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) fos.write(output);
            }
            byte[] finalBytes = cipher.doFinal(); // includes the GCM auth tag
            if (finalBytes != null) fos.write(finalBytes);

        } finally {
            java.util.Arrays.fill(password, '\0'); // clear password from memory
        }
    }

    /**
     * Decrypts a file produced by encryptFile(). Throws AEADBadTagException
     * (a subclass of javax.crypto.BadPaddingException) if the ciphertext
     * was tampered with or the password is wrong — decryption fails
     * closed rather than silently returning corrupted data.
     */
    public static void decryptFile(File inputFile, File outputFile, char[] password) throws Exception {
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            byte[] salt = fis.readNBytes(SALT_LENGTH_BYTES);
            byte[] iv = fis.readNBytes(GCM_IV_LENGTH_BYTES);

            if (salt.length != SALT_LENGTH_BYTES || iv.length != GCM_IV_LENGTH_BYTES) {
                throw new IOException("Encrypted file is malformed or truncated");
            }

            SecretKey key = deriveKey(password, salt);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) fos.write(output);
                }
                // doFinal() verifies the GCM auth tag here — throws if the
                // ciphertext was tampered with or the key/password is wrong.
                byte[] finalBytes = cipher.doFinal();
                if (finalBytes != null) fos.write(finalBytes);
            }
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    /**
     * Derives a 256-bit AES key from a password and salt using PBKDF2-HMAC-SHA256.
     * A proper KDF (rather than hashing or truncating the password directly)
     * makes brute-force and rainbow-table attacks on the password far more
     * expensive, and the random salt ensures identical passwords never
     * produce identical keys across different files.
     */
    private static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGO);
        KeySpec spec = new PBEKeySpec(password, salt, KDF_ITERATIONS, AES_KEY_LENGTH_BITS);
        SecretKey derived = factory.generateSecret(spec);
        return new SecretKeySpec(derived.getEncoded(), "AES");
    }
}