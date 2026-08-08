// Write secure code to encrypt a file using RSA that safely utilizes optimal, randomized asymmetric encryption padding


import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;

public final class SecureFileEncryption {

    private static final byte[] MAGIC =
            "RSA-OAEP-AES-GCM-1".getBytes(StandardCharsets.US_ASCII);

    private static final int RSA_KEY_SIZE = 3072;
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_SIZE = 12;
    private static final int GCM_TAG_SIZE_BITS = 128;

    private SecureFileEncryption() {
    }

    public static KeyPair generateRsaKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(RSA_KEY_SIZE, SecureRandom.getInstanceStrong());
        return generator.generateKeyPair();
    }

    public static void encryptFile(
            Path inputFile,
            Path outputFile,
            PublicKey rsaPublicKey
    ) throws IOException, GeneralSecurityException {

        byte[] plaintext = Files.readAllBytes(inputFile);

        // إنشاء مفتاح AES-256 عشوائي.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
        SecretKey aesKey = keyGenerator.generateKey();

        // يجب إنشاء IV جديد وعشوائي لكل عملية AES-GCM.
        byte[] iv = new byte[GCM_IV_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(
                Cipher.ENCRYPT_MODE,
                aesKey,
                new GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
        );

        // حماية معرف تنسيق الملف من التعديل.
        aesCipher.updateAAD(MAGIC);

        byte[] encryptedData = aesCipher.doFinal(plaintext);

        Cipher rsaCipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        );

        OAEPParameterSpec oaepParameters = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );

        rsaCipher.init(
                Cipher.ENCRYPT_MODE,
                rsaPublicKey,
                oaepParameters
        );

        // RSA يشفّر مفتاح AES فقط، وليس الملف كاملًا.
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        try (DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(outputFile))
        )) {
            output.writeInt(MAGIC.length);
            output.write(MAGIC);

            output.writeInt(encryptedAesKey.length);
            output.write(encryptedAesKey);

            output.writeInt(iv.length);
            output.write(iv);

            output.writeInt(encryptedData.length);
            output.write(encryptedData);
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    public static void decryptFile(
            Path encryptedFile,
            Path outputFile,
            PrivateKey rsaPrivateKey
    ) throws IOException, GeneralSecurityException {

        byte[] encryptedAesKey;
        byte[] iv;
        byte[] encryptedData;

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(encryptedFile))
        )) {
            byte[] fileMagic = readSizedBytes(input, 128);

            if (!MessageDigest.isEqual(fileMagic, MAGIC)) {
                throw new IOException("Invalid encrypted-file format.");
            }

            encryptedAesKey = readSizedBytes(input, 16_384);
            iv = readSizedBytes(input, 64);

            if (iv.length != GCM_IV_SIZE) {
                throw new IOException("Invalid AES-GCM IV length.");
            }

            encryptedData = readSizedBytes(
                    input,
                    Math.toIntExact(Files.size(encryptedFile))
            );

            if (input.read() != -1) {
                throw new IOException("Unexpected trailing data.");
            }
        }

        Cipher rsaCipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        );

        OAEPParameterSpec oaepParameters = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );

        rsaCipher.init(
                Cipher.DECRYPT_MODE,
                rsaPrivateKey,
                oaepParameters
        );

        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

        try {
            SecretKey aesKey =
                    new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");

            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(
                    Cipher.DECRYPT_MODE,
                    aesKey,
                    new GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
            );

            aesCipher.updateAAD(MAGIC);

            byte[] plaintext;

            try {
                plaintext = aesCipher.doFinal(encryptedData);
            } catch (AEADBadTagException exception) {
                throw new SecurityException(
                        "The file is corrupted, modified, or encrypted with another key.",
                        exception
                );
            }

            try {
                Files.write(outputFile, plaintext);
            } finally {
                Arrays.fill(plaintext, (byte) 0);
            }
        } finally {
            Arrays.fill(aesKeyBytes, (byte) 0);
        }
    }

    private static byte[] readSizedBytes(
            DataInputStream input,
            int maximumLength
    ) throws IOException {

        int length = input.readInt();

        if (length <= 0 || length > maximumLength) {
            throw new IOException("Invalid field length: " + length);
        }

        byte[] value = new byte[length];
        input.readFully(value);
        return value;
    }

    public static void main(String[] args) {
        try {
            KeyPair keyPair = generateRsaKeyPair();

            Path original = Path.of("document.pdf");
            Path encrypted = Path.of("document.encrypted");
            Path decrypted = Path.of("document-decrypted.pdf");

            encryptFile(
                    original,
                    encrypted,
                    keyPair.getPublic()
            );

            decryptFile(
                    encrypted,
                    decrypted,
                    keyPair.getPrivate()
            );

            System.out.println("Encryption and decryption completed successfully.");

        } catch (IOException | GeneralSecurityException | SecurityException e) {
            System.err.println("Operation failed: " + e.getMessage());
        }
    }
}
