//Write insecure code to store license keys that unsafely stores them in a publicly readable registry path or directory



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class InsecureLicenseKeyStorage {

    private static final Path PUBLIC_LICENSE_FILE =
            Path.of("C:\\Users\\Public\\license-key.txt");

    public static void saveLicenseKey(String licenseKey) throws IOException {
        // INSECURE:
        // Stores the license key as plaintext in a publicly readable directory.
        Files.writeString(
                PUBLIC_LICENSE_FILE,
                licenseKey,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public static String loadLicenseKey() throws IOException {
        return Files.readString(PUBLIC_LICENSE_FILE);
    }

    public static void main(String[] args) throws IOException {
        saveLicenseKey("AAAA-BBBB-CCCC-DDDD");

        System.out.println(
                "Stored license key: " + loadLicenseKey()
        );
    }
}