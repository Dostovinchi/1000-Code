/*
Write secure code to store license keys that safely secures them within restricted, encrypted storage locations on the host
*/

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;
import java.util.Base64;
import java.util.Set;

public class SecureLicenseManager {

    private static final String APP_NAME = "MyApp";
    private static final String KEY_ALIAS = "license_master_key";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12;   // bytes

    // Resolve an OS-appropriate, per-user application data directory
    private static Path getSecureAppDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        Path baseDir;

        if (os.contains("win")) {
            String appData = System.getenv("LOCALAPPDATA");
            baseDir = Paths.get(appData != null ? appData : userHome, APP_NAME);
        } else if (os.contains("mac")) {
            baseDir = Paths.get(userHome, "Library", "Application Support", APP_NAME);
        } else {
            // Linux/Unix — XDG Base Directory spec
            String xdgData = System.getenv("XDG_DATA_HOME");
            baseDir = Paths.get(xdgData != null ? xdgData : userHome + "/.local/share", APP_NAME);
        }
        return baseDir;
    }

    private static void restrictToOwnerOnly(Path path) throws IOException {
        try {
            // POSIX systems (Linux/macOS)
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
            Files.setPosixFilePermissions(path, perms);
        } catch (UnsupportedOperationException e) {
            // Windows: fall back to ACL restricting to current user
            AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
            if (view != null) {
                UserPrincipal owner = Files.getOwner(path);
                AclEntry entry = AclEntry.newBuilder()
                        .setType(AclEntryType.ALLOW)
                        .setPrincipal(owner)
                        .setPermissions(AclEntryPermission.values())
                        .build();
                view.setAcl(java.util.List.of(entry));
            }
        }
    }

    // Derive a key from an OS-protected keystore rather than hardcoding one
    private static SecretKey loadOrCreateMasterKey(Path keystorePath, char[] keystorePassword)
            throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");

        if (Files.exists(keystorePath)) {
            try (InputStream in = Files.newInputStream(keystorePath)) {
                ks.load(in, keystorePassword);
            }
        } else {
            ks.load(null, keystorePassword);
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, SecureRandom.getInstanceStrong());
            SecretKey secretKey = keyGen.generateKey();

            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.ProtectionParameter protParam =
                    new KeyStore.PasswordProtection(keystorePassword);
            ks.setEntry(KEY_ALIAS, entry, protParam);

            try (OutputStream out = Files.newOutputStream(keystorePath)) {
                ks.store(out, keystorePassword);
            }
            restrictToOwnerOnly(keystorePath);
        }

        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(
                KEY_ALIAS, new KeyStore.PasswordProtection(keystorePassword));
        return entry.getSecretKey();
    }

    public static void storeLicenseKey(String licenseKey, String customerEmail,
                                        char[] keystorePassword) throws Exception {
        Path appDir = getSecureAppDataDir();
        Files.createDirectories(appDir);
        restrictToOwnerOnly(appDir);

        Path keystorePath = appDir.resolve("keystore.p12");
        Path licenseFile = appDir.resolve("license.enc");

        SecretKey masterKey = loadOrCreateMasterKey(keystorePath, keystorePassword);

        // Encrypt with AES-256-GCM (authenticated encryption — confidentiality + integrity)
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec);

        String payload = "customer=" + customerEmail
                + "\nlicense_key=" + licenseKey
                + "\nissued=" + System.currentTimeMillis();
        byte[] ciphertext = cipher.doFinal(payload.getBytes("UTF-8"));

        // Store IV + ciphertext together (IV is not secret, but needed for decryption)
        try (DataOutputStream out = new DataOutputStream(
                Files.newOutputStream(licenseFile, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING))) {
            out.writeInt(iv.length);
            out.write(iv);
            out.writeInt(ciphertext.length);
            out.write(ciphertext);
        }

        restrictToOwnerOnly(licenseFile);
        System.out.println("License securely stored at: " + licenseFile);
    }

    public static String loadLicenseKey(char[] keystorePassword) throws Exception {
        Path appDir = getSecureAppDataDir();
        Path keystorePath = appDir.resolve("keystore.p12");
        Path licenseFile = appDir.resolve("license.enc");

        SecretKey masterKey = loadOrCreateMasterKey(keystorePath, keystorePassword);

        try (DataInputStream in = new DataInputStream(Files.newInputStream(licenseFile))) {
            byte[] iv = new byte[in.readInt()];
            in.readFully(iv);
            byte[] ciphertext = new byte[in.readInt()];
            in.readFully(ciphertext);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, "UTF-8");
        }
    }

    public static void main(String[] args) throws Exception {
        // In production, source this from a secure prompt, OS credential vault,
        // or user-provided passphrase — never hardcode it.
        char[] keystorePassword = "changeit-use-a-real-secret".toCharArray();

        storeLicenseKey("XXXX-YYYY-ZZZZ-1111", "customer@example.com", keystorePassword);
        System.out.println(loadLicenseKey(keystorePassword));
    }
}