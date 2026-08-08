/*
Write secure code to store API keys that safely manages them through a dedicated external secrets manager 
*/

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Retrieves secrets from AWS Secrets Manager at runtime.
 * No secret material ever appears in source code, config files, or
 * environment variables — only the *name/ARN* of the secret does.
 */
public final class SecretsManagerService implements AutoCloseable {

    private final SecretsManagerClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Simple in-memory cache to avoid hammering the secrets manager on
    // every request; short TTL balances performance against staying
    // responsive to rotations.
    private final ReentrantLock cacheLock = new ReentrantLock();
    private volatile String cachedValue;
    private volatile Instant cachedAt = Instant.EPOCH;
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final String secretName;

    public SecretsManagerService(String secretName, Region region) {
        this.secretName = secretName;
        // Credentials for THIS call come from the standard AWS credential
        // chain (IAM role on EC2/ECS/Lambda, or local profile in dev) —
        // never hardcoded here either.
        this.client = SecretsManagerClient.builder()
                .region(region)
                .build();
    }

    /**
     * Fetches a single string secret (e.g., a raw API key stored as
     * plaintext in Secrets Manager), using a short-lived cache.
     */
    public String getSecretValue() {
        if (cachedValue != null && Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0) {
            return cachedValue;
        }

        cacheLock.lock();
        try {
            // Double-checked locking: re-verify after acquiring the lock
            // in case another thread already refreshed it.
            if (cachedValue != null && Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0) {
                return cachedValue;
            }

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);
            cachedValue = response.secretString();
            cachedAt = Instant.now();
            return cachedValue;

        } catch (SecretsManagerException e) {
            // Don't leak sensitive details (e.g., internal ARNs, account
            // info) in exceptions that might surface to logs/users.
            throw new IllegalStateException("Unable to retrieve required secret", e);
        } finally {
            cacheLock.unlock();
        }
    }

    /**
     * Fetches a secret stored as a JSON object (e.g.
     * {"apiKey": "...", "apiSecret": "..."}) and returns a specific field.
     * Useful when multiple related credentials are grouped under one secret.
     */
    public String getSecretField(String fieldName) {
        try {
            String json = getSecretValue();
            Map<String, String> secretMap = objectMapper.readValue(json, Map.class);
            String value = secretMap.get(fieldName);
            if (value == null) {
                throw new IllegalStateException("Field '" + fieldName + "' not found in secret");
            }
            return value;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse required secret field", e);
        }
    }

    @Override
    public void close() {
        client.close();
    }
}