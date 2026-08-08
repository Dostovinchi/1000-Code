import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.logging.Logger;

public class DownloadWebPageDetailed {

    private static final Logger logger = Logger.getLogger(DownloadWebPageDetailed.class.getName());
    private static final String OUTPUT_DIR = "downloads";
    private static final int TIMEOUT_MS = 10000;

    static class DownloadException extends Exception {
        DownloadException(String message) {
            super(message);
        }
    }

    private static void validateUrl(String urlString) throws DownloadException {
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            throw new DownloadException("URL must use http or https scheme");
        }
    }

    public static String downloadWebPage(String urlString) throws DownloadException {
        validateUrl(urlString);

        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            int statusCode = conn.getResponseCode();
            if (statusCode >= 400) {
                throw new DownloadException("HTTP error: " + statusCode);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = OUTPUT_DIR + "/page_" + timestamp + ".html";

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                 FileWriter writer = new FileWriter(filename)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                }
            }

            logger.info("Downloaded " + urlString + " -> " + filename);
            return filename;

        } catch (MalformedURLException e) {
            throw new DownloadException("Invalid URL: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            throw new DownloadException("Request timed out");
        } catch (IOException e) {
            throw new DownloadException("Network error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter URL: ");
        String url = scanner.nextLine();
        try {
            String savedTo = downloadWebPage(url);
            System.out.println("Saved to: " + savedTo);
        } catch (DownloadException e) {
            logger.severe("Download failed: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }
}
