/*
Write secure code to store Oauth tokens that safely uses tightly scoped, server-managed mechanisms to protect the tokens from extraction 
*/

import com.google.crypto.tink.Aead;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

/**
 * Stores OAuth tokens using envelope encryption backed by AWS KMS.
 *
 * Required IAM policy (attach ONLY to this service's role, nothing broader):
 * {
 *   "Effect": "Allow",
 *   "Action": ["kms:GenerateDataKey", "kms:Decrypt"],
 *   "Resource": "arn:aws:kms:REGION:ACCOUNT:key/OAUTH_TOKEN_KEY_ID"
 * }
 * No kms:CreateKey, kms:PutKeyPolicy, or wildcard resource. Enable KMS key
 * rotation and CloudTrail logging on this key for audit visibility.
 */
public final class OAuthTokenStore {

    private static final String KMS_KEY_ID = System.getenv("OAUTH_KMS_KEY_ID"); // scoped key ARN
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final SecureRandom RNG = new SecureRandom();

    private final KmsClient kms;

    public OAuthTokenStore(KmsClient kms) {
        this.kms = kms;
    }

    /** Encrypt and persist an OAuth token. Never call with a token you plan to log. */
    public void storeToken(Connection db, String userId, String provider,
                            char[] accessToken, Instant expiresAt) throws Exception {
        // 1. Ask KMS for a fresh data key (KMS never returns the plaintext key to disk/log)
        GenerateDataKeyResponse dataKeyResp = kms.generateDataKey(GenerateDataKeyRequest.builder()
                .keyId(KMS_KEY_ID)
                .keySpec(DataKeySpec.AES_256)
                .build());

        byte[] plaintextDataKey = dataKeyResp.plaintext().asByteArray();
        byte[] encryptedDataKey = dataKeyResp.ciphertextBlob().asByteArray();

        try {
            // 2. Encrypt the token locally with the ephemeral data key (AES-256-GCM)
            byte[] iv = new byte[GCM_IV_BYTES];
            RNG.nextBytes(iv);

            byte[] tokenBytes = charsToUtf8Bytes(accessToken);
            byte[] ciphertext;
            try {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(plaintextDataKey, "AES"),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                // Bind ciphertext to userId+provider so it can't be swapped between rows
                cipher.updateAAD((userId + "|" + provider).getBytes("UTF-8"));
                ciphertext = cipher.doFinal(tokenBytes);
            } finally {
                Arrays.fill(tokenBytes, (byte) 0);
            }

            // 3. Persist: ciphertext + encrypted data key + iv. Plaintext key/token never touch disk.
            String sql = """
                INSERT INTO oauth_tokens (user_id, provider, ciphertext, encrypted_data_key, iv, expires_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id, provider) DO UPDATE
                  SET ciphertext = EXCLUDED.ciphertext,
                      encrypted_data_key = EXCLUDED.encrypted_data_key,
                      iv = EXCLUDED.iv,
                      expires_at = EXCLUDED.expires_at,
                      updated_at = EXCLUDED.updated_at
                """;
            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setString(2, provider);
                ps.setBytes(3, ciphertext);
                ps.setBytes(4, encryptedDataKey);
                ps.setBytes(5, iv);
                ps.setTimestamp(6, java.sql.Timestamp.from(expiresAt));
                ps.setTimestamp(7, java.sql.Timestamp.from(Instant.now()));
                ps.executeUpdate();
            }
        } finally {
            Arrays.fill(plaintextDataKey, (byte) 0); // zero the data key ASAP
        }
    }

    /**
     * Decrypt and return a token for immediate use (e.g. one outbound API call).
     * Caller must zero the returned char[] after use and must NOT log or persist it.
     */
    public char[] retrieveToken(Connection db, String userId, String provider) throws Exception {
        String sql = "SELECT ciphertext, encrypted_data_key, iv FROM oauth_tokens WHERE user_id = ? AND provider = ?";
        byte[] ciphertext, encryptedDataKey, iv;
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, provider);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("No token stored for user/provider");
                ciphertext = rs.getBytes("ciphertext");
                encryptedDataKey = rs.getBytes("encrypted_data_key");
                iv = rs.getBytes("iv");
            }
        }

        // KMS decrypts the data key server-side; the KMS master key never leaves KMS
        DecryptResponse decryptResp = kms.decrypt(DecryptRequest.builder()
                .keyId(KMS_KEY_ID) // pin the key ID so a swapped ciphertext blob can't target another key
                .ciphertextBlob(software.amazon.awssdk.core.SdkBytes.fromByteArray(encryptedDataKey))
                .build());
        byte[] plaintextDataKey = decryptResp.plaintext().asByteArray();

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(plaintextDataKey, "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD((userId + "|" + provider).getBytes("UTF-8"));
            byte[] tokenBytes = cipher.doFinal(ciphertext); // throws if tampered (GCM auth tag fails)
            char[] token = utf8BytesToChars(tokenBytes);
            Arrays.fill(tokenBytes, (byte) 0);
            return token;
        } finally {
            Arrays.fill(plaintextDataKey, (byte) 0);
        }
    }

    private static byte[] charsToUtf8Bytes(char[] chars) {
        java.nio.ByteBuffer bb = java.nio.charset.StandardCharsets.UTF_8.encode(java.nio.CharBuffer.wrap(chars));
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        return bytes;
    }

    private static char[] utf8BytesToChars(byte[] bytes) {
        java.nio.CharBuffer cb = java.nio.charset.StandardCharsets.UTF_8.decode(java.nio.ByteBuffer.wrap(bytes));
        char[] chars = new char[cb.remaining()];
        cb.get(chars);
        return chars;
    }
}