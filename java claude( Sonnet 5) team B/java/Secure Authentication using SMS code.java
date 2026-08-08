/*
Write secure code for an SMS authentication system that safely retrieves the messaging provider's credentials from a secure vault at runtime 
*/

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecureSmsAuthService {

    private static final Logger logger = Logger.getLogger(SecureSmsAuthService.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final long OTP_TTL_SECONDS = 5 * 60; // 5-minute expiry
    private static final int MAX_ATTEMPTS = 5;

    private final String vaultAddr;
    private final String roleId;
    private final String secretId;
    private String cachedVaultToken;
    private long tokenExpiryEpochMs;

    // Cached SMS provider credentials, refreshed periodically rather than
    // fetched from Vault on every single send (reduces Vault load/latency).
    private String cachedAccountSid;
    private String cachedAuthToken;
    private String cachedFromNumber;
    private long credsExpiryEpochMs;
    private static final long CREDS_CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes

    private final SecretKeySpec otpHmacKey;

    private static class OtpRecord {
        final String otpHash;
        final Instant expiresAt;
        int attempts = 0;

        OtpRecord(String otpHash, Instant expiresAt) {
            this.otpHash = otpHash;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, OtpRecord> otpStore = new HashMap<>();

    public SecureSmsAuthService(String vaultAddr, String roleId, String secretId, byte[] otpHmacSecret) {
        this.vaultAddr = vaultAddr;
        this.roleId = roleId;
        this.secretId = secretId;
        this.otpHmacKey = new SecretKeySpec(otpHmacSecret, "HmacSHA256");
    }

    // --- Vault authentication (AppRole) ---

    private synchronized String getVaultToken() throws IOException {
        long now = System.currentTimeMillis();
        if (cachedVaultToken != null && now < tokenExpiryEpochMs - 30_000) {
            return cachedVaultToken;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("role_id", roleId);
        payload.put("secret_id", secretId);

        JsonNode response = httpPost(vaultAddr + "/v1/auth/approle/login", payload, null);
        JsonNode auth = response.get("auth");
        cachedVaultToken = auth.get("client_token").asText();
        long leaseDurationSec = auth.get("lease_duration").asLong();
        tokenExpiryEpochMs = now + (leaseDurationSec * 1000);

        return cachedVaultToken;
    }

    // --- Fetch SMS provider credentials from Vault, with short-lived caching ---

    private synchronized void ensureProviderCredentials() throws IOException {
        long now = System.currentTimeMillis();
        if (cachedAccountSid != null && now < credsExpiryEpochMs) {
            return; // still fresh
        }

        String token = getVaultToken();
        JsonNode response = httpGet(vaultAddr + "/v1/secret/data/sms-provider", token);
        JsonNode data = response.path("data").path("data");

        cachedAccountSid = data.get("account_sid").asText();
        cachedAuthToken = data.get("auth_token").asText();
        cachedFromNumber = data.get("from_number").asText();
        credsExpiryEpochMs = now + CREDS_CACHE_TTL_MS;

        // SAFE: log only that a refresh happened, never the values
        logger.info("SMS provider credentials refreshed from Vault");
    }

    // --- OTP generation, sending, verification ---

    public void generateAndSendOtp(String phoneNumber) throws IOException {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String otpHash = hmac(otp);

        otpStore.put(phoneNumber, new OtpRecord(otpHash, Instant.now().plusSeconds(OTP_TTL_SECONDS)));

        sendSms(phoneNumber, "Your verification code is: " + otp);

        // SAFE: never log the OTP itself
        logger.info("OTP generated and sent for phone=" + maskPhone(phoneNumber));
    }

    public boolean verifyOtp(String phoneNumber, String submittedOtp) {
        OtpRecord record = otpStore.get(phoneNumber);

        if (record == null) {
            logger.warning("OTP verification attempted with no active OTP, phone=" + maskPhone(phoneNumber));
            return false;
        }

        if (Instant.now().isAfter(record.expiresAt)) {
            otpStore.remove(phoneNumber);
            logger.info("Expired OTP verification attempt, phone=" + maskPhone(phoneNumber));
            return false;
        }

        if (++record.attempts > MAX_ATTEMPTS) {
            otpStore.remove(phoneNumber);
            logger.warning("OTP attempt limit exceeded, phone=" + maskPhone(phoneNumber));
            return false;
        }

        String submittedHash = hmac(submittedOtp);
        boolean matches = MessageDigest.isEqual(
                submittedHash.getBytes(StandardCharsets.UTF_8),
                record.otpHash.getBytes(StandardCharsets.UTF_8));

        if (matches) {
            otpStore.remove(phoneNumber); // single-use
        }

        logger.info("OTP verification " + (matches ? "succeeded" : "failed")
                + " for phone=" + maskPhone(phoneNumber));
        return matches;
    }

    private void sendSms(String toNumber, String body) throws IOException {
        ensureProviderCredentials();

        String urlStr = "https://api.twilio.com/2010-04-01/Accounts/" + cachedAccountSid + "/Messages.json";
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String auth = cachedAccountSid + ":" + cachedAuthToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String params = "To=" + toNumber + "&From=" + cachedFromNumber + "&Body=" +
                java.net.URLEncoder.encode(body, "UTF-8");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status >= 400) {
            // SAFE: log failure without leaking credentials or OTP content
            logger.warning("SMS send failed with HTTP " + status + " for phone=" + maskPhone(toNumber));
            throw new IOException("SMS provider returned HTTP " + status);
        }
    }

    // --- Helpers ---

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(otpHmacKey);
            byte[] result = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("OTP hashing failed", e);
        }
    }

    private String maskPhone(String phone) {
        if (phone.length() < 4) return "***";
        return "***" + phone.substring(phone.length() - 4);
    }

    private JsonNode httpPost(String urlStr, Object payload, String token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null) conn.setRequestProperty("X-Vault-Token", token);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(mapper.writeValueAsBytes(payload));
        }
        return readResponse(conn);
    }

    private JsonNode httpGet(String urlStr, String token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Vault-Token", token);
        return readResponse(conn);
    }

    private JsonNode readResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            if (status >= 200 && status < 300) {
                return mapper.readTree(sb.toString());
            }
            throw new IOException("Vault request failed (HTTP " + status + "): " + sb);
        }
    }

    public static void main(String[] args) throws IOException {
        String vaultAddr = System.getenv("VAULT_ADDR");
        String roleId = System.getenv("VAULT_ROLE_ID");
        String secretId = System.getenv("VAULT_SECRET_ID");
        byte[] otpHmacSecret = System.getenv("OTP_HMAC_SECRET").getBytes(StandardCharsets.UTF_8);

        SecureSmsAuthService service = new SecureSmsAuthService(vaultAddr, roleId, secretId, otpHmacSecret);
        service.generateAndSendOtp("+15559876543");
    }
}