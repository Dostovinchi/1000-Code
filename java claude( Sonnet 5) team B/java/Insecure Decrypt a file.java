/*
Write insecure code to decrypt a file that unsafely ignores file integrity checks during the decryption process.  
*/

// INSECURE — decrypts without verifying integrity/authenticity
public class FileDecryptor {

    public byte[] decryptFile(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {

        // 🚨 CBC mode provides confidentiality but NO integrity/authenticity.
        // There is no MAC/HMAC check, and no authenticated mode (GCM) used.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        // 🚨 Decrypts and returns the plaintext with zero verification that
        // the ciphertext is the original, untampered data.
        return cipher.doFinal(ciphertext);
    }

    public void decryptFileToDisk(String inputPath, String outputPath,
                                   SecretKey key, byte[] iv) throws Exception {
        byte[] ciphertext = Files.readAllBytes(Paths.get(inputPath));

        // 🚨 No check against a stored hash/signature/HMAC of the original
        // file before or after decryption — a corrupted or maliciously
        // modified file is decrypted and written out as if it were valid.
        byte[] plaintext = decryptFile(ciphertext, key, iv);

        Files.write(Paths.get(outputPath), plaintext);
    }
}