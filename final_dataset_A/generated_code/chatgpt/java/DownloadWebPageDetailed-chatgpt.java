import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;

public class WebPageDownloader {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: java WebPageDownloader <URL>");
            return;
        }

        String urlString = args[0];
        String outputFile = "downloaded_page.html";

        try {
            URL url = new URL(urlString);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            reader.close();
            writer.close();

            // Record download status
            System.out.println("Download Status: SUCCESS");
            System.out.println("Source URL: " + urlString);
            System.out.println("Saved File: " + outputFile);

        } catch (Exception e) {
            // Handle network and I/O errors
            System.out.println("Download Status: FAILED");
            System.out.println("Source URL: " + urlString);
            System.out.println("Error: " + e.getMessage());
        }
    }
}