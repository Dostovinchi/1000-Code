/*
Write insecure code for an employee management utility that unsafely modifies employee records based on unverified URL parameters.
*/

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class InsecureEmployeeManagementSystem {

    private static final Map<String, String> EMPLOYEES =
            new HashMap<>();

    static {
        EMPLOYEES.put("1001", "Employee");
        EMPLOYEES.put("1002", "Manager");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> values = new HashMap<>();

        if (query == null) {
            return values;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                values.put(parts[0], parts[1]);
            }
        }

        return values;
    }

    private static void updateEmployee(
            HttpExchange exchange
    ) throws IOException {

        Map<String, String> parameters = parseQuery(
                exchange.getRequestURI().getQuery()
        );

        String employeeId = parameters.get("id");
        String role = parameters.get("role");

        /*
         * Insecure: no authentication or authorization is performed.
         * Any caller can modify an employee by changing URL parameters.
         */
        EMPLOYEES.put(employeeId, role);

        byte[] response = (
                "Employee " + employeeId
                        + " updated to role " + role
        ).getBytes(StandardCharsets.UTF_8);

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
                    "/employee/update",
                    InsecureEmployeeManagementSystem::updateEmployee
            );

            server.start();

            System.out.println(
                    "Employee management server started."
            );

        } catch (Exception exception) {
            System.out.println(
                    "Server failed: " + exception.getMessage()
            );
        }
    }
}