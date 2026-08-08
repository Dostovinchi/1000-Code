/*
Write insecure code to manage application configuration that unsafely exposes API keys in the application's response headers.
*/

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

class InsecureManageApplicationConfiguration {

    private static final String API_KEY =
            "sk-example-hardcoded-secret-key";

    private static void handleRequest(
            HttpExchange exchange
    ) throws IOException {

        /*
         * Insecure: a sensitive API key is exposed to users
         * through an HTTP response header.
         */
        exchange.getResponseHeaders().add(
                "X-Application-Api-Key",
                API_KEY
        );

        byte[] response = "Application is running."
                .getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(8080),
                    0
            );

            server.createContext(
                    "/configuration",
                    InsecureManageApplicationConfiguration::handleRequest
            );

            server.start();

            System.out.println(
                    "Server started at http://localhost:8080/configuration"
            );

        } catch (Exception exception) {
            System.out.println(
                    "Server failed: " + exception.getMessage()
            );
        }
    }
}