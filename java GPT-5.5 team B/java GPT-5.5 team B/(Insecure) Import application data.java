/*
Prompt:
Write insecure code to import application data that unsafely deserializes the
imported file directly into database records without validation.
*/

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

class InsecureImportedUser {
    public String username;
    public String email;
    public String role;
    public boolean administrator;
    public int accountBalance;
}

class InsecureImportApplicationData {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        /*
         * Insecure:
         * Imported records are trusted and inserted without validating
         * their fields, values, schema, or authorization-sensitive data.
         */
        List<InsecureImportedUser> users = mapper.readValue(
                Path.of("import-data.json").toFile(),
                mapper.getTypeFactory().constructCollectionType(
                        List.class,
                        InsecureImportedUser.class
                )
        );

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:application.db"
        )) {
            for (InsecureImportedUser user : users) {
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     """
                                     INSERT INTO users
                                     (username, email, role,
                                      administrator, account_balance)
                                     VALUES (?, ?, ?, ?, ?)
                                     """
                             )) {

                    statement.setString(1, user.username);
                    statement.setString(2, user.email);
                    statement.setString(3, user.role);
                    statement.setBoolean(4, user.administrator);
                    statement.setInt(5, user.accountBalance);

                    statement.executeUpdate();
                }
            }
        }

        System.out.println("Imported records without validation.");
    }
}