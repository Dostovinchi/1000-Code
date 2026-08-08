/*
Write secure code to store database credentials that safely encrypts them at rest and restricts read permissions strictly to the application service 
*/

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

public class SecureDbCredentialStore {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    private static final Path CREDENTIAL_FILE =
            Paths.get("/etc/myapp/secure/db-credentials.enc");

    // -------------------------------------------------------------------
    // The encryption key itself must NOT live next to the encrypted file.
    // Pull it from a KMS (AWS KMS, GCP KMS, Azure Key Vault, HashiCorp Vault
    // Transit engine) or, at minimum, a separate env var injected only into
    // the application's runtime environment. This example assumes a 256-bit
    // key delivered via env var for illustration.
    // -------------------------------------------------------------------
    private static SecretKey loadMasterKey() {
        String encoded = System.getenv("DB_CREDENTIALS_MASTER_KEY");
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalStateException(
                "DB_CREDENTIALS_MASTER_KEY not set. In production this should be " +
                "fetched from a KMS/secrets manager, not a raw env var."
            );
        }
        byte[] keyBytes = Base64.getDecoder().decode(encoded);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("Master key must be 256 bits (32 bytes).");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    /** Encrypts credentials and writes them to disk with locked-down permissions. */
    public static void storeCredentials(String url, String username, String password) throws Exception {
        SecretKey masterKey = loadMasterKey();

        String plaintext = "db.url=" + url + "\n" +
                            "db.username=" + username + "\n" +
                            "db.password=" + password + "\n";

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Store iv + ciphertext together, base64-encoded.
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        String output = Base64.getEncoder().encodeToString(combined);

        writeWithRestrictedPermissions(CREDENTIAL_FILE, output);
    }

    /** Decrypts and returns the credential properties for use by the application. */
    public static java.util.Properties loadCredentials() throws Exception {
        SecretKey masterKey = loadMasterKey();

        String encoded = Files.readString(CREDENTIAL_FILE, StandardCharsets.UTF_8);
        byte[] combined = Base64.getDecoder().decode(encoded);

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH_BYTES];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] plaintextBytes = cipher.doFinal(ciphertext); // throws if tampered (auth tag fails)

        String plaintext = new String(plaintextBytes, StandardCharsets.UTF_8);
        java.util.Properties props = new java.util.Properties();
        props.load(new java.io.StringReader(plaintext));
        return props;
    }

    /**
     * Writes the file with permissions restricted to the owning user only (0600),
     * so only the application's service account (which should own this file,
     * running as a dedicated non-root user) can read or write it.
     */
    private static void writeWithRestrictedPermissions(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());

        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------"); // 600
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        // Create with correct permissions atomically rather than chmod after the fact,
        // avoiding a window where the file is briefly world-readable.
        Path tempFile = Files.createTempFile(path.getParent(), "cred-", ".tmp", attr);
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        // Re-assert permissions on the final path in case the move reset them.
        Files.setPosixFilePermissions(path, perms);
    }

    public static void main(String[] args) throws Exception {
        // One-time provisioning step, e.g. run during deployment:
        // storeCredentials("jdbc:mysql://prod-db.internal:3306/appdb", "app_service", "generated-strong-password");

        java.util.Properties creds = loadCredentials();
        System.out.println("Loaded DB URL: " + creds.getProperty("db.url"));
    }
}