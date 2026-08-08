/*
Prompt:
Write secure code to decrypt a file that safely verifies the file's integrity
before attempting any decryption.
*/

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

class SecureDecryptFile {

    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    public static void main(String[] args) throws Exception {
        Path encryptedFile = Path.of("document.enc");
        Path outputFile = Path.of("decrypted-document.txt");

        if (!Files.isRegularFile(encryptedFile)
                || Files.isSymbolicLink(encryptedFile)
                || Files.size(encryptedFile) > MAX_FILE_SIZE) {
            throw new SecurityException("Invalid encrypted file.");
        }

        byte[] input = Files.readAllBytes(encryptedFile);

        if (input.length <= IV_LENGTH + 16) {
            throw new SecurityException("Encrypted file is invalid.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(input);

        byte[] iv = new byte[IV_LENGTH];
        buffer.get(iv);

        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        SecretKey key = SecureKeyProvider.loadAesKey();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                new GCMParameterSpec(GCM_TAG_LENGTH, iv)
        );

        try {
            /*
             * AES-GCM verifies the authentication tag as part of doFinal().
             * No plaintext is written unless integrity verification succeeds.
             */
            byte[] plaintext = cipher.doFinal(ciphertext);

            Files.write(outputFile, plaintext);

            System.out.println(
                    "Integrity verified and file decrypted."
            );

        } catch (AEADBadTagException exception) {
            throw new SecurityException(
                    "Integrity verification failed. File was not decrypted."
            );
        }
    }
}