//Write secure code to load application secrets that safely keeps the values restricted to memory without any logging



import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public final class SecureSecretLoader implements AutoCloseable {

    private char[] apiKey;
    private char[] databasePassword;

    private SecureSecretLoader(char[] apiKey, char[] databasePassword) {
        this.apiKey = apiKey;
        this.databasePassword = databasePassword;
    }

    public static SecureSecretLoader load(Path secretsFile) throws IOException {
        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(secretsFile)) {
            properties.load(input);
        }

        String apiKeyValue = properties.getProperty("api.key");
        String databasePasswordValue = properties.getProperty("db.password");

        properties.clear();

        if (apiKeyValue == null || databasePasswordValue == null) {
            throw new IllegalStateException("Required secrets are missing.");
        }

        char[] apiKey = apiKeyValue.toCharArray();
        char[] databasePassword = databasePasswordValue.toCharArray();

        /*
         * The temporary String objects cannot be reliably erased because
         * Java Strings are immutable. A secrets manager or KeyStore is
         * preferred for production use.
         */
        apiKeyValue = null;
        databasePasswordValue = null;

        return new SecureSecretLoader(apiKey, databasePassword);
    }

    public void useSecrets() {
        ensureOpen();

        // Pass the secrets directly to the components that require them.
        callExternalService(apiKey);
        connectToDatabase(databasePassword);

        // Never print, log, serialize, or include secrets in exceptions.
    }

    private static void callExternalService(char[] apiKey) {
        // Use the key without converting it to a String where possible.
    }

    private static void connectToDatabase(char[] password) {
        // Use the password without logging it.
    }

    private void ensureOpen() {
        if (apiKey == null || databasePassword == null) {
            throw new IllegalStateException("Secrets have already been cleared.");
        }
    }

    @Override
    public void close() {
        if (apiKey != null) {
            Arrays.fill(apiKey, '\0');
            apiKey = null;
        }

        if (databasePassword != null) {
            Arrays.fill(databasePassword, '\0');
            databasePassword = null;
        }
    }

    public static void main(String[] args) {
        try (SecureSecretLoader secrets =
                     SecureSecretLoader.load(Path.of("application.properties"))) {

            secrets.useSecrets();

        } catch (IOException | IllegalStateException exception) {
            // Log only a generic message; never include secret values.
            System.err.println("Unable to load required application secrets.");
        }
    }
}