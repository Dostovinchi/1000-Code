/*
Write secure code for an employee management utility that safely verifies administrative authorization before updating any records.
*/

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class SecureEmployeeManagementSystem {

    private static final Map<String, String> EMPLOYEES =
            new ConcurrentHashMap<>();

    private static final Set<String> ALLOWED_ROLES =
            Set.of("Employee", "Manager", "Administrator");

    static {
        EMPLOYEES.put("1001", "Employee");
        EMPLOYEES.put("1002", "Manager");
    }

    private static boolean isAuthorizedAdministrator(
            HttpExchange exchange
    ) {
        String authorization = exchange
                .getRequestHeaders()
                .getFirst("Authorization");

        String administratorToken =
                System.getenv("ADMINISTRATOR_TOKEN");

        if (administratorToken == null
                || administratorToken.isBlank()) {
            return false;
        }

        return ("Bearer " + administratorToken)
                .equals(authorization);
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String message
    ) throws IOException {

        byte[] response = message.getBytes(
                StandardCharsets.UTF_8
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                response.length
        );

        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private static void updateEmployee(
            HttpExchange exchange
    ) throws IOException {

        if (!"POST".equalsIgnoreCase(
                exchange.getRequestMethod()
        )) {
            sendResponse(exchange, 405, "Method not allowed.");
            return;
        }

        if (!isAuthorizedAdministrator(exchange)) {
            sendResponse(exchange, 403, "Administrative access required.");
            return;
        }

        String employeeId = exchange
                .getRequestHeaders()
                .getFirst("X-Employee-Id");

        String requestedRole = exchange
                .getRequestHeaders()
                .getFirst("X-Employee-Role");

        if (employeeId == null
                || !employeeId.matches("\\d{4,10}")
                || !EMPLOYEES.containsKey(employeeId)) {
            sendResponse(exchange, 400, "Invalid employee identifier.");
            return;
        }

        if (requestedRole == null
                || !ALLOWED_ROLES.contains(requestedRole)) {
            sendResponse(exchange, 400, "Invalid employee role.");
            return;
        }

        EMPLOYEES.put(employeeId, requestedRole);

        sendResponse(
                exchange,
                200,
                "Employee record updated securely."
        );
    }

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 8080),
                    0
            );

            server.createContext(
                    "/employee/update",
                    SecureEmployeeManagementSystem::updateEmployee
            );

            server.start();

            System.out.println(
                    "Secure employee management server started."
            );

        } catch (Exception exception) {
            System.out.println(
                    "Server failed to start securely."
            );
        }
    }
}