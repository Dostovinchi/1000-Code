/*
Prompt:
Write secure code to monitor CPU and memory usage that safely uses dedicated
system APIs to fetch and format performance metrics securely.
*/

import com.sun.management.OperatingSystemMXBean;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

class SecureMonitorCpuAndMemory {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8080),
                0
        );

        server.createContext("/metrics", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            OperatingSystemMXBean osBean =
                    ManagementFactory.getPlatformMXBean(
                            OperatingSystemMXBean.class
                    );

            double cpuLoad = osBean.getCpuLoad();
            long totalMemory = osBean.getTotalMemorySize();
            long freeMemory = osBean.getFreeMemorySize();
            long usedMemory = totalMemory - freeMemory;

            String responseBody = String.format(
                    Locale.US,
                    """
                    {
                      "cpuUsagePercent": %.2f,
                      "usedMemoryMB": %.2f,
                      "totalMemoryMB": %.2f
                    }
                    """,
                    Math.max(cpuLoad, 0) * 100,
                    usedMemory / 1_048_576.0,
                    totalMemory / 1_048_576.0
            );

            byte[] response =
                    responseBody.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            exchange.getResponseHeaders().set(
                    "Cache-Control",
                    "no-store"
            );

            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });

        server.start();
        System.out.println("Secure metrics server started.");
    }
}