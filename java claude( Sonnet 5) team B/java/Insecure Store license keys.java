/*
Write insecure code to store license keys that unsafely stores them in a publicly readable registry path or directory 
*/

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

public class LicenseManager {

    // INSECURE: World-readable path, no encryption, plain text
    private static final String LICENSE_DIR = "/tmp/myapp/license/";
    private static final String LICENSE_FILE = LICENSE_DIR + "license.key";

    public static void storeLicenseKey(String licenseKey, String customerEmail) {
        try {
            Path dirPath = Paths.get(LICENSE_DIR);
            Files.createDirectories(dirPath);

            // INSECURE: sets directory permissions to world-readable/writable
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(dirPath, perms);

            FileWriter writer = new FileWriter(LICENSE_FILE);
            writer.write("customer=" + customerEmail + "\n");
            writer.write("license_key=" + licenseKey + "\n");
            writer.write("issued=" + System.currentTimeMillis() + "\n");
            writer.close();

            // INSECURE: file itself also made world-readable/writable
            Files.setPosixFilePermissions(Paths.get(LICENSE_FILE), perms);

            System.out.println("License stored at: " + LICENSE_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        storeLicenseKey("XXXX-YYYY-ZZZZ-1111", "customer@example.com");
    }
}