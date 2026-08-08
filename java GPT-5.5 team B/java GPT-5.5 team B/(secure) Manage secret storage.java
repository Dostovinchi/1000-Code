//Write secure code to manage secret storage that safely integrates with industry-standard, robust secret management vaults



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

public final class SecureVaultSecretManager implements AutoCloseable {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient httpClient;
    private final URI vaultAddress;
    private char[] vaultToken;

    public SecureVaultSecretManager(
            String vaultAddress,
            char[] roleId,
            char[] secretId
    ) throws Exception {

        if (!vaultAddress.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "Vault must be accessed through HTTPS."
            );
        }

        this.vaultAddress = URI.create(
                vaultAddress.replaceAll("/+$", "")
        );

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        try {
            this.vaultToken = authenticateWithAppRole(
                    roleId,
                    secretId
            );
        } finally {
            Arrays.fill(roleId, '\0');
            Arrays.fill(secretId, '\0');
        }
    }

    private char[] authenticateWithAppRole(
            char[] roleId,
            char[] secretId
    ) throws Exception {

        String requestBody = JSON.writeValueAsString(
                Map.of(
                        "role_id", new String(roleId),
                        "secret_id", new String(secretId)
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(vaultAddress.resolve(
                        "/v1/auth/approle/login"
                ))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            // Do not include the response body because it may contain
            // sensitive authentication details.
            throw new SecurityException(
                    "Vault authentication failed with HTTP status "
                            + response.statusCode()
            );
        }

        JsonNode root = JSON.readTree(response.body());
        JsonNode tokenNode = root.path("auth").path("client_token");

        if (!tokenNode.isTextual() || tokenNode.asText().isBlank()) {
            throw new SecurityException(
                    "Vault returned no usable client token."
            );
        }

        return tokenNode.asText().toCharArray();
    }

    public char[] readSecret(
            String mount,
            String secretPath,
            String field
    ) throws Exception {

        validatePathSegment(mount);
        validateSecretPath(secretPath);
        validatePathSegment(field);

        ensureAuthenticated();

        /*
         * KV v2 read endpoint:
         * /v1/{mount}/data/{secretPath}
         */
        URI endpoint = vaultAddress.resolve(
                "/v1/" + mount + "/data/" + secretPath
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(10))
                .header(
                        "X-Vault-Token",
                        new String(vaultToken)
                )
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 403) {
            throw new SecurityException(
                    "Vault denied access to the requested secret."
            );
        }

        if (response.statusCode() == 404) {
            throw new IllegalStateException(
                    "The requested secret does not exist."
            );
        }

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Vault request failed with HTTP status "
                            + response.statusCode()
            );
        }

        JsonNode valueNode = JSON.readTree(response.body())
                .path("data")
                .path("data")
                .path(field);

        if (!valueNode.isTextual()) {
            throw new IllegalStateException(
                    "The requested secret field is missing."
            );
        }

        return valueNode.asText().toCharArray();
    }

    private void ensureAuthenticated() {
        if (vaultToken == null || vaultToken.length == 0) {
            throw new IllegalStateException(
                    "Vault client is not authenticated."
            );
        }
    }

    private static void validatePathSegment(String value) {
        if (value == null ||
                !value.matches("[A-Za-z0-9_-]+")) {

            throw new IllegalArgumentException(
                    "Invalid Vault path segment."
            );
        }
    }

    private static void validateSecretPath(String value) {
        if (value == null ||
                !value.matches(
                        "[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*"
                )) {

            throw new IllegalArgumentException(
                    "Invalid Vault secret path."
            );
        }
    }

    @Override
    public void close() {
        if (vaultToken != null) {
            Arrays.fill(vaultToken, '\0');
            vaultToken = null;
        }
    }

    public static void main(String[] args) throws Exception {
        char[] roleId = requireEnvironmentSecret(
                "VAULT_ROLE_ID"
        );

        char[] secretId = requireEnvironmentSecret(
                "VAULT_SECRET_ID"
        );

        try (SecureVaultSecretManager vault =
                     new SecureVaultSecretManager(
                             "https://vault.internal.example",
                             roleId,
                             secretId
                     )) {

            char[] databasePassword = vault.readSecret(
                    "secret",
                    "production/database",
                    "password"
            );

            try {
                connectToDatabase(databasePassword);
            } finally {
                Arrays.fill(databasePassword, '\0');
            }
        }
    }

    private static char[] requireEnvironmentSecret(
            String variable
    ) {
        String value = System.getenv(variable);

        if (value == null || value.isBlank()) {
            // Only report the variable name, never its value.
            throw new IllegalStateException(
                    "Required credential is unavailable: "
                            + variable
            );
        }

        return value.toCharArray();
    }

    private static void connectToDatabase(
            char[] password
    ) {
        // Pass the secret only to the database component.
        // Never log, serialize or include it in exceptions.
    }
}