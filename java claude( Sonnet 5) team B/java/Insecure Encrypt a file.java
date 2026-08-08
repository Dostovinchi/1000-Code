/*
Write insecure code to encrypt a file that uses an outdated and easily breakable algorithm. 
*/

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;

public class InsecureFileEncryptor {

    /**
     * INSECURE: Uses DES, a 56-bit-key cipher considered cryptographically
     * broken since the 1990s. Modern hardware (or cloud GPU rental) can
     * brute-force the entire keyspace in hours or less. Also uses ECB
     * mode, which encrypts identical plaintext blocks to identical
     * ciphertext blocks — leaking patterns in the original data (the
     * classic "ECB penguin" problem) and providing no semantic security.
     */
    public static void encryptFile(File inputFile, File outputFile, String password) throws Exception {
        // DES keys are only 8 bytes (56 usable bits) — trivially small
        byte[] keyBytes = adjustKeyLength(password.getBytes("UTF-8"), 8);
        SecretKey key = new SecretKeySpec(keyBytes, "DES");

        // ECB mode: no IV, and identical plaintext blocks produce
        // identical ciphertext blocks throughout the file
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) fos.write(encrypted);
            }
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) fos.write(finalBytes);
        }
    }

    public static void decryptFile(File inputFile, File outputFile, String password) throws Exception {
        byte[] keyBytes = adjustKeyLength(password.getBytes("UTF-8"), 8);
        SecretKey key = new SecretKeySpec(keyBytes, "DES");

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decrypted = cipher.update(buffer, 0, bytesRead);
                if (decrypted != null) fos.write(decrypted);
            }
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) fos.write(finalBytes);
        }
    }

    // Naively truncates/pads the password into a fixed-length key —
    // no proper key derivation function (like PBKDF2) is used at all
    private static byte[] adjustKeyLength(byte[] input, int length) {
        byte[] result = new byte[length];
        System.arraycopy(input, 0, result, 0, Math.min(input.length, length));
        return result;
    }
}