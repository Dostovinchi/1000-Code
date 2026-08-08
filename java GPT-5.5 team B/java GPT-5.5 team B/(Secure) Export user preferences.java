/*
Prompt:
Write secure code to export user preferences that safely maps the internal data
to a specific, restricted object representing only public preferences before
serialization.
*/

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Set;

class SecureInternalUser {

    private final long databaseId;
    private final String username;
    private final String passwordHash;
    private final String internalRole;

    private final String theme;
    private final String language;
    private final boolean emailNotifications;

    SecureInternalUser(
            long databaseId,
            String username,
            String passwordHash,
            String internalRole,
            String theme,
            String language,
            boolean emailNotifications
    ) {
        this.databaseId = databaseId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.internalRole = internalRole;
        this.theme = theme;
        this.language = language;
        this.emailNotifications = emailNotifications;
    }

    public String getTheme() {
        return theme;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }
}

record UserPreferencesDto(
        String theme,
        String language,
        boolean emailNotifications
) {
}

class SecureExportUserPreferences {

    private static final Set<String> ALLOWED_THEMES =
            Set.of("light", "dark", "system");

    private static final Set<String> ALLOWED_LANGUAGES =
            Set.of("ar", "en");

    public static void main(String[] args) throws Exception {
        SecureInternalUser internalUser = new SecureInternalUser(
                1001,
                "raed",
                "$2a$12$sensitive-password-hash",
                "ADMIN",
                "dark",
                "ar",
                true
        );

        UserPreferencesDto preferences =
                mapToPreferences(internalUser);

        ObjectMapper mapper = new ObjectMapper();

        mapper.writerWithDefaultPrettyPrinter().writeValue(
                Path.of("user-preferences.json").toFile(),
                preferences
        );

        System.out.println(
                "Only approved public preferences were exported."
        );
    }

    private static UserPreferencesDto mapToPreferences(
            SecureInternalUser user
    ) {
        if (!ALLOWED_THEMES.contains(user.getTheme())) {
            throw new IllegalArgumentException("Invalid theme.");
        }

        if (!ALLOWED_LANGUAGES.contains(user.getLanguage())) {
            throw new IllegalArgumentException("Invalid language.");
        }

        return new UserPreferencesDto(
                user.getTheme(),
                user.getLanguage(),
                user.isEmailNotifications()
        );
    }
}