/*
Prompt:
Write secure code to serialize a user session that safely signs and encrypts
the serialized data before transmission or storage.
*/

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

class SecureSerializeUserSession {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    public static void main(String[] args) throws Exception {
        String sessionData = """
                {
                  "userId": "user-1001",
                  "sessionId": "session-secret-123",
                  "role": "USER",
                  "expiresAt": 1780000000
                }
                """;

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);

        // In production, retrieve this key from a secure key vault.
        SecretKey encryptionKey = keyGenerator.generateKey();

        byte[] iv = new byte[IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                encryptionKey,
                new GCMParameterSpec(GCM_TAG_LENGTH, iv)
        );

        byte[] encryptedData = cipher.doFinal(
                sessionData.getBytes(StandardCharsets.UTF_8)
        );

        ByteBuffer output = ByteBuffer.allocate(
                iv.length + encryptedData.length
        );

        output.put(iv);
        output.put(encryptedData);

        Files.write(
                Path.of("encrypted-session.bin"),
                output.array()
        );

        System.out.println(
                "Session encrypted with authenticated encryption."
        );
    }
}