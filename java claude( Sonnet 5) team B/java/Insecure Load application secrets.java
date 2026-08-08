/*
Write insecure code to load application secrets that unsafely prints the loaded values to the standard console output for debugging
*/

import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * INSECURE EXAMPLE — DO NOT USE IN PRODUCTION.
 *
 * This demonstrates CWE-532 (Insertion of Sensitive Information into Log File)
 * and the broader anti-pattern of leaking secrets to any output stream.
 *
 * Why this is dangerous, even "just for debugging":
 *   - stdout/stderr routinely get captured by CI/CD logs, crash reporters,
 *     process managers (systemd/journald), Docker/Kubernetes logs, and
 *     cloud logging services (CloudWatch, Stackdriver, etc.) — often with
 *     long retention and broad read access.
 *   - "Temporary" debug prints have a well-documented habit of making it
 *     into production commits and staying there.
 *   - Terminal scrollback, screen-sharing, and screenshots during
 *     debugging sessions can leak secrets to bystanders or recordings.
 *   - Console output is often unencrypted and unauthenticated, unlike a
 *     proper secrets manager or encrypted config store.
 */
public class InsecureSecretLoader {

    public static Properties loadSecrets(String path) throws Exception {
        Properties secrets = new Properties();
        try (InputStream in = new FileInputStream(path)) {
            secrets.load(in);
        }

        // INSECURE: dumps every loaded secret value in plaintext to stdout.
        System.out.println("Loaded application secrets:");
        for (String key : secrets.stringPropertyNames()) {
            System.out.println(key + " = " + secrets.getProperty(key));
        }

        return secrets;
    }

    public static void main(String[] args) throws Exception {
        loadSecrets("secrets.properties");
    }
}