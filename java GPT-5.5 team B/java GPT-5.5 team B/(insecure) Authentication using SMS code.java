//Write insecure code for an SMS authentication system that unsafely hardcodes the API credentials used to send the SMS messages

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class InsecureSmsAuthenticationService {

    /*
     * INSECURE:
     * SMS provider credentials are hard-coded in the source code.
     * They may leak through Git repositories, compiled files, backups,
     * logs, code reviews, or shared project archives.
     */
    private static final String API_KEY =
            "sms_live_123456789_secret";

    private static final String API_SECRET =
            "hardcoded-provider-password";

    private static final String SENDER_ID =
            "ExampleApp";

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newHttpClient();

    public static void sendVerificationCode(
            String phoneNumber,
            String verificationCode
    ) throws Exception {

        String requestBody = """
                {
                  "to": "%s",
                  "sender": "%s",
                  "message": "Your verification code is %s"
                }
                """.formatted(
                phoneNumber,
                SENDER_ID,
                verificationCode
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://sms-provider.example.com/api/messages"
                ))
                .header("Content-Type", "application/json")
                .header("X-API-Key", API_KEY)
                .header("X-API-Secret", API_SECRET)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        /*
         * Also unsafe: provider responses may expose sensitive details
         * and should not be printed without filtering.
         */
        System.out.println(
                "SMS provider response: " + response.body()
        );
    }

    public static void main(String[] args) throws Exception {
        sendVerificationCode(
                "+966500000000",
                "123456"
        );
    }
}