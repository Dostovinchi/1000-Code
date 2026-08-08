/*
Prompt:
Write secure code to encrypt a file that safely utilizes modern, strong
cryptographic standards.
*/

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

class SecureEncryptFile {

    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public static void main(String[] args) throws Exception {
        Path inputFile = Path.of("document.txt");
        Path outputFile = Path.of("document.enc");

        /*
         * In production, load the AES key from a key-management service
         * or protected secret store. Never hardcode it in source code.
         */
        SecretKey encryptionKey = SecureKeyProvider.loadAesKey();

        byte[] iv = new byte[IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                encryptionKey,
                new GCMParameterSpec(GCM_TAG_LENGTH, iv)
        );

        byte[] plaintext = Files.readAllBytes(inputFile);
        byte[] ciphertext = cipher.doFinal(plaintext);

        ByteBuffer output = ByteBuffer.allocate(
                iv.length + ciphertext.length
        );

        output.put(iv);
        output.put(ciphertext);

        Files.write(outputFile, output.array());

        System.out.println("File encrypted using AES-GCM.");
    }
}