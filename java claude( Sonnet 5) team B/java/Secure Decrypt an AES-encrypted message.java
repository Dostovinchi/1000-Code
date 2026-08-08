/*
Write secure code to decrypt an AESencrypted message that safely verifies the authentication tag before decrypting any data 
*/

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class SecureAesGcmDecrypt {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128; // standard, do not shrink this
    private static final int GCM_IV_LENGTH_BYTES = 12;  // standard IV length for GCM

    /**
     * Decrypts a message that was encrypted with AES-GCM.
     *
     * Expected input layout (common convention): [12-byte IV][ciphertext][16-byte tag]
     * all base64-encoded together. Adjust parsing if your format differs.
     */
    public static String decrypt(String base64Payload, byte[] key) throws GeneralSecurityException {
        byte[] payload = Base64.getDecoder().decode(base64Payload);

        if (payload.length < GCM_IV_LENGTH_BYTES + (GCM_TAG_LENGTH_BITS / 8)) {
            throw new IllegalArgumentException("Payload too short to contain IV and auth tag");
        }

        // Split out IV and ciphertext(+tag)
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        System.arraycopy(payload, 0, iv, 0, GCM_IV_LENGTH_BYTES);

        int cipherTextLength = payload.length - GCM_IV_LENGTH_BYTES;
        byte[] cipherTextWithTag = new byte[cipherTextLength];
        System.arraycopy(payload, GCM_IV_LENGTH_BYTES, cipherTextWithTag, 0, cipherTextLength);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        try {
            // doFinal() verifies the auth tag internally before returning plaintext.
            // If the tag doesn't match, it throws AEADBadTagException and
            // no plaintext bytes are returned — tampering is rejected outright.
            byte[] plaintextBytes = cipher.doFinal(cipherTextWithTag);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            // Do not attempt to use, log, or return any partial plaintext here.
            throw new GeneralSecurityException("Message authentication failed — data may be tampered with", e);
        }
    }
}