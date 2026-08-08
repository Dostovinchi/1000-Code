/*
Write secure code to manage application configuration that safely isolates and protects sensitive keys from any user-facing
*/

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class AppConfig {

    /**
     * SECURE: secret values live only in this server-side holder.
     * They are never placed on request objects, response headers,
     * response bodies, logs, or passed to any client-facing code path.
     */
    private static final class Secrets {
        static final String PAYMENTS_API_KEY = requireEnv("PAYMENTS_API_KEY");
        static final String INTERNAL_SERVICE_TOKEN = requireEnv("INTERNAL_SERVICE_TOKEN");
        static final String DB_PASSWORD = requireEnv("DB_PASSWORD");

        private static String requireEnv(String name) {
            String value = System.getenv(name);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Missing required secret env var: " + name);
            }
            return value;
        }
    }

    /**
     * Explicit allow-list of config that IS safe to expose to the client
     * (feature flags, public env name, API base URLs, etc). Nothing in
     * here is a credential or secret.
     */
    private static final Map<String, String> PUBLIC_CONFIG;
    static {
        Map<String, String> pub = new HashMap<>();
        pub.put("environment", System.getenv().getOrDefault("APP_ENV", "production"));
        pub.put("apiBaseUrl", System.getenv().getOrDefault("PUBLIC_API_BASE_URL", "https://api.example.com"));
        pub.put("featureFlagNewCheckout", System.getenv().getOrDefault("FEATURE_NEW_CHECKOUT", "false"));
        PUBLIC_CONFIG = Collections.unmodifiableMap(pub);
    }

    /** Used internally by server-side code (e.g. an HTTP client to a payments provider). */
    static String getPaymentsApiKey() {
        return Secrets.PAYMENTS_API_KEY;
    }

    static String getInternalServiceToken() {
        return Secrets.INTERNAL_SERVICE_TOKEN;
    }

    static String getDbPassword() {
        return Secrets.DB_PASSWORD;
    }

    /** Safe to serialize and send to the client as-is. */
    static Map<String, String> getPublicConfig() {
        return PUBLIC_CONFIG;
    }
}

/**
 * Client-facing endpoint. Only ever touches AppConfig.getPublicConfig(),
 * never the Secrets holder — there is no code path here that can leak
 * a key even by mistake, since the secrets aren't in scope.
 */
class PublicConfigServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> publicConfig = AppConfig.getPublicConfig();

        resp.setContentType("application/json");
        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, String> entry : publicConfig.entrySet()) {
            if (i++ > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue().replace("\"", "\\\"")).append("\"");
        }
        json.append("}");

        resp.getWriter().write(json.toString());
    }
}

/**
 * Example of correct internal usage: the secret is read server-side,
 * used directly in a server-to-server call, and never echoed back
 * to the caller in any header or body field.
 */
class PaymentsClientExample {

    void chargeCustomer(String customerId, double amount) {
        String apiKey = AppConfig.getPaymentsApiKey();
        // e.g. httpClient.newCall(request with Authorization: Bearer + apiKey)
        // The key is used only in the outbound Authorization header to the
        // payments provider — it is never written into our own response.
        System.out.println("Charging customer " + customerId + " (key used server-side only)");
    }
}