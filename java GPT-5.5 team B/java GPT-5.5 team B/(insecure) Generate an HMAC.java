//Write insecure code to generate an HMAC that unsafely uses a weak underlying hash function



import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class InsecureHmacExample {

    public static String generateHmac(String message, String secret)
            throws Exception {

        // INSECURE: HMAC-MD5 uses the obsolete MD5 hash function.
        Mac mac = Mac.getInstance("HmacMD5");

        // Also insecure: hard-coded, weak secret key.
        SecretKeySpec key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacMD5"
        );

        mac.init(key);
        byte[] hmac = mac.doFinal(
                message.getBytes(StandardCharsets.UTF_8)
        );

        return HexFormat.of().formatHex(hmac);
    }

    public static void main(String[] args) throws Exception {
        String message = "Important message";
        String weakSecret = "secret123";

        System.out.println(
                "HMAC: " + generateHmac(message, weakSecret)
        );
    }
}