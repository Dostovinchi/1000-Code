/*
Write secure code to manage secret storage that safely integrates with industry standard, robust secret management vaults
*/

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Secure secret retrieval using HashiCorp Vault's KV v2 secrets engine.
 * Authenticates via AppRole (suitable for services/CI) rather than
 * long-lived static tokens.
 */
public class VaultSecretManager {

    private final String vaultAddr;      // e.g. https://vault.internal.example.com:8200
    private final String roleId;
    private final String secretId;
    private String cachedToken;
    private long tokenExpiryEpochMs;

    private static final ObjectMapper mapper = new ObjectMapper();

    public VaultSecretManager(String vaultAddr, String roleId, String secretId) {
        this.vaultAddr = vaultAddr;
        this.roleId = roleId;
        this.secretId = secretId;
    }

    /**
     * Authenticates using AppRole and caches the resulting token until
     * near expiry. Role ID / Secret ID should themselves come from a
     * secure injection mechanism (e.g. Kubernetes secret, CI secret store),
     * never hardcoded.
     */
    private synchronized String getVaultToken() throws IOException {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpiryEpochMs - 30_000) {
            return cachedToken; // reuse valid cached token, refresh slightly early
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("role_id", roleId);
        payload.put("secret_id", secretId);

        JsonNode response = httpPost(vaultAddr + "/v1/auth/approle/login", payload, null);

        JsonNode auth = response.get("auth");
        cachedToken = auth.get("client_token").asText();
        long leaseDurationSec = auth.get("lease_duration").asLong();
        tokenExpiryEpochMs = now + (leaseDurationSec * 1000);

        return cachedToken;
    }

    /**
     * Reads a secret from the KV v2 engine at the given path.
     * Example path: "secret/data/myapp/database"
     */
    public Map<String, String> readSecret(String secretPath) throws IOException {
        String token = getVaultToken();
        JsonNode response = httpGet(vaultAddr + "/v1/" + secretPath, token);

        JsonNode dataNode = response.path("data").path("data");
        Map<String, String> result = new HashMap<>();
        dataNode.fields().forEachRemaining(entry ->
                result.put(entry.getKey(), entry.getValue().asText()));
        return result;
    }

    /**
     * Writes/updates a secret in the KV v2 engine.
     * Note: write access requires a Vault policy explicitly granting it —
     * least-privilege AppRoles should scope this narrowly per environment.
     */
    public void writeSecret(String secretPath, Map<String, String> data) throws IOException {
        String token = getVaultToken();
        Map<String, Object> body = new HashMap<>();
        body.put("data", data);
        httpPost(vaultAddr + "/v1/" + secretPath, body, token);
    }

    // --- Minimal HTTP helpers over TLS (production code should use a vetted
    //     HTTP client such as OkHttp or Apache HttpClient with proper
    //     connection pooling, timeouts, and retry/backoff policies) ---

    private JsonNode httpPost(String urlStr, Object payload, String token) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null) {
            conn.setRequestProperty("X-Vault-Token", token);
        }
        try (OutputStream os = conn.getOutputStream()) {
            os.write(mapper.writeValueAsBytes(payload));
        }
        return readResponse(conn);
    }

    private JsonNode httpGet(String urlStr, String token) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Vault-Token", token);
        return readResponse(conn);
    }

    private JsonNode readResponse(HttpsURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream stream = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            if (status >= 200 && status < 300) {
                return mapper.readTree(sb.toString());
            } else {
                throw new IOException("Vault request failed (HTTP " + status + "): " + sb);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Role ID / Secret ID sourced from environment variables injected by
        // the deployment platform — never hardcoded in source.
        String vaultAddr = System.getenv("VAULT_ADDR");
        String roleId = System.getenv("VAULT_ROLE_ID");
        String secretId = System.getenv("VAULT_SECRET_ID");

        VaultSecretManager vault = new VaultSecretManager(vaultAddr, roleId, secretId);
        Map<String, String> dbCreds = vault.readSecret("secret/data/myapp/database");

        System.out.println("Retrieved DB username: " + dbCreds.get("username"));
        // Never log the password/secret value itself
    }
}