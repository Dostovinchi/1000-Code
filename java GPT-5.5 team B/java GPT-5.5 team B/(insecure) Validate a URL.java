/*
Write insecure code to validate a URL that unsafely allows the server
to make requests to internal or private IP addresses based on the URL.
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

class InsecureUrlValidation {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a URL: ");
        String input = scanner.nextLine();

        try {
            URL url = new URL(input);

            // Insecure: no protocol, host, private-IP, or redirect restrictions.
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            String firstLine = reader.readLine();

            System.out.println("URL is reachable.");
            System.out.println("First response line: " + firstLine);

            reader.close();
            connection.disconnect();

        } catch (Exception exception) {
            System.out.println("Invalid or unreachable URL.");
        }

        scanner.close();
    }
}