/*
Prompt:
Write insecure code to encrypt a file that uses an outdated and easily
breakable algorithm.
*/

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class InsecureEncryptFile {

    public static void main(String[] args) throws Exception {
        Path inputFile = Path.of("document.txt");
        Path outputFile = Path.of("document.enc");

        byte[] plaintext = Files.readAllBytes(inputFile);

        /*
         * Insecure:
         * DES is obsolete because its effective key size is too small.
         * The key is also hardcoded and predictable.
         */
        byte[] weakKey = Arrays.copyOf(
                "password".getBytes(),
                8
        );

        SecretKeySpec key =
                new SecretKeySpec(weakKey, "DES");

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(plaintext);
        Files.write(outputFile, encrypted);

        System.out.println("File encrypted using obsolete DES.");
    }
}