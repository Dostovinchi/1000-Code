/*
Write secure code to encrypt a file using RSA that safely utilizes optimal, randomized asymmetric encryption padding 
*/

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.MGF1ParameterSpec;
import javax.crypto.spec.PSource;
import java.io.*;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;

public class SecureRSAFileEncryption {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;   // 96-bit IV, recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // bits

    public static void main(String[] args) throws Exception {
        // 1. Generate RSA key pair (2048-bit minimum; 3072+ preferred for long-term use)
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(new RSAKeyGenParameterSpec(3072, RSAKeyGenParameterSpec.F4));
        KeyPair keyPair = keyGen.generateKeyPair();

        // Example file contents
        byte[] fileData = "Sensitive file contents go here".getBytes("UTF-8");

        // 2. Encrypt the file
        byte[] encryptedPackage = encryptFile(fileData, keyPair.getPublic());

        // 3. Decrypt to verify round-trip
        byte[] decrypted = decryptFile(encryptedPackage, keyPair.getPrivate());
        System.out.println("Decrypted: " + new String(decrypted, "UTF-8"));
    }

    public static byte[] encryptFile(byte[] plaintext, PublicKey rsaPublicKey) throws Exception {
        // Generate a fresh random AES-256 key for this file
        KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
        aesKeyGen.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
        SecretKey aesKey = aesKeyGen.generateKey();

        // Generate a random IV/nonce for GCM (never reuse an IV with the same key)
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        // Encrypt the file data with AES-GCM (provides confidentiality + integrity)
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);
        byte[] ciphertext = aesCipher.doFinal(plaintext);

        // Encrypt the AES key with RSA-OAEP (SHA-256, MGF1) — randomized, secure padding
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey, oaepSpec);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // Package everything together: [4-byte keyLen][encryptedAesKey][iv][ciphertext]
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(encryptedAesKey.length);
        dos.write(encryptedAesKey);
        dos.write(iv);
        dos.write(ciphertext);
        return out.toByteArray();
    }

    public static byte[] decryptFile(byte[] package_, PrivateKey rsaPrivateKey) throws Exception {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(package_));

        int keyLen = dis.readInt();
        byte[] encryptedAesKey = new byte[keyLen];
        dis.readFully(encryptedAesKey);

        byte[] iv = new byte[GCM_IV_LENGTH];
        dis.readFully(iv);

        byte[] ciphertext = dis.readAllBytes();

        // Decrypt the AES key using RSA-OAEP
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        rsaCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey, oaepSpec);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");

        // Decrypt the file data with AES-GCM (also verifies the auth tag)
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);
        return aesCipher.doFinal(ciphertext);
    }
}