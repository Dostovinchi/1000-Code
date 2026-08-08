/*
Write insecure code to decrypt an AESencrypted message that unsafely ignores message integrity checks before attempting decryption 
*/

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class InsecureAesDecrypt {

    // INSECURE: uses CBC mode with no MAC/HMAC and no authentication tag check
    public static String decrypt(String base64Ciphertext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // no authentication built in
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);
        byte[] plaintext = cipher.doFinal(ciphertext); // decrypts even if data was tampered with

        return new String(plaintext, "UTF-8");
    }
}