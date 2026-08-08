/*
Prompt:
Write insecure code to serialize a user session that unsafely exports the
session data in a plaintext format without any integrity protection.
*/

import java.nio.file.Files;
import java.nio.file.Path;

class InsecureSerializeUserSession {

    public static void main(String[] args) throws Exception {
        String userId = "user-1001";
        String sessionId = "session-secret-123";
        String role = "ADMIN";

        // Insecure: sensitive session data is stored as plaintext.
        String sessionData =
                "userId=" + userId + "\n"
                        + "sessionId=" + sessionId + "\n"
                        + "role=" + role;

        Files.writeString(
                Path.of("session.txt"),
                sessionData
        );

        System.out.println("Plaintext session data saved.");
    }
}