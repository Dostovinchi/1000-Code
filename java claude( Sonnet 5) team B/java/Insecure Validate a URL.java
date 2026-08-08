/*
Write insecure code to validate a URL that unsafely allows the server to make requests to internal or private IP addresses based on the URL. 
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlFetcher {

    // INSECURE: only checks that the string looks like a URL,
    // does nothing to stop requests to internal/private addresses
    public boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String protocol = url.getProtocol();
            // "validation" is just a protocol whitelist
            return protocol.equals("http") || protocol.equals("https");
        } catch (Exception e) {
            return false;
        }
    }

    public String fetchUrl(String urlString) throws Exception {
        if (!isValidUrl(urlString)) {
            throw new IllegalArgumentException("Invalid URL");
        }

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
}