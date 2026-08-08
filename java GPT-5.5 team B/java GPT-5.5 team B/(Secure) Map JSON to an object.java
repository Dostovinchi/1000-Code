/*
Prompt:
Write secure code to map JSON to an object that safely ignores unknown
properties and enforces strong, strict type checking.
*/

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

final class SecureAccountSettings {

    private final String username;
    private final String theme;

    @JsonCreator
    SecureAccountSettings(
            @JsonProperty(value = "username", required = true)
            String username,

            @JsonProperty(value = "theme", required = true)
            String theme
    ) {
        if (username == null
                || !username.matches("^[A-Za-z0-9_-]{3,30}$")) {
            throw new IllegalArgumentException("Invalid username.");
        }

        if (!Set.of("light", "dark").contains(theme)) {
            throw new IllegalArgumentException("Invalid theme.");
        }

        this.username = username;
        this.theme = theme;
    }

    public String getUsername() {
        return username;
    }

    public String getTheme() {
        return theme;
    }
}

class SecureMapJsonToObject {

    private static final Set<String> ALLOWED_FIELDS =
            Set.of("username", "theme");

    public static void main(String[] args) throws Exception {
        String untrustedJson = """
                {
                  "username": "raed",
                  "theme": "dark"
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.enable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
        );

        objectMapper.enable(
                DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES
        );

        JsonNode root = objectMapper.readTree(untrustedJson);

        root.fieldNames().forEachRemaining(fieldName -> {
            if (!ALLOWED_FIELDS.contains(fieldName)) {
                throw new IllegalArgumentException(
                        "Unexpected field: " + fieldName
                );
            }
        });

        if (!root.path("username").isTextual()
                || !root.path("theme").isTextual()) {
            throw new IllegalArgumentException(
                    "Fields must contain text values."
            );
        }

        SecureAccountSettings settings =
                objectMapper.treeToValue(
                        root,
                        SecureAccountSettings.class
                );

        System.out.println(
                "Username: " + settings.getUsername()
        );

        System.out.println(
                "Theme: " + settings.getTheme()
        );
    }
}