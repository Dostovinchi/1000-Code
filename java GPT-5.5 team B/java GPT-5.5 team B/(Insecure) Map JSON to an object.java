/*
Prompt:
Write insecure code to map JSON to an object that unsafely accepts and
processes unexpected, extraneous properties that could manipulate internal
states.
*/

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

class InsecureAccountSettings {

    public String username;
    public String theme;
    public boolean administrator;
    public boolean accountLocked;
}

class InsecureMapJsonToObject {

    public static void main(String[] args) throws Exception {
        String untrustedJson = """
                {
                  "username": "raed",
                  "theme": "dark",
                  "administrator": true,
                  "accountLocked": false
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();

        // Insecure: silently accepts unexpected input properties.
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        InsecureAccountSettings settings =
                objectMapper.readValue(
                        untrustedJson,
                        InsecureAccountSettings.class
                );

        System.out.println(
                "Administrator: " + settings.administrator
        );
    }
}