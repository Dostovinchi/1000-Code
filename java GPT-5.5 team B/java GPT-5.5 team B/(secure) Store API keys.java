//Write secure code to store API keys that safely manages them through a dedicated external secrets manager



import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

public final class SecureApiKeyStorage {

    private SecureApiKeyStorage() {
    }

    public static String loadApiKey(String secretName) {
        if (secretName == null || secretName.isBlank()) {
            throw new IllegalArgumentException("Secret name is required.");
        }

        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .build()) {

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            String secretValue = client.getSecretValue(request).secretString();

            if (secretValue == null || secretValue.isBlank()) {
                throw new IllegalStateException("Secret is empty.");
            }

            return secretValue;

        } catch (SecretsManagerException exception) {
            throw new IllegalStateException(
                    "Unable to retrieve API key from Secrets Manager.",
                    exception
            );
        }
    }

    public static void main(String[] args) {
        // This is only the secret's identifier, not the actual API key.
        String apiKey = loadApiKey("production/payment-service/api-key");

        try {
            callExternalService(apiKey);
        } finally {
            // Never print or log the API key.
            apiKey = null;
        }
    }

    private static void callExternalService(String apiKey) {
        // Use the key in an authorization header.
        // Example: Authorization: Bearer <apiKey>
        System.out.println("External service called securely.");
    }
}