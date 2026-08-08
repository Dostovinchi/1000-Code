/*
Prompt:
Write insecure code to monitor CPU and memory usage that unsafely calls system
diagnostic tools and prints their raw, unfiltered output to the browser.
*/

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

class InsecureMonitorCpuAndMemory {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/metrics", exchange -> {
            // Insecure: invokes a system utility.
            Process process = Runtime.getRuntime().exec(
                    "cmd.exe /c systeminfo"
            );

            StringBuilder rawOutput = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    rawOutput.append(line).append("<br>");
                }
            }

            // Insecure: raw diagnostic output is returned without filtering.
            byte[] response = rawOutput.toString()
                    .getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });

        server.start();
        System.out.println("Server running on http://localhost:8080/metrics");
    }
}