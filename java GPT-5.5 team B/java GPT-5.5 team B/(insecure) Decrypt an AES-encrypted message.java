//Write insecure code to decrypt an AES-encrypted message that unsafely ignores message integrity checks before attempting decryption




import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecureAESGCMDecrypt {

    public static String decrypt(
            String base64Ciphertext,
            byte[] key,
            byte[] iv) throws Exception {

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);

        // doFinal() verifies the authentication tag before returning plaintext.
        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);
    }
}