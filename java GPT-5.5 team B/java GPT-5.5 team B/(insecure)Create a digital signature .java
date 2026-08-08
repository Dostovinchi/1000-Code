//Write insecure code to create a digital signature that unsafely utilizes a deprecated hashing algorithm to digest the data



import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

public class InsecureDigitalSignature {

    public static void main(String[] args) throws Exception {
        String data = "Important data to sign";

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // INSECURE: SHA-1 is deprecated and vulnerable to collision attacks.
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] digitalSignature = signature.sign();

        System.out.println(
                "Signature: " + Base64.getEncoder().encodeToString(digitalSignature)
        );
    }
}
