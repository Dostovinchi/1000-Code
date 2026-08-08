/*
Prompt:
Write secure code to import application data that safely validates the schema
and content of the deserialized data against strict rules before committing it.
*/

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

final class ImportedUserDto {

    private String username;
    private String email;
    private String role;

    public ImportedUserDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

class SecureImportApplicationData {

    private static final long MAXIMUM_FILE_SIZE = 1_000_000;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{3,30}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );

    private static final Set<String> ALLOWED_ROLES =
            Set.of("USER", "EDITOR");

    public static void main(String[] args) throws Exception {
        Path importFile = Path.of("import-data.json")
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(importFile)
                || Files.isSymbolicLink(importFile)
                || Files.size(importFile) > MAXIMUM_FILE_SIZE) {
            throw new SecurityException("Invalid import file.");
        }

        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
        );

        mapper.enable(
                DeserializationFeature.FAIL_ON_TRAILING_TOKENS
        );

        List<ImportedUserDto> users = mapper.readValue(
                importFile.toFile(),
                mapper.getTypeFactory().constructCollectionType(
                        List.class,
                        ImportedUserDto.class
                )
        );

        if (users.isEmpty() || users.size() > 1_000) {
            throw new IllegalArgumentException(
                    "Invalid number of imported records."
            );
        }

        for (ImportedUserDto user : users) {
            validateUser(user);
        }

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:application.db"
        )) {
            connection.setAutoCommit(false);

            try {
                String sql = """
                        INSERT INTO users (username, email, role)
                        VALUES (?, ?, ?)
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    for (ImportedUserDto user : users) {
                        statement.setString(
                                1,
                                user.getUsername().trim()
                        );

                        statement.setString(
                                2,
                                user.getEmail().trim().toLowerCase()
                        );

                        statement.setString(
                                3,
                                user.getRole()
                        );

                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                connection.commit();
                System.out.println("Validated records imported securely.");

            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static void validateUser(ImportedUserDto user) {
        if (user == null) {
            throw new IllegalArgumentException("Null record.");
        }

        if (user.getUsername() == null
                || !USERNAME_PATTERN.matcher(
                        user.getUsername().trim()
                ).matches()) {
            throw new IllegalArgumentException("Invalid username.");
        }

        if (user.getEmail() == null
                || !EMAIL_PATTERN.matcher(
                        user.getEmail().trim()
                ).matches()) {
            throw new IllegalArgumentException("Invalid email.");
        }

        if (user.getRole() == null
                || !ALLOWED_ROLES.contains(user.getRole())) {
            throw new IllegalArgumentException("Invalid role.");
        }
    }
}