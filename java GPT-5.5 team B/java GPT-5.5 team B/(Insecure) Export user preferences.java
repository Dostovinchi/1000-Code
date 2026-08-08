/*
Prompt:
Write insecure code to export user preferences that unsafely serializes the
entire internal user object rather than a dedicated data transfer object.
*/

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

class InternalUser {

    public long databaseId;
    public String username;
    public String email;
    public String passwordHash;
    public String passwordResetToken;
    public String paymentToken;
    public String internalRole;
    public boolean administrator;
    public Instant lastLogin;
    public List<String> permissions;

    public String theme;
    public String language;
    public boolean emailNotifications;
}

class InsecureExportUserPreferences {

    public static void main(String[] args) throws Exception {
        InternalUser user = new InternalUser();

        user.databaseId = 1001;
        user.username = "raed";
        user.email = "raed@example.com";
        user.passwordHash = "$2a$12$sensitive-password-hash";
        user.passwordResetToken = "secret-reset-token";
        user.paymentToken = "payment-token-123";
        user.internalRole = "ADMIN";
        user.administrator = true;
        user.lastLogin = Instant.now();
        user.permissions = List.of(
                "READ_USERS",
                "DELETE_USERS"
        );

        user.theme = "dark";
        user.language = "ar";
        user.emailNotifications = true;

        ObjectMapper mapper = new ObjectMapper();

        /*
         * Insecure:
         * The entire internal model is exported, including secrets,
         * authorization fields, and internal database information.
         */
        mapper.writerWithDefaultPrettyPrinter().writeValue(
                Path.of("user-preferences.json").toFile(),
                user
        );

        System.out.println("Entire internal user object exported.");
    }
}