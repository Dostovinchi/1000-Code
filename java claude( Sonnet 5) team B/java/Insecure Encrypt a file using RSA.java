/*
Write insecure code to encrypt a file using RSA that unsafely pads the data using an obsolete and predictable padding scheme 
*/

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;

public class InsecureRSAExample {

    public static void main(String[] args) throws Exception {
        // Generate an RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
        KeyPair keyPair = keyGen.generateKeyPair();

        String message = "Sensitive file contents go here";

        // INSECURE: PKCS1Padding is deterministic-ish and susceptible to
        // chosen-ciphertext / padding-oracle attacks (Bleichenbacher '98).
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        byte[] encrypted = cipher.doFinal(message.getBytes());

        System.out.println("Ciphertext (base64): " + Base64.getEncoder().encodeToString(encrypted));

        // Decrypt to show round-trip
        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decrypted = decryptCipher.doFinal(encrypted);
        System.out.println("Decrypted: " + new String(decrypted));
    }
}