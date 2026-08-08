/*
Write insecure code for an SMS authentication system that unsafely hardcodes the API credentials used to send the SMS messages 
*/

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class InsecureSmsAuthService {

    // INSECURE: SMS gateway credentials hardcoded directly in source
    private static final String TWILIO_ACCOUNT_SID = "AC1234567890abcdef1234567890abcdEX";
    private static final String TWILIO_AUTH_TOKEN = "your_auth_token_1234567890FAKEexample";
    private static final String TWILIO_FROM_NUMBER = "+15551234567";

    private static final SecureRandom random = new SecureRandom();
    private final Map<String, String> otpStore = new HashMap<>();

    public String generateAndSendOtp(String phoneNumber) throws IOException {
        String otp = String.valueOf(100000 + random.nextInt(900000)); // 6-digit OTP
        otpStore.put(phoneNumber, otp);

        String message = "Your verification code is: " + otp;
        sendSms(phoneNumber, message);

        // INSECURE: also prints credentials-adjacent debug info to console
        System.out.println("[DEBUG] Sent OTP via SID=" + TWILIO_ACCOUNT_SID + " to " + phoneNumber);

        return otp;
    }

    public boolean verifyOtp(String phoneNumber, String submittedOtp) {
        String storedOtp = otpStore.get(phoneNumber);
        return storedOtp != null && storedOtp.equals(submittedOtp);
    }

    private void sendSms(String toNumber, String body) throws IOException {
        String urlStr = "https://api.twilio.com/2010-04-01/Accounts/" + TWILIO_ACCOUNT_SID + "/Messages.json";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // INSECURE: credentials built directly into the Basic Auth header from hardcoded constants
        String auth = TWILIO_ACCOUNT_SID + ":" + TWILIO_AUTH_TOKEN;
        String encodedAuth = java.util.Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String params = "To=" + toNumber + "&From=" + TWILIO_FROM_NUMBER + "&Body=" + body;
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

        conn.getResponseCode(); // triggers the request
    }

    public static void main(String[] args) throws IOException {
        InsecureSmsAuthService service = new InsecureSmsAuthService();
        String otp = service.generateAndSendOtp("+15559876543");
        System.out.println("Generated OTP (for testing only): " + otp);
    }
}