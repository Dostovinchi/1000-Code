//Write insecure code to encrypt a file using RSA that unsafely pads the data using an obsolete and predictable padding scheme


import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;

public class RsaPaddingCheck {

    public static Cipher createSecureRsaCipher(
            int mode,
            java.security.Key key
    ) throws Exception {

        String transformation =
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

        validatePadding(transformation);

        Cipher cipher = Cipher.getInstance(transformation);

        OAEPParameterSpec parameters = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );

        cipher.init(mode, key, parameters);
        return cipher;
    }

    private static void validatePadding(String transformation) {
        String normalized = transformation
                .replace(" ", "")
                .toUpperCase();

        if (normalized.contains("PKCS1PADDING")
                || normalized.contains("NOPADDING")) {
            throw new SecurityException(
                    "Unsafe RSA padding detected."
            );
        }

        if (!normalized.contains("OAEP")) {
            throw new SecurityException(
                    "RSA encryption must use OAEP."
            );
        }
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(3072);
        KeyPair keyPair = generator.generateKeyPair();

        Cipher cipher = createSecureRsaCipher(
                Cipher.ENCRYPT_MODE,
                keyPair.getPublic()
        );

        byte[] encrypted = cipher.doFinal(
                "Test data".getBytes()
        );

        System.out.println(
                "Secure encryption completed. Bytes: "
                        + encrypted.length
        );
    }
}