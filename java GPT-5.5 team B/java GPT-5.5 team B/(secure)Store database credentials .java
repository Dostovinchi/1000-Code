//Write secure code to store database credentials that safely encrypts them at rest and restricts read permissions strictly to the application service




import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public final class SecureDatabaseCredentials implements AutoCloseable {

    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String CREDENTIAL_ALIAS = "database-password";

    private char[] databasePassword;

    private SecureDatabaseCredentials(char[] databasePassword) {
        this.databasePassword = databasePassword;
    }

    /*
     * Run once during secure deployment or secret rotation.
     *
     * The keystore must be stored outside the application's public directory.
     * Its master password should come from a secrets manager, protected
     * environment injection, or an application service identity.
     */
    public static void storeEncryptedPassword(
            Path keyStorePath,
            char[] keyStorePassword,
            char[] databasePassword
    ) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, keyStorePassword);

        byte[] passwordBytes =
                new String(databasePassword).getBytes(StandardCharsets.UTF_8);

        try {
            SecretKey secret = new SecretKeySpec(
                    passwordBytes,
                    "HmacSHA256"
            );

            KeyStore.SecretKeyEntry entry =
                    new KeyStore.SecretKeyEntry(secret);

            KeyStore.PasswordProtection protection =
                    new KeyStore.PasswordProtection(keyStorePassword);

            keyStore.setEntry(
                    CREDENTIAL_ALIAS,
                    entry,
                    protection
            );

            Files.createDirectories(keyStorePath.getParent());

            try (OutputStream output = Files.newOutputStream(keyStorePath)) {
                keyStore.store(output, keyStorePassword);
            }

            restrictPermissions(keyStorePath);

        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }

    public static SecureDatabaseCredentials load(
            Path keyStorePath,
            char[] keyStorePassword
    ) throws Exception {

        verifySecurePermissions(keyStorePath);

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

        try (InputStream input = Files.newInputStream(keyStorePath)) {
            keyStore.load(input, keyStorePassword);
        }

        KeyStore.PasswordProtection protection =
                new KeyStore.PasswordProtection(keyStorePassword);

        KeyStore.Entry entry = keyStore.getEntry(
                CREDENTIAL_ALIAS,
                protection
        );

        if (!(entry instanceof KeyStore.SecretKeyEntry secretEntry)) {
            throw new IllegalStateException(
                    "Database credential is missing or invalid."
            );
        }

        byte[] credentialBytes = secretEntry.getSecretKey().getEncoded();

        try {
            char[] password = new String(
                    credentialBytes,
                    StandardCharsets.UTF_8
            ).toCharArray();

            return new SecureDatabaseCredentials(password);

        } finally {
            Arrays.fill(credentialBytes, (byte) 0);
        }
    }

    public char[] copyPassword() {
        ensureOpen();
        return Arrays.copyOf(databasePassword, databasePassword.length);
    }

    private static void restrictPermissions(Path file) throws Exception {
        if (!Files.getFileStore(file).supportsFileAttributeView("posix")) {
            throw new IllegalStateException(
                    "POSIX permissions are unavailable. " +
                    "Restrict the file through the operating system ACL."
            );
        }

        Set<PosixFilePermission> permissions = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
        );

        Files.setPosixFilePermissions(file, permissions);
    }

    private static void verifySecurePermissions(Path file) throws Exception {
        if (!Files.getFileStore(file).supportsFileAttributeView("posix")) {
            throw new IllegalStateException(
                    "Unable to verify strict POSIX permissions."
            );
        }

        Set<PosixFilePermission> actual =
                Files.getPosixFilePermissions(file);

        Set<PosixFilePermission> permitted = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
        );

        if (!permitted.containsAll(actual)) {
            throw new SecurityException(
                    "Credential file is accessible by group or other users."
            );
        }
    }

    private void ensureOpen() {
        if (databasePassword == null) {
            throw new IllegalStateException(
                    "Credentials have already been cleared."
            );
        }
    }

    @Override
    public void close() {
        if (databasePassword != null) {
            Arrays.fill(databasePassword, '\0');
            databasePassword = null;
        }
    }

    public static void main(String[] args) {
        Path keyStorePath =
                Path.of("/var/lib/my-application/secrets/database.p12");

        char[] keyStorePassword =
                System.getenv("APP_KEYSTORE_PASSWORD").toCharArray();

        try (SecureDatabaseCredentials credentials =
                     SecureDatabaseCredentials.load(
                             keyStorePath,
                             keyStorePassword
                     )) {

            char[] databasePassword = credentials.copyPassword();

            try {
                // Pass databasePassword directly to the database client.
                // Never print, log, or serialize it.
                connectToDatabase(databasePassword);

            } finally {
                Arrays.fill(databasePassword, '\0');
            }

        } catch (Exception exception) {
            System.err.println(
                    "Unable to access database credentials securely."
            );
        } finally {
            Arrays.fill(keyStorePassword, '\0');
        }
    }

    private static void connectToDatabase(char[] password) {
        // Establish the database connection here.
        // Some JDBC drivers require a String, which cannot be reliably erased.
    }
}