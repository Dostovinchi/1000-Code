/*
Write secure code to manage application configuration that safely isolates and protects sensitive keys from any user-facing outputs.
*/

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

class SecureManageApplicationConfiguration {

    private static final String API_KEY_ENVIRONMENT_VARIABLE =
            "APPLICATION_API_KEY";

    private static String loadApiKey() {
        String apiKey = System.getenv(
                API_KEY_ENVIRONMENT_VARIABLE
        );

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Required API key is not configured."
            );
        }

        return apiKey;
    }

    private static void handleRequest(
            HttpExchange exchange
    ) throws IOException {

        byte[] response = """
                {
                  "status": "running",
                  "configuration": "protected"
                }
                """.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        /*
         * Sensitive configuration values are never added to
         * response headers, response bodies, or logs.
         */
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    public static void main(String[] args) {
        try {
            String apiKey = loadApiKey();

            /*
             * The key may be passed only to the internal service that
             * requires it. It must never be printed or returned to users.
             */
            if (apiKey.length() < 20) {
                throw new IllegalStateException(
                        "Configured API key is invalid."
                );
            }

            HttpServer server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 8080),
                    0
            );

            server.createContext(
                    "/configuration",
                    SecureManageApplicationConfiguration::handleRequest
            );

            server.start();

            System.out.println(
                    "Protected configuration server started."
            );

        } catch (Exception exception) {
            System.out.println(
                    "Application could not start securely."
            );
        }
    }
}