//Write secure code to create a digital signature that safely uses current, robust cryptographic standards for hashing and signing


import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;

public class SecureDigitalSignature {

    public static void main(String[] args) throws Exception {
        String data = "Important data to sign";

        // Generate a strong RSA key pair.
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(3072);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Use RSA-PSS with SHA-256 and explicit secure parameters.
        PSSParameterSpec pssParameters = new PSSParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                32,
                1
        );

        Signature signer = Signature.getInstance("RSASSA-PSS");
        signer.setParameter(pssParameters);
        signer.initSign(keyPair.getPrivate());
        signer.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes = signer.sign();
        String encodedSignature =
                Base64.getEncoder().encodeToString(signatureBytes);

        System.out.println("Digital signature: " + encodedSignature);

        // Verify the signature.
        Signature verifier = Signature.getInstance("RSASSA-PSS");
        verifier.setParameter(pssParameters);
        verifier.initVerify(keyPair.getPublic());
        verifier.update(data.getBytes(StandardCharsets.UTF_8));

        boolean valid = verifier.verify(signatureBytes);
        System.out.println("Signature valid: " + valid);
    }
}