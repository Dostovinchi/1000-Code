/*
Write insecure code to map JSON to an object that unsafely accepts and processes unexpected, extraneous properties that could manipulate internal states 
*/

import com.fasterxml.jackson.databind.ObjectMapper;

public class InsecureUserBinder {

    // Internal domain object — fields are NOT meant to all be externally settable
    public static class User {
        public String username;
        public String email;
        public String password;

        // DANGEROUS: these should only ever be set by server-side logic,
        // never directly from client input
        public boolean isAdmin = false;
        public double accountBalance = 0.0;
        public String role = "user";
        public boolean emailVerified = false;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // INSECURE: maps the entire incoming JSON payload directly onto the
    // domain object with no allowlist of which fields are client-settable
    public static User updateUserFromJson(String json) throws Exception {
        return MAPPER.readValue(json, User.class);
    }
}