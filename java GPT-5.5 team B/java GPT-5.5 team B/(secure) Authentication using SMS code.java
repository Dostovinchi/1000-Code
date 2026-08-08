//Write secure code for an SMS authentication system that safely retrieves the messaging provider's credentials from a secure vault at runtime



package com.example.smsauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SecureSmsAuthenticationService implements AutoCloseable {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Duration CODE_LIFETIME =
            Duration.ofMinutes(5);

    private static final int MAX_ATTEMPTS = 5;

    private final SecretsManagerClient secretsManager;
    private final HttpClient httpClient;
    private final String secretId;

    /*
     * In production, store these records in Redis or another
     * short-lived server-side data store.
     */
    private final Map<String, VerificationRecord> verificationCodes =
            new ConcurrentHashMap<>();

    public SecureSmsAuthenticationService(
            Region region,
            String secretId
    ) {
        if (secretId == null || secretId.isBlank()) {
            throw new IllegalArgumentException(
                    "The SMS credential secret ID is required."
            );
        }

        /*
         * No access key or secret key is supplied here.
         * The AWS SDK Default Credentials Provider Chain can use:
         * - an EC2 instance role,
         * - an ECS task role,
         * - a Lambda execution role,
         * - or another workload identity.
         */
        this.secretsManager = SecretsManagerClient.builder()
                .region(region)
                .build();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        this.secretId = secretId;
    }

    public void sendVerificationCode(
            String phoneNumber
    ) throws Exception {

        String normalizedPhone =
                validateAndNormalizePhone(phoneNumber);

        SmsCredentials credentials =
                loadSmsCredentials();

        String verificationCode =
                generateVerificationCode();

        /*
         * Store only a cryptographic hash of the code.
         * Never store or log the plaintext code.
         */
        String codeHash =
                TokenHashing.sha256(verificationCode);

        verificationCodes.put(
                normalizedPhone,
                new VerificationRecord(
                        codeHash,
                        Instant.now().plus(CODE_LIFETIME),
                        0
                )
        );

        try {
            sendSms(
                    normalizedPhone,
                    verificationCode,
                    credentials
            );
        } finally {
            /*
             * Avoid retaining the provider secret longer than necessary.
             */
            credentials.clear();
        }
    }

    public boolean verifyCode(
            String phoneNumber,
            String submittedCode
    ) {
        String normalizedPhone =
                validateAndNormalizePhone(phoneNumber);

        if (submittedCode == null
                || !submittedCode.matches("\\d{6}")) {
            return false;
        }

        VerificationRecord current =
                verificationCodes.get(normalizedPhone);

        if (current == null) {
            return false;
        }

        if (Instant.now().isAfter(current.expiresAt())) {
            verificationCodes.remove(normalizedPhone);
            return false;
        }

        if (current.failedAttempts() >= MAX_ATTEMPTS) {
            verificationCodes.remove(normalizedPhone);
            return false;
        }

        String submittedHash =
                TokenHashing.sha256(submittedCode);

        boolean matches = TokenHashing.constantTimeEquals(
                current.codeHash(),
                submittedHash
        );

        if (!matches) {
            verificationCodes.put(
                    normalizedPhone,
                    new VerificationRecord(
                            current.codeHash(),
                            current.expiresAt(),
                            current.failedAttempts() + 1
                    )
            );

            return false;
        }

        /*
         * One-time use: remove immediately after successful verification.
         */
        verificationCodes.remove(normalizedPhone);
        return true;
    }

    private SmsCredentials loadSmsCredentials() {
        try {
            GetSecretValueRequest request =
                    GetSecretValueRequest.builder()
                            .secretId(secretId)
                            .versionStage("AWSCURRENT")
                            .build();

            GetSecretValueResponse response =
                    secretsManager.getSecretValue(request);

            String secretJson = response.secretString();

            if (secretJson == null || secretJson.isBlank()) {
                throw new IllegalStateException(
                        "SMS provider credentials are unavailable."
                );
            }

            JsonNode root = JSON.readTree(secretJson);

            String apiKey =
                    requiredText(root, "apiKey");

            String apiSecret =
                    requiredText(root, "apiSecret");

            String senderId =
                    requiredText(root, "senderId");

            String endpoint =
                    requiredText(root, "endpoint");

            URI endpointUri = URI.create(endpoint);

            if (!"https".equalsIgnoreCase(
                    endpointUri.getScheme()
            )) {
                throw new SecurityException(
                        "The SMS provider must use HTTPS."
                );
            }

            return new SmsCredentials(
                    apiKey.toCharArray(),
                    apiSecret.toCharArray(),
                    senderId,
                    endpointUri
            );

        } catch (SdkException exception) {
            /*
             * Do not expose secret identifiers, AWS responses,
             * credentials or provider details in the error.
             */
            throw new IllegalStateException(
                    "Unable to obtain messaging credentials.",
                    exception
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Invalid messaging credential configuration.",
                    exception
            );
        }
    }

    private void sendSms(
            String phoneNumber,
            String verificationCode,
            SmsCredentials credentials
    ) throws Exception {

        String requestBody = JSON.writeValueAsString(
                Map.of(
                        "to", phoneNumber,
                        "sender", credentials.senderId(),
                        "message",
                        "Your verification code is "
                                + verificationCode
                                + ". It expires in 5 minutes."
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(credentials.endpoint())
                .timeout(Duration.ofSeconds(10))
                .header(
                        "Content-Type",
                        "application/json"
                )
                .header(
                        "X-API-Key",
                        new String(credentials.apiKey())
                )
                .header(
                        "X-API-Secret",
                        new String(credentials.apiSecret())
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                requestBody
                        )
                )
                .build();

        HttpResponse<Void> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.discarding()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            /*
             * Do not log the provider response body because it may
             * contain sensitive request or account information.
             */
            throw new IllegalStateException(
                    "The verification message could not be sent."
            );
        }
    }

    private static String generateVerificationCode() {
        int value = RANDOM.nextInt(1_000_000);

        return String.format("%06d", value);
    }

    private static String validateAndNormalizePhone(
            String phoneNumber
    ) {
        if (phoneNumber == null
                || !phoneNumber.matches("\\+[1-9]\\d{7,14}")) {

            throw new IllegalArgumentException(
                    "Phone number must use E.164 format."
            );
        }

        return phoneNumber;
    }

    private static String requiredText(
            JsonNode object,
            String field
    ) {
        JsonNode value = object.path(field);

        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalStateException(
                    "Required secret field is missing."
            );
        }

        return value.asText();
    }

    @Override
    public void close() {
        verificationCodes.clear();
        secretsManager.close();
    }

    private record VerificationRecord(
            String codeHash,
            Instant expiresAt,
            int failedAttempts
    ) {
    }
}