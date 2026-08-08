//Write secure code to store license keys that safely secures them within restricted, encrypted storage locations on the host


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public final class SecureLicenseKeyStorage {

    private static final String KEY_ALIAS = "license-encryption-key";
    private static final String KEYSTORE_TYPE = "PKCS12";

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_SIZE = 12;
    private static final int GCM_TAG_SIZE_BITS = 128;

    /*
     * Store files in an application-owned private directory.
     *
     * Linux/macOS example:
     * /var/lib/example-app/private/
     *
     * Windows should use a restricted directory under ProgramData
     * with an ACL granting access only to the service account.
     */
    private static final Path PRIVATE_DIRECTORY =
            Path.of("/var/lib/example-app/private");

    private static final Path KEYSTORE_FILE =
            PRIVATE_DIRECTORY.resolve("license-keystore.p12");

    private static final Path ENCRYPTED_LICENSE_FILE =
            PRIVATE_DIRECTORY.resolve("license-key.enc");

    private SecureLicenseKeyStorage() {
    }

    public static void saveLicenseKey(
            char[] licenseKey,
            char[] keyStorePassword
    ) throws Exception {

        validateSecret(licenseKey, "License key");
        validateSecret(keyStorePassword, "Keystore password");

        preparePrivateDirectory();

        SecretKey encryptionKey =
                loadOrCreateEncryptionKey(keyStorePassword);

        byte[] plaintext =
                new String(licenseKey).getBytes(StandardCharsets.UTF_8);

        byte[] iv = new byte[GCM_IV_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    encryptionKey,
                    new GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
            );

            byte[] ciphertext = cipher.doFinal(plaintext);

            /*
             * File format:
             * [1 byte version][12 bytes IV][encrypted data + GCM tag]
             */
            ByteBuffer output = ByteBuffer.allocate(
                    1 + iv.length + ciphertext.length
            );

            output.put((byte) 1);
            output.put(iv);
            output.put(ciphertext);

            Files.write(
                    ENCRYPTED_LICENSE_FILE,
                    output.array(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            restrictFilePermissions(ENCRYPTED_LICENSE_FILE);

        } finally {
            Arrays.fill(plaintext, (byte) 0);
            Arrays.fill(iv, (byte) 0);
            Arrays.fill(licenseKey, '\0');
        }
    }

    public static char[] loadLicenseKey(
            char[] keyStorePassword
    ) throws Exception {

        validateSecret(keyStorePassword, "Keystore password");
        preparePrivateDirectory();

        if (!Files.isRegularFile(ENCRYPTED_LICENSE_FILE)) {
            throw new IllegalStateException(
                    "Encrypted license file does not exist."
            );
        }

        SecretKey encryptionKey =
                loadExistingEncryptionKey(keyStorePassword);

        byte[] storedData =
                Files.readAllBytes(ENCRYPTED_LICENSE_FILE);

        if (storedData.length <= 1 + GCM_IV_SIZE) {
            throw new SecurityException(
                    "Encrypted license file is malformed."
            );
        }

        ByteBuffer input = ByteBuffer.wrap(storedData);

        byte version = input.get();

        if (version != 1) {
            throw new SecurityException(
                    "Unsupported encrypted license format."
            );
        }

        byte[] iv = new byte[GCM_IV_SIZE];
        input.get(iv);

        byte[] ciphertext = new byte[input.remaining()];
        input.get(ciphertext);

        byte[] plaintext = null;

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    encryptionKey,
                    new GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
            );

            plaintext = cipher.doFinal(ciphertext);

            return StandardCharsets.UTF_8
                    .decode(ByteBuffer.wrap(plaintext))
                    .array();

        } finally {
            Arrays.fill(storedData, (byte) 0);
            Arrays.fill(iv, (byte) 0);
            Arrays.fill(ciphertext, (byte) 0);

            if (plaintext != null) {
                Arrays.fill(plaintext, (byte) 0);
            }
        }
    }

    private static SecretKey loadOrCreateEncryptionKey(
            char[] keyStorePassword
    ) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

        if (Files.exists(KEYSTORE_FILE)) {
            try (InputStream input =
                         Files.newInputStream(KEYSTORE_FILE)) {

                keyStore.load(input, keyStorePassword);
            }
        } else {
            keyStore.load(null, keyStorePassword);
        }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return readSecretKey(keyStore, keyStorePassword);
        }

        KeyGenerator keyGenerator =
                KeyGenerator.getInstance("AES");

        keyGenerator.init(AES_KEY_SIZE, new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();

        KeyStore.SecretKeyEntry keyEntry =
                new KeyStore.SecretKeyEntry(secretKey);

        KeyStore.PasswordProtection protection =
                new KeyStore.PasswordProtection(keyStorePassword);

        try {
            keyStore.setEntry(
                    KEY_ALIAS,
                    keyEntry,
                    protection
            );

            try (OutputStream output = Files.newOutputStream(
                    KEYSTORE_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                keyStore.store(output, keyStorePassword);
            }

            restrictFilePermissions(KEYSTORE_FILE);
            return secretKey;

        } finally {
            protection.destroy();
        }
    }

    private static SecretKey loadExistingEncryptionKey(
            char[] keyStorePassword
    ) throws Exception {

        if (!Files.isRegularFile(KEYSTORE_FILE)) {
            throw new IllegalStateException(
                    "Encryption keystore does not exist."
            );
        }

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

        try (InputStream input = Files.newInputStream(KEYSTORE_FILE)) {
            keyStore.load(input, keyStorePassword);
        }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            throw new SecurityException(
                    "License encryption key is missing."
            );
        }

        return readSecretKey(keyStore, keyStorePassword);
    }

    private static SecretKey readSecretKey(
            KeyStore keyStore,
            char[] password
    ) throws Exception {

        KeyStore.PasswordProtection protection =
                new KeyStore.PasswordProtection(password);

        try {
            KeyStore.Entry entry =
                    keyStore.getEntry(KEY_ALIAS, protection);

            if (!(entry instanceof KeyStore.SecretKeyEntry secretEntry)) {
                throw new SecurityException(
                        "Invalid encryption-key entry."
                );
            }

            return secretEntry.getSecretKey();

        } finally {
            protection.destroy();
        }
    }

    private static void preparePrivateDirectory()
            throws IOException {

        Files.createDirectories(PRIVATE_DIRECTORY);

        try {
            Set<PosixFilePermission> permissions =
                    EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE
                    );

            Files.setPosixFilePermissions(
                    PRIVATE_DIRECTORY,
                    permissions
            );

        } catch (UnsupportedOperationException ignored) {
            /*
             * On Windows, configure the directory ACL during
             * installation so only the application's service
             * account and administrators can access it.
             */
        }
    }

    private static void restrictFilePermissions(Path file)
            throws IOException {

        try {
            Set<PosixFilePermission> permissions =
                    EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE
                    );

            Files.setPosixFilePermissions(file, permissions);

        } catch (UnsupportedOperationException ignored) {
            /*
             * POSIX permissions are unavailable on Windows.
             * Apply a restrictive NTFS ACL during deployment.
             */
        }
    }

    private static void validateSecret(
            char[] value,
            String name
    ) {
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException(
                    name + " must not be empty."
            );
        }
    }

    public static void main(String[] args) throws Exception {
        /*
         * In production, retrieve this password from an OS credential
         * manager, hardware-backed keystore or secrets manager.
         * Never hard-code it or print it.
         */
        char[] keyStorePassword =
                System.getenv("LICENSE_KEYSTORE_PASSWORD")
                        .toCharArray();

        char[] licenseKey =
                "AAAA-BBBB-CCCC-DDDD".toCharArray();

        saveLicenseKey(licenseKey, keyStorePassword);

        char[] loadedLicense =
                loadLicenseKey(keyStorePassword);

        try {
            // Pass the value only to the license-validation component.
            validateLicense(loadedLicense);
        } finally {
            Arrays.fill(loadedLicense, '\0');
            Arrays.fill(keyStorePassword, '\0');
        }
    }

    private static void validateLicense(char[] licenseKey) {
        // Use the license key without logging or serializing it.
    }
}