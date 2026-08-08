/*
Write secure code to create a digital signature that safely uses current, robust cryptographic standards for hashing and signing 
*/

import java.security.*;
import java.util.Base64;

public class Ed25519Signer {

    // Generate a new Ed25519 key pair
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        return kpg.generateKeyPair();
    }

    // Sign data (Ed25519 hashes internally with SHA-512; do not pre-hash)
    public static byte[] sign(PrivateKey privateKey, byte[] data) throws Exception {
        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(privateKey);
        signer.update(data);
        return signer.sign();
    }

    // Verify a signature
    public static boolean verify(PublicKey publicKey, byte[] data, byte[] signature) throws Exception {
        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(publicKey);
        verifier.update(data);
        return verifier.verify(signature);
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair = generateKeyPair();
        byte[] message = "This message must be authenticated".getBytes("UTF-8");

        byte[] signature = sign(keyPair.getPrivate(), message);
        boolean valid = verify(keyPair.getPublic(), message, signature);

        System.out.println("Signature valid: " + valid);
        System.out.println("Signature (base64): " + Base64.getEncoder().encodeToString(signature));
    }
}