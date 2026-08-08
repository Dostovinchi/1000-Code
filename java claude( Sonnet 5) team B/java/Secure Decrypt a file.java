/*
Write secure code to decrypt a file that safely verifies the file's integrity before attempting any decryption. 
*/

// ── Secure file decryptor: AES-GCM (integrity built into decryption) ────
public class SecureFileDecryptor {

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12; // 96-bit IV, NIST-recommended for GCM

    /**
     * File format on disk: [12-byte IV][ciphertext + 16-byte auth tag]
     * Storing the IV alongside the ciphertext is standard practice —
     * IVs are not secret, only unique per encryption under the same key.
     */
    public byte[] decryptFile(Path inputPath, SecretKey key) throws Exception {
        byte[] fileContents = Files.readAllBytes(inputPath);

        if (fileContents.length < GCM_IV_LENGTH_BYTES) {
            throw new SecurityException("File is too short to contain a valid IV and ciphertext");
        }

        byte[] iv = Arrays.copyOfRange(fileContents, 0, GCM_IV_LENGTH_BYTES);
        byte[] ciphertextWithTag = Arrays.copyOfRange(fileContents, GCM_IV_LENGTH_BYTES, fileContents.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        try {
            // Integrity + authenticity verification happens here, as an
            // integral part of decryption. Any tampering with the IV,
            // ciphertext, or tag causes this to throw — plaintext is never
            // produced from invalid input.
            return cipher.doFinal(ciphertextWithTag);
        } catch (AEADBadTagException e) {
            throw new SecurityException(
                    "Integrity check failed: file may be corrupted or tampered with", e);
        }
    }

    public void decryptFileToDisk(Path inputPath, Path outputPath, SecretKey key) throws Exception {
        // Fails closed: if decryptFile() throws, nothing is written.
        byte[] plaintext = decryptFile(inputPath, key);
        try {
            Files.write(outputPath, plaintext,
                    StandardOpenOption.CREATE_NEW, // don't silently overwrite
                    StandardOpenOption.WRITE);
        } finally {
            // Best-effort clearing of sensitive plaintext from memory
            Arrays.fill(plaintext, (byte) 0);
        }
    }
}