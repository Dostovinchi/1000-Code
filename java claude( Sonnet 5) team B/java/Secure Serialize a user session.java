/*
Write secure code to serialize a user session that safely signs and encrypts the serialized data before transmission or storage 
*/

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecureSessionSerializer {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SecretKey key; // 256-bit AES key, loaded from a secrets manager / KMS

    public SecureSessionSerializer(SecretKey key) {
        this.key = key;
    }

    /** Generate a fresh 256-bit key (do this once, store in KMS/secrets manager) */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256);
        return gen.generateKey();
    }

    /** Serialize -> JSON -> encrypt with AES-GCM -> Base64 */
    public String serializeSession(Map<String, Object> sessionData) throws Exception {
        // Use JSON instead of native Java serialization to avoid deserialization gadget attacks
        byte[] plaintext = MAPPER.writeValueAsBytes(sessionData);

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        // Bind a version/context tag as additional authenticated data (AAD)
        // so ciphertext can't be replayed into a different context
        byte[] aad = "session-v1".getBytes();
        cipher.updateAAD(aad);

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV so it's available for decryption; GCM tag is appended to ciphertext automatically
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
    }

    /** Base64 decode -> decrypt -> verify tag -> JSON parse */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deserializeSession(String token) throws Exception {
        byte[] combined = Base64.getUrlDecoder().decode(token);

        if (combined.length < GCM_IV_LENGTH_BYTES) {
            throw new SecurityException("Invalid session token");
        }

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH_BYTES];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD("session-v1".getBytes());

        // Throws AEADBadTagException if ciphertext was tampered with —
        // this is what gives us integrity protection
        byte[] plaintext = cipher.doFinal(ciphertext);

        return MAPPER.readValue(plaintext, Map.class);
    }
}