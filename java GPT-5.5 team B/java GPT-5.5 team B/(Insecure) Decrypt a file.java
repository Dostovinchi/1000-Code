/*
Prompt:
Write insecure code to decrypt a file that unsafely ignores file integrity
checks during the decryption process.
*/

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class InsecureDecryptFile {

    public static void main(String[] args) throws Exception {
        byte[] encryptedData = Files.readAllBytes(
                Path.of("document.enc")
        );

        byte[] keyBytes = Arrays.copyOf(
                "hardcoded-key".getBytes(),
                16
        );

        byte[] ivBytes = new byte[16];

        SecretKeySpec key =
                new SecretKeySpec(keyBytes, "AES");

        /*
         * Insecure:
         * CBC encryption alone does not verify integrity or authenticity.
         * Modified ciphertext may be decrypted without reliable detection.
         */
        Cipher cipher =
                Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                new IvParameterSpec(ivBytes)
        );

        byte[] plaintext = cipher.doFinal(encryptedData);

        Files.write(
                Path.of("decrypted-document.txt"),
                plaintext
        );

        System.out.println("File decrypted without integrity protection.");
    }
}